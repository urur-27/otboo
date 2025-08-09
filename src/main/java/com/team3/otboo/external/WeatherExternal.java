package com.team3.otboo.external;

import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.ForecastItem;
import com.team3.otboo.domain.weather.dto.response.VilageFcstResponse;
import com.team3.otboo.domain.weather.enums.WeatherApiParams;
import com.team3.otboo.global.exception.weather.ExternalApiException;
import com.team3.otboo.props.external.ExternalApisProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherExternal {

    @Qualifier("weatherRestTemplate")
    private final RestTemplate weatherRestTemplate;

    private final ExternalApisProperties apisProps;

    @CircuitBreaker(name = "weatherRestTemplate", fallbackMethod = "fallbackWeather")
    public List<ForecastItem> getWeather(Integer x, Integer y) {
        ExternalApisProperties.ApiProperties props = apisProps.getApis().get("weather");

        String url = UriComponentsBuilder
                .fromHttpUrl(props.getBaseUrl())
                .queryParam(WeatherApiParams.SERVICEKEY.getKey(), props.getApiKey())
                .queryParam(WeatherApiParams.PAGENO.getKey(), WeatherApiParams.PAGENO.getValue())
                .queryParam(WeatherApiParams.NUMOFROWS.getKey(), WeatherApiParams.NUMOFROWS.getValue())
                .queryParam(WeatherApiParams.DATATYPE.getKey(), WeatherApiParams.DATATYPE.getValue())
                .queryParam(WeatherApiParams.BASEDATE.getKey(), WeatherApiParams.BASEDATE.getValue())
                .queryParam(WeatherApiParams.BASETIME.getKey(), WeatherApiParams.BASETIME.getValue())
                .queryParam(WeatherApiParams.NX.getKey(), x)
                .queryParam(WeatherApiParams.NY.getKey(), y)
                .build(true)
                .toUriString();

        VilageFcstResponse resp = weatherRestTemplate.getForObject(url, VilageFcstResponse.class);

        return resp.getResponse()
                .getBody()
                .getItems()
                .getItem();

    }

    private List<ForecastItem> fallbackWeather(Integer x, Integer y, Throwable ex) {
        throw new ExternalApiException();
    }

}
