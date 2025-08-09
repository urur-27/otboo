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
    BASEDATE("base_date", WeatherApiParams::defaultBaseDate), // 기본: 오늘
    BASETIME("base_time", () -> "0500"),
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

    public String getValueDaysAgo(int daysAgo) {
        if (this != BASEDATE) return getValue(); // BASEDATE 외에는 기존 값 유지
        if (daysAgo < 0) daysAgo = 0;
        return DATE_FMT.format(ZonedDateTime.now(SEOUL).minusDays(daysAgo).toLocalDate());
    }

    private static String defaultBaseDate() {
        return DATE_FMT.format(ZonedDateTime.now(SEOUL).toLocalDate()); // 오늘
    }
}
