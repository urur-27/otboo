package com.team3.otboo.props.external;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "external")
public class ExternalApisProperties {

    private Map<String, ApiProperties> apis = new HashMap<>();

    @Data
    public static class ApiProperties {
        private String baseUrl;
        private int connectTimeout;
        private int readTimeout;
        private String apiKey;

    }
}
