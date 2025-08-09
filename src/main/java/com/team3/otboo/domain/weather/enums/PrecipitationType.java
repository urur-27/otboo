package com.team3.otboo.domain.weather.enums;

public enum PrecipitationType {
    NONE,       // 0: 없음
    RAIN,       // 1: 비
    RAIN_SNOW,  // 2: 비/눈
    SNOW,       // 3: 눈
    SHOWER;     // 4: 소나기

    public static PrecipitationType fromCode(int code) {
        switch (code) {
            case 1: return RAIN;
            case 2: return RAIN_SNOW;
            case 3: return SNOW;
            case 4: return SHOWER;
            case 0:
            default:
                return NONE;
        }
    }
}
