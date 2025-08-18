package com.team3.otboo.domain.weather.service;

import com.team3.otboo.converter.GridConverter;
import com.team3.otboo.domain.notification.service.OnceDeduper;
import com.team3.otboo.domain.user.entity.Location;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.weather.dto.KakaoGeoResponse;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.ForecastItem;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;
import com.team3.otboo.domain.weather.entity.*;
import com.team3.otboo.domain.weather.enums.*;
import com.team3.otboo.domain.weather.repository.WeatherRepository;
import com.team3.otboo.event.WeatherAlert;
import com.team3.otboo.external.WeatherExternal;
import com.team3.otboo.global.exception.weather.ExternalApiException;
import com.team3.otboo.global.exception.weather.WeatherApiException;
import com.team3.otboo.props.external.ExternalApisProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.team3.otboo.domain.weather.enums.WeatherApiParams;
import static com.team3.otboo.domain.weather.enums.Category.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

  @Qualifier("locationRestTemplate")
  private final RestTemplate locationRestTemplate;

  private final ExternalApisProperties apisProps;
  private final ProfileRepository profileRepository;
  private final WeatherRepository weatherRepository;
  private final WeatherExternal weatherExternal;
  private final TemperatureDeltaRule temperatureDeltaRule;
  private final ApplicationEventPublisher eventPublisher;
  private final OnceDeduper onceDeduper;

  private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.BASIC_ISO_DATE;
  private static final DateTimeFormatter DATE_TIME_PARSER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  @Override
  @Transactional
  public List<WeatherDto> getWeatherForUser(LocationRequest locationRequest) {
    ZonedDateTime nowKst = ZonedDateTime.now(KST);
    ZonedDateTime startKst = nowKst.toLocalDate().atStartOfDay(KST); // 오늘 00:00 KST
    ZonedDateTime endKstExclusive = startKst.plusDays(5);            // +5일 00:00 KST

    LocalDate startDate = startKst.toLocalDate();
    LocalDateTime from = startKst.toLocalDateTime();
    LocalDateTime toExclusive = endKstExclusive.toLocalDateTime();

    LocationResponse lr = getLocationForUser(locationRequest);

    var base = WeatherApiParams.currentBase();
    LocalDateTime forecastedAt = LocalDateTime.parse(base.date() + base.time(), DATE_TIME_PARSER);

    List<Weather> weathers = weatherRepository
            .findWeathersWithForecastedAt(lr.getX(), lr.getY(), forecastedAt, from, toExclusive);

    Location location = new Location(lr.getLatitude(), lr.getLongitude(), lr.getX(), lr.getY(), lr.getLocationNames());

    if (!hasFiveDistinctDays(weathers, startDate)) {
      weathers = computeDailyForecastMap(location)
              .map(byDate -> upsertForecastsForProfile(location, byDate))  // <- 반환값 사용
              .orElseGet(List::of);
    }

    return weathers.stream()
            .sorted(Comparator.comparing(Weather::getForecastAt))
            .map(WeatherDto::from)
            .toList();

  }

  // KST 기준 오늘~+4일까지(총 5일) 날짜별로 데이터가 있는지 확인
  private boolean hasFiveDistinctDays(List<Weather> weathers, LocalDate startDateKst) {
    var days = weathers.stream()
            .map(w -> w.getForecastAt().toLocalDate()) // DB가 KST 기준이라면 OK (UTC라면 변환 필요)
            .filter(d -> !d.isBefore(startDateKst) && !d.isAfter(startDateKst.plusDays(4)))
            .collect(Collectors.toSet());
    return days.size() == 5;
  }

  // latitude(위도), longitude(경도)
  @Override
  public WeatherDto getWeatherById(UUID weatherId) {
    Weather weather = weatherRepository.findById(weatherId)
            .orElseThrow(WeatherApiException::new);

    return WeatherDto.from(weather);
  }

  @Override
  @CircuitBreaker(name = "locationRestTemplate", fallbackMethod = "fallbackLocation")
  public LocationResponse getLocationForUser(LocationRequest locationRequest) {
    String uri = UriComponentsBuilder
            .fromUriString(apisProps.getApis().get("kakao-map").getBaseUrl())
            .queryParam("x", locationRequest.longitude())
            .queryParam("y", locationRequest.latitude())
            .build()
            .toUriString();

    KakaoGeoResponse responseJson = locationRestTemplate.getForObject(uri, KakaoGeoResponse.class);

    int[] grid = GridConverter.latLonToGrid(locationRequest.latitude(), locationRequest.longitude());

    if (responseJson.getDocuments().isEmpty()) {
      throw new ExternalApiException();
    }

    var doc = responseJson.getDocuments().get(0);

    List<String> locationNames = Stream.of(
                    doc.getRegion1depthName(),
                    doc.getRegion2depthName(),
                    doc.getRegion3depthName(),
                    doc.getRegion4depthName()
            )
            .filter(s -> s != null && !s.isBlank())
            .toList();

    return LocationResponse.of(
            locationRequest.latitude(),
            locationRequest.longitude(),
            grid[0],
            grid[1],
            locationNames
    );
  }

  @Override
  @Transactional
  public void collectWeatherData() {
    List<Profile> profiles = profileRepository.findAll();

    for (Profile profile : profiles) {
      Location location = profile.getLocation();
      if(location == null || location.getLatitude() == null || location.getLongitude() == null || location.getX() == null || location.getY() == null) {
        continue;
      }

      computeDailyForecastMap(profile.getLocation()).ifPresent(byDate -> {
        upsertForecastsForProfile(profile.getLocation(), byDate);
      });
    }
  }

  private List<Weather> upsertForecastsForProfile(
          Location loc,
          Map<LocalDate, Map<Category, ForecastItem>> byDate
  ) {
    List<Weather> resultWeather = new ArrayList<>();

    LocalDate firstDate = byDate.keySet().stream()
            .min(LocalDate::compareTo)
            .orElseThrow();

    Optional<Weather> prevDbOfFirst = weatherRepository
            .findxByyAndForecastDate(
                    loc.getX(), loc.getY(),
                    firstDate.minusDays(1)
            );

    var base = WeatherApiParams.currentBase(); // 스냅샷 1회 캡처

    for (var entry : byDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .toList()) {

      Optional<Weather> prevDb = entry.getKey().equals(firstDate) ? prevDbOfFirst : Optional.empty();

      Weather weather = buildWeatherFromForecast(loc, entry, byDate, prevDb, base);


      Optional<Weather> prevWeather= weatherRepository.findByLocation_XAndLocation_YAndForecastAt(weather.getLocation().getX(), weather.getLocation().getY(), weather.getForecastAt());

      // 존재하면 update, 없으면 insert
      if(prevWeather.isPresent()) {
        Weather prev = prevWeather.get();

        LocalDate todayKst = LocalDate.now(KST);
        if (weather.getForecastAt().toLocalDate().equals(todayKst)) {
          detectAndPublishAlerts(prev, weather);
        }

        prev.updateFrom(weather.getForecastedAt(), weather.getForecastAt(), weather.getSkyStatus(), weather.getLocation(), weather.getPrecipitation(), weather.getHumidity(), weather.getTemperature(), weather.getWindSpeed());
        resultWeather.add(prev);

      }else{
        resultWeather.add(weatherRepository.save(weather));
      }
    }

    return resultWeather;
  }

  /**
   * 위치 유효성 검사 → 외부 호출 → 일자별 예보 맵 생성까지 한 번에 처리
   */
  private Optional<Map<LocalDate, Map<Category, ForecastItem>>> computeDailyForecastMap(Location loc) {
    List<ForecastItem> items = weatherExternal.getWeather(loc.getX(), loc.getY());
    if (items == null || items.isEmpty()) return Optional.empty();

    Map<LocalDate, String> earliestTimePerDate = getLocalDateStringMap(items);
    Map<LocalDate, Map<Category, ForecastItem>> weatherListDateMap =
            getWeatherListDateMap(items, earliestTimePerDate);

    return Optional.of(weatherListDateMap);
  }

  /**
   * 한 날짜(entry)와 이전 날짜 데이터를 합쳐 Weather 엔티티 생성
   */
  private Weather buildWeatherFromForecast(
          Location loc,
          Map.Entry<LocalDate, Map<Category, ForecastItem>> currentEntry,
          Map<LocalDate, Map<Category, ForecastItem>> byDate,
          Optional<Weather> prevDb,
          WeatherApiParams.Base base
  ) {
    // 예보 대상 일자 (YYYYMMdd)
    String fcstDate = currentEntry.getValue().get(SKY).getFcstDate();

    WeatherLocation location = new WeatherLocation(
            loc.getLatitude(),
            loc.getLongitude(),
            loc.getX(),
            loc.getY(),
            loc.getLocationNames()
    );

    // forecastAt: 해당 날짜 00:00 (예보가 적용되는 날의 자정)
    LocalDateTime forecastAt = LocalDateTime.parse(fcstDate + "0000", DATE_TIME_PARSER);
    // forecastedAt: 발표본 시각 (base_date + base_time) ← 세트로!
    LocalDateTime forecastedAt = LocalDateTime.parse(base.date() + base.time(), DATE_TIME_PARSER);

    LocalDate prevDate = currentEntry.getKey().minusDays(1);
    Map<Category, ForecastItem> prevApi = byDate.get(prevDate); // 있을 수도, 없을 수도

    // 하늘상태
    int skyCode = Integer.parseInt(currentEntry.getValue().get(SKY).getFcstValue());
    SkyStatus sky = SkyStatus.fromSkyStatusCode(skyCode);

    // 온도
    double cur = Double.parseDouble(currentEntry.getValue().get(TMP).getFcstValue());
    double min = Double.parseDouble(currentEntry.getValue().get(TMN).getFcstValue());
    double max = Double.parseDouble(currentEntry.getValue().get(TMX).getFcstValue());

    double prevTmp = prevDb
            .map(w -> w.getTemperature().getTemperatureCurrent()) // 프로젝트 게터명에 맞춤
            .orElseGet(() -> prevApi != null && prevApi.get(TMP) != null
                    ? Double.parseDouble(prevApi.get(TMP).getFcstValue())
                    : cur);
    Temperature temperature = new Temperature(cur, prevTmp, min, max);

    // 습도
    double curHum = Double.parseDouble(currentEntry.getValue().get(REH).getFcstValue());
    double prevHum = prevDb
            .map(w -> w.getHumidity().getHumidityCurrent()) // 프로젝트 게터명에 맞춤
            .orElseGet(() -> prevApi != null && prevApi.get(REH) != null
                    ? Double.parseDouble(prevApi.get(REH).getFcstValue())
                    : curHum);
    Humidity humidity = new Humidity(curHum, prevHum);

    // 강수
    Precipitation precipitation = getPrecipitation(currentEntry);

    // 바람
    double windSpeedMs = Double.parseDouble(currentEntry.getValue().get(WSD).getFcstValue());
    WindSpeed windSpeed = new WindSpeed(windSpeedMs, WindSpeedLevel.fromSpeed(windSpeedMs));

    log.info("기온: {}", temperature);
    log.info("습도: {}", humidity);
    log.info("강수 {}", precipitation);
    log.info("바람 {}", windSpeed);

    return Weather.of(forecastedAt, forecastAt, sky, location, precipitation, humidity, temperature, windSpeed);
  }

  private Precipitation getPrecipitation(Map.Entry<LocalDate, Map<Category, ForecastItem>> entry) {
    Map<Category, ForecastItem> m = entry.getValue();

    // 안전 파싱
    String ptyStr = m.getOrDefault(PTY, new ForecastItem()).getFcstValue();
    String popStr = m.getOrDefault(POP, new ForecastItem()).getFcstValue();
    String pcpRaw = m.getOrDefault(PCP, new ForecastItem()).getFcstValue();

    int ptyCode = safeParseInt(ptyStr, 0);
    PrecipitationType type = PrecipitationType.fromCode(ptyCode);

    // POP은 0~1 스케일로 통일
    double prob01 = parsePop01(popStr); // 0~1

    // PCP(1시간 강수량) 파싱
    double amount = getPrecipitationAmount(pcpRaw); // mm

    // 보정 규칙
    if (amount > 0 && prob01 == 0.0) prob01 = 1.0;                 // 강수량>0인데 확률 0 → 100%
    if (amount > 0 && type == PrecipitationType.NONE) type = PrecipitationType.RAIN;  // PTY 미스 보정
    if (amount == 0 && type != PrecipitationType.NONE) amount = 0.1; // 의미상 강수 있음을 표시하고 싶다면

    prob01 = clamp01(prob01);

    // 생성자 순서: (type, amount, probability[0~1])
    return new Precipitation(type, amount, prob01);
  }

  private static int safeParseInt(String s, int def) {
    try { return (s == null || s.isBlank()) ? def : Integer.parseInt(s.trim()); }
    catch (NumberFormatException e) { return def; }
  }

  private static double parsePop01(String raw) {
    if (raw == null || raw.isBlank()) return 0.0;
    double v;
    try { v = Double.parseDouble(raw.trim()); } catch (NumberFormatException e) { return 0.0; }
    // 입력이 0~100으로 온다고 가정 → 0~1로 정규화
    return clamp01(v / 100.0);
  }

  private static double clamp01(double v) {
    return Math.max(0.0, Math.min(1.0, v));
  }

  private double getPrecipitationAmount(String raw) {
    if (raw == null) return 0.0;
    raw = raw.trim();
    // 기상청 응답: "강수없음", "1.0mm 미만", "-", "" 등
    if (raw.isEmpty() || raw.contains("없음") || raw.equals("-")) return 0.0;

    boolean isBelow = raw.contains("미만");     // "1.0mm 미만"
    boolean isOver  = raw.contains("이상");     // "150.0mm 이상" 같은 케이스 대비

    String num = raw.replaceAll("[^0-9.]", ""); // 숫자/소수점만 추출
    if (num.isEmpty()) return 0.0;

    double v;
    try { v = Double.parseDouble(num); }
    catch (NumberFormatException e) {
      log.warn("PCP parse failed: '{}'", raw, e);
      return 0.0;
    }

    if (isBelow)  return 0.0;   // 정책: '미만'은 0.0으로 처리 (원하면 0.5 등으로 조정)
    if (isOver)   return v;     // '이상'은 하한선으로 그대로 사용

    return v; // 단위:mm
  }

  private LinkedHashMap<LocalDate, Map<Category, ForecastItem>> getWeatherListDateMap(List<ForecastItem> items, Map<LocalDate, String> earliestTimePerDate) {
    // 날짜별 전체 ForecastItem 묶음
    LinkedHashMap<LocalDate, List<ForecastItem>> itemsByDate = items.stream()
            .collect(Collectors.groupingBy(
                    fi -> LocalDate.parse(fi.getFcstDate(), DATE_PARSER),
                    LinkedHashMap::new,
                    Collectors.toList()
            ));

    // 날짜별 TMP 통계
    Map<LocalDate, DoubleSummaryStatistics> tmpStatsByDate = itemsByDate.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream()
                            .filter(fi -> TMP.name().equals(fi.getCategory()))
                            .mapToDouble(fi -> Double.parseDouble(fi.getFcstValue()))
                            .summaryStatistics(),
                    (a, b) -> a,
                    LinkedHashMap::new
            ));

    // 가장 빠른 시각 항목들 + TMN/TMX 추가
    return items.stream()
            .filter(item -> {
              LocalDate d = LocalDate.parse(item.getFcstDate(), DATE_PARSER);
              return item.getFcstTime().equals(earliestTimePerDate.get(d));
            })
            .collect(Collectors.groupingBy(
                    item -> LocalDate.parse(item.getFcstDate(), DATE_PARSER),
                    LinkedHashMap::new,
                    Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                              // 1) 가장 이른 시각의 기본 카테고리들
                              EnumMap<Category, ForecastItem> m = new EnumMap<>(Category.class);
                              list.forEach(fi -> m.put(Category.valueOf(fi.getCategory()), fi));

                              LocalDate date = LocalDate.parse(list.get(0).getFcstDate(), DATE_PARSER);
                              DoubleSummaryStatistics stats = tmpStatsByDate.get(date);

                              // 2) 하루 전체 TMP 중 최소값 -> TMN
                              itemsByDate.get(date).stream()
                                      .filter(fi -> TMP.name().equals(fi.getCategory()))
                                      .filter(fi -> Double.parseDouble(fi.getFcstValue()) == stats.getMin())
                                      .findFirst()
                                      .ifPresent(fiMin -> m.put(Category.TMN, fiMin));

                              // 3) 하루 전체 TMP 중 최대값 -> TMX
                              itemsByDate.get(date).stream()
                                      .filter(fi -> TMP.name().equals(fi.getCategory()))
                                      .filter(fi -> Double.parseDouble(fi.getFcstValue()) == stats.getMax())
                                      .findFirst()
                                      .ifPresent(fiMax -> m.put(Category.TMX, fiMax));

                              return m;
                            }
                    )
            ));
  }

  private Map<LocalDate, String> getLocalDateStringMap(List<ForecastItem> items) {
    return items.stream()
            .collect(Collectors.groupingBy(
                    item -> LocalDate.parse(item.getFcstDate(), DATE_PARSER),
                    // 각 아이템의 fcstTime을 뽑아서, MIN(Time) 구하기
                    Collectors.mapping(ForecastItem::getFcstTime,
                            Collectors.minBy(String::compareTo))
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().orElseThrow(),
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
  }


  private void detectAndPublishAlerts(Weather prev, Weather next) {
    List<WeatherAlert> alerts = temperatureDeltaRule.evaluate(prev, next);
    if (alerts.isEmpty()) return;

    for (WeatherAlert a : alerts) {
      String key = "wa:%s:%d:%d:%s>%s".formatted(a.type(), a.x(), a.y(), prev.getForecastedAt(), a.forecastedAt());
      if (onceDeduper.acquireOnce(key, Duration.ofHours(3))) {
        eventPublisher.publishEvent(a);
      }
    }
  }

  public LocationResponse fallbackLocation(LocationRequest locationRequest, Throwable ex) {
    if (ex instanceof CallNotPermittedException) {
      log.warn("CircuitBreaker OPEN 상태: 호출 차단됨 호출 정보={}, 예외={}", locationRequest, ex.toString());
    } else {
      log.warn("외부 API 호출 실패: 요청 정보={}, 예외={}", locationRequest, ex.toString());
    }
    throw new ExternalApiException();
  }
}