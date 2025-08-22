package com.team3.otboo.domain.clothing.infrastructure.llm;

import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class LlmGateway {
    private final WebClient llmClient;

    public LlmGateway(WebClient llmClient) {
        this.llmClient = llmClient;
    }

    public Mono<VisionAnalysisResult> analyzeVision(
            String imageUrl,
            String title,
            String description,
            List<String> definitionNames,
            Map<String, List<String>> optionsByDef,
            String locale,
            @Nullable String providerOverride,
            @Nullable String modelOverride,
            @Nullable String idempotencyKey
    ) {
        var req = Map.of(
                "imageUrl", imageUrl,
                "title", title,
                "description", description,
                "definitionNames", definitionNames,
                "optionsByDef", optionsByDef,
                "locale", locale
        );

        WebClient.RequestBodySpec spec = llmClient.post().uri("/v1/vision/analyze");
        if (providerOverride != null && !providerOverride.isBlank()) {
            spec = spec.header("X-Provider", providerOverride);
        }
        if (modelOverride != null && !modelOverride.isBlank()) {
            spec = spec.header("X-Model-Name", modelOverride);
        }
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            spec = spec.header("X-Idempotency-Key", idempotencyKey);
        }

        return spec.bodyValue(req)
                .retrieve()
                .bodyToMono(VisionAnalysisResult.class);
    }
}