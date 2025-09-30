package com.team3.otboo.domain.clothing.infrastructure.llm;

import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

//@Component
//public class LlmGateway {
//    private final WebClient llmClient;
//
//    public LlmGateway(WebClient llmClient) {
//        this.llmClient = llmClient;
//    }
//
//    public Mono<VisionAnalysisResult> analyzeVision(
//            String imageUrl,
//            String title,
//            String description,
//            List<String> definitionNames,
//            Map<String, List<String>> optionsByDef,
//            String locale,
//            @Nullable String providerOverride,
//            @Nullable String modelOverride,
//            @Nullable String idempotencyKey
//    ) {
//        var req = Map.of(
//                "imageUrl", imageUrl,
//                "title", title,
//                "description", description,
//                "definitionNames", definitionNames,
//                "optionsByDef", optionsByDef,
//                "locale", locale
//        );
//
//        WebClient.RequestBodySpec spec = llmClient.post().uri("/v1/vision/analyze");
//        if (providerOverride != null && !providerOverride.isBlank()) {
//            spec = spec.header("X-Provider", providerOverride);
//        }
//        if (modelOverride != null && !modelOverride.isBlank()) {
//            spec = spec.header("X-Model-Name", modelOverride);
//        }
//        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
//            spec = spec.header("X-Idempotency-Key", idempotencyKey);
//        }
//
//        return spec.bodyValue(req)
//                .retrieve()
//                .bodyToMono(VisionAnalysisResult.class);
//    }
//}


@Component
//@Profile("s2")  // s2 실행시만 이 구현 사용
public class LlmGateway {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${llm.base-url:http://localhost:8000}")
    private String baseUrl;

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
        return Mono.fromCallable(() -> {
            // 요청 바디 구성
            Map<String, Object> req = Map.of(
                    "imageUrl", imageUrl,
                    "title", title,
                    "description", description,
                    "definitionNames", definitionNames,
                    "optionsByDef", optionsByDef,
                    "locale", locale,
                    "model", modelOverride != null ? modelOverride : "gpt-4o-mini"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (idempotencyKey != null) {
                headers.set("X-Idempotency-Key", idempotencyKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);

            // 동기 호출
            ResponseEntity<VisionAnalysisResult> resp = restTemplate.exchange(
                    baseUrl + "/v1/vision/analyze",
                    HttpMethod.POST,
                    entity,
                    VisionAnalysisResult.class
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new IllegalStateException("Python LLM 호출 실패: " + resp.getStatusCode());
            }
            return resp.getBody();
        }).subscribeOn(Schedulers.boundedElastic()); // 블로킹 I/O는 별도 쓰레드풀에서 실행
    }
}
