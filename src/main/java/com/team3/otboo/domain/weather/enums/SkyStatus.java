package com.team3.otboo.domain.weather.enums;

public enum SkyStatus {
    CLEAR, MOSTLY_CLOUDY, CLOUDY;

    public static SkyStatus fromSkyStatusCode(int code) {
        switch (code) {
            case 3: return MOSTLY_CLOUDY;
            case 4: return CLOUDY;
            case 1:
            default:
                return CLEAR;
        }
    }
}
