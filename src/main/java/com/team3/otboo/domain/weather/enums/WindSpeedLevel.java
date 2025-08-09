package com.team3.otboo.domain.weather.enums;

public enum WindSpeedLevel {
    WEAK, MODERATE, STRONG;

    public static WindSpeedLevel fromSpeed(double speed) {
        if (speed < 5.0) {            // 5m/s 미만
            return WEAK;
        } else if (speed < 10.0) {    // 5~10m/s
            return MODERATE;
        } else {                      // 10m/s 이상
            return STRONG;
        }
    }
}
