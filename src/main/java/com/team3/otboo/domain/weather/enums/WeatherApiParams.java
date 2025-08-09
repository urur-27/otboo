package com.team3.otboo.domain.weather.enums;

import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Getter
public enum WeatherApiParams {
    SERVICEKEY("serviceKey", () -> /* your service key here */ ""),
    PAGENO("pageNo", () -> "1"),
    NUMOFROWS("numOfRows", () -> "1500"),
    DATATYPE("dataType", () -> "JSON"),
    BASEDATE("base_date", WeatherApiParams::defaultBaseDate), // 🔁 동적
    BASETIME("base_time", WeatherApiParams::defaultBaseTime), // 🔁 동적
    NX("nx", () -> null),
    NY("ny", () -> null);

    private final String key;
    private final Supplier<String> valueSupplier;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    WeatherApiParams(String key, Supplier<String> valueSupplier) {
        this.key = key;
        this.valueSupplier = valueSupplier;
    }

    public String getValue() {
        return valueSupplier.get();
    }

    private static String defaultBaseDate() {
        Base b = latestBase();
        return b.date();
    }

    private static String defaultBaseTime() {
        Base b = latestBase();
        return b.time();
    }

    /** 현재 시각 기준으로 사용 가능한 가장 최근 발표본(＋10분 버퍼) */
    private static Base latestBase() {
        var now = ZonedDateTime.now(SEOUL).withSecond(0).withNano(0);

        int[] hours = {23, 20, 17, 14, 11, 8, 5, 2};

        for (int h : hours) {
            var base = now.withHour(h).withMinute(0);
            if (!now.isBefore(base.plusMinutes(10))) {
                return new Base(base.format(DATE_FMT), String.format("%02d00", h));
            }
        }

        var prev2300 = now.minusDays(1).withHour(23).withMinute(0);
        return new Base(prev2300.format(DATE_FMT), "2300");
    }

    public static record Base(String date, String time) {}
    public static Base currentBase() { return latestBase(); }
}
