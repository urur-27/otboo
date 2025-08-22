package com.team3.otboo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProps(String baseUrl, Integer timeoutMs, String defaultProvider, String defaultModel) {}