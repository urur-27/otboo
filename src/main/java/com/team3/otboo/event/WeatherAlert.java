package com.team3.otboo.event;

import com.team3.otboo.domain.weather.entity.Weather;

import java.time.LocalDateTime;

public record WeatherAlert(
        Type type,
        int x, int y,
        LocalDateTime forecastAt,
        LocalDateTime forecastedAt,
        // 케이스별 선택값
        Double rainProb,        // 현재 강수확률(0~1)
        Double rainProbDelta,   // 확률 변화량(+면 상승)
        Double temperatureDelta // 현재기온 변화량(+면 상승, -면 하강)
) {
    public enum Type { RAIN_START, RAIN_PROB_SPIKE, TEMP_SPIKE }

    // 정적 팩토리
    public static WeatherAlert rainStart(Weather w, double prob) {
        return new WeatherAlert(Type.RAIN_START,
                w.getLocation().getX(), w.getLocation().getY(),
                w.getForecastAt(), w.getForecastedAt(),
                prob, null, null);
    }
    public static WeatherAlert rainProbSpike(Weather w, double prob, double delta) {
        return new WeatherAlert(Type.RAIN_PROB_SPIKE,
                w.getLocation().getX(), w.getLocation().getY(),
                w.getForecastAt(), w.getForecastedAt(),
                prob, delta, null);
    }
    public static WeatherAlert tempSpike(Weather w, double delta) {
        return new WeatherAlert(Type.TEMP_SPIKE,
                w.getLocation().getX(), w.getLocation().getY(),
                w.getForecastAt(), w.getForecastedAt(),
                null, null, delta);
    }
}