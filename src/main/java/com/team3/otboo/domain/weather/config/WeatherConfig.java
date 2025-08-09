package com.team3.otboo.domain.weather.config;

import com.team3.otboo.props.external.ExternalApisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@RequiredArgsConstructor
public class WeatherConfig {

    private final ExternalApisProperties apisProps;

    @Bean("locationRestTemplate")
    public RestTemplate locationRestTemplate() {
        ExternalApisProperties.ApiProperties props = apisProps.getApis().get("kakao-map");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeout());
        factory.setReadTimeout(props.getReadTimeout());

        RestTemplate rt = new RestTemplate(factory);
        rt.setUriTemplateHandler(new DefaultUriBuilderFactory(props.getBaseUrl()));

        rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "KakaoAK " + props.getApiKey());
            return execution.execute(request, body);
        });


        return rt;
    }

    @Bean("weatherRestTemplate")
    public RestTemplate weatherRestTemplate() {
        ExternalApisProperties.ApiProperties props = apisProps.getApis().get("weather");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeout());
        factory.setReadTimeout(props.getReadTimeout());

        RestTemplate restTemplate = new RestTemplate(factory);

        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(props.getBaseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(uriFactory);

        return restTemplate;
    }

}
