package com.team3.otboo.domain.weather.service;

import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.event.WeatherAlert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemperatureDeltaRule {

    @Value("${weather.alerts.rain.prob-threshold:0.6}")
    double rainProbThreshold;

    @Value("${weather.alerts.rain.prob-spike:0.3}")
    double rainProbSpike;

    @Value("${weather.alerts.temp.spike:3}")
    double tempSpike;

    public List<WeatherAlert> evaluate(Weather prev, Weather next) {
        List<WeatherAlert> out = new ArrayList<>();

        // 1) RAIN_START
        var p0 = prev.getPrecipitation();
        var p1 = next.getPrecipitation();

        if (p0.getType() == PrecipitationType.NONE
                && p1.getType() != PrecipitationType.NONE
                && p1.getProbability() >= rainProbThreshold) {
            out.add(WeatherAlert.rainStart(next, p1.getProbability()));
        }

        // 2) RAIN_PROB_SPIKE
        double probDelta = p1.getProbability() - p0.getProbability();
        if (probDelta >= rainProbSpike && p1.getProbability() >= rainProbThreshold) {
            out.add(WeatherAlert.rainProbSpike(next, p1.getProbability(), probDelta));
        }

        // 3) TEMP_SPIKE (상승/하강 모두, 부호로 구분)
        var t0 = prev.getTemperature();
        var t1 = next.getTemperature();

        double test1 = t0.getTemperatureCurrent();
        double test2 = t1.getTemperatureCurrent();

        double dCur = test2 - test1;
        if (Math.abs(dCur) >= tempSpike) {
            out.add(WeatherAlert.tempSpike(next, dCur));
        }

        return out;
    }
}
