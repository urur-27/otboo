package com.team3.otboo.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter @Setter
@Validated
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    @NotBlank
    private String apiKey;

    @NotBlank
    private String model;

    @Min(0) @Max(2)
    private double temperature = 0.2;

    @Min(1) @Max(4096)
    private int maxTokens = 600;
}