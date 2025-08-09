package com.team3.otboo.domain.weather.service;

import com.team3.otboo.converter.GridConverter;
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
import com.team3.otboo.external.WeatherExternal;
import com.team3.otboo.global.exception.weather.ExternalApiException;
import com.team3.otboo.props.external.ExternalApisProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.BASIC_ISO_DATE;
  private static final DateTimeFormatter DATE_TIME_PARSER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  // latitude(위도), longitude(경도)
  @Override
  public WeatherDto getWeatherById(UUID weatherId) {
    return null;
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
      computeDailyForecastMap(profile.getLocation()).ifPresent(byDate -> {

        LocalDate firstDate = byDate.keySet().stream()
                .min(LocalDate::compareTo)
                .orElseThrow();

        Optional<Weather> prevDbOfFirst = weatherRepository
                .findLatestByLocationAndForecastDate(
                        profile.getLocation().getLatitude(),
                        profile.getLocation().getLongitude(),
                        firstDate.minusDays(1)
                );

        for (Map.Entry<LocalDate, Map<Category, ForecastItem>> entry : byDate.entrySet()) {
          Optional<Weather> prevDb = entry.getKey().equals(firstDate) ? prevDbOfFirst : Optional.empty();

          Weather weather = buildWeatherFromForecast(profile, entry, byDate, prevDb);
          upsertWeather(weather);
        }
      });
    }
  }

  /**
   * 위치 유효성 검사 → 외부 호출 → 일자별 예보 맵 생성까지 한 번에 처리
   */
  private Optional<Map<LocalDate, Map<Category, ForecastItem>>> computeDailyForecastMap(Location loc) {
    if (loc == null
            || loc.getLatitude() == null || loc.getLongitude() == null
            || loc.getX() == null || loc.getY() == null) {
      return Optional.empty();
    }

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
          Profile profile,
          Map.Entry<LocalDate, Map<Category, ForecastItem>> currentEntry,
          Map<LocalDate, Map<Category, ForecastItem>> byDate,
          Optional<Weather> prevDb // ✅ 추가
  ) {
    String baseDate = currentEntry.getValue().get(SKY).getFcstDate();
    String baseTime = WeatherApiParams.BASETIME.getValue();

    WeatherLocation location = new WeatherLocation(
            profile.getLocation().getLatitude(),
            profile.getLocation().getLongitude(),
            profile.getLocation().getX(),
            profile.getLocation().getY(),
            profile.getLocation().getLocationNames()
    );

    // forecastAt: 해당 날짜 00:00
    LocalDateTime forecastAt = LocalDateTime.parse(baseDate.concat("0000"), DATE_TIME_PARSER);
    // forecastedAt: API 기준 발표시각(baseTime)
    LocalDateTime forecastedAt = LocalDateTime.parse(baseDate.concat(baseTime), DATE_TIME_PARSER);

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
            .map(w -> w.getTemperature().getTemperatureCurrent())
            .orElseGet(() -> prevApi != null
                    ? Double.parseDouble(prevApi.get(TMP).getFcstValue())
                    : cur);
    Temperature temperature = new Temperature(cur, prevTmp, min, max);

    // 습도
    double curHum = Double.parseDouble(currentEntry.getValue().get(REH).getFcstValue());
    double prevHum = prevDb
            .map(w -> w.getHumidity().getHumidityCurrent())
            .orElseGet(() -> prevApi != null
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

  /**
   * 존재하면 update, 없으면 insert
   */
  private void upsertWeather(Weather weather) {
    Optional<Weather> existing = weatherRepository
            .findByLocationLatitudeAndLocationLongitudeAndForecastAt(
                    weather.getLocation().getLatitude(),
                    weather.getLocation().getLongitude(),
                    weather.getForecastAt());

    existing.ifPresentOrElse(
            e -> e.updateFrom(
                    weather.getForecastedAt(),
                    weather.getForecastAt(),
                    weather.getSkyStatus(),
                    weather.getLocation(),
                    weather.getPrecipitation(),
                    weather.getHumidity(),
                    weather.getTemperature(),
                    weather.getWindSpeed()),
            () -> weatherRepository.save(weather)
    );
  }

  private Precipitation getPrecipitation(Map.Entry<LocalDate, Map<Category, ForecastItem>> currentForecastItem) {
    int precipitationCode = Integer.parseInt(currentForecastItem.getValue().get(PTY).getFcstValue());
    PrecipitationType precipitationType = PrecipitationType.fromCode(precipitationCode);

    // 2) 강수 확률 (%)
    double precipitationProbability = Double.parseDouble(currentForecastItem.getValue().get(POP).getFcstValue());

    // 3) 강수량 (mm) — “강수없음” 같은 값 처리
    String pcpRaw = currentForecastItem.getValue().get(PCP).getFcstValue();
    double precipitationAmount = getPrecipitationAmount(pcpRaw);
    return new Precipitation(precipitationType, precipitationProbability, precipitationAmount);
  }

  private Double getPrecipitationAmount(String pcpRaw) {
    if (pcpRaw == null || "강수없음".equals(pcpRaw)) {
      return 0.0;
    }
    return Double.parseDouble(pcpRaw.replace("mm", ""));
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

  private Optional<Weather> getCombinedForecast(
          Location location,
          Map<LocalDate, ForecastItem> apiForecastMap
  ) {
    LocalDate firstDate = apiForecastMap.keySet().stream()
            .min(LocalDate::compareTo)
            .orElseThrow(() -> new IllegalArgumentException("Forecast map is empty"));

    LocalDate prevDate = firstDate.minusDays(1);

    return weatherRepository
            .findLatestByLocationAndForecastDate(
                    location.getLatitude(),
                    location.getLongitude(),
                    prevDate
            );
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
