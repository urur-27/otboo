package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.infrastructure.llm.LlmGateway;
import com.team3.otboo.domain.clothing.infrastructure.llm.LlmRoute;
import com.team3.otboo.domain.clothing.infrastructure.llm.LlmRoutingPolicy;
import com.team3.otboo.domain.clothing.mapper.ClothingDtoMerger;
import com.team3.otboo.domain.clothing.analyzers.HtmlExtractor;
import com.team3.otboo.domain.clothing.mapper.AttributeMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothingExtractionServiceImpl implements ClothingExtractionService {

    private final HtmlExtractor htmlExtractor;
    private final ClothingDtoMerger clothingDtoMerger;
    private final AttributeMapper attributeMapper;

    private final LlmGateway llmGateway;
    private final LlmRoutingPolicy routingPolicy;

    private final com.team3.otboo.domain.clothing.service.AttributeReadService attributeReadService;

    @Override
    public Mono<ClothesDto> extractFromUrlReactive(String url) {
        // html 정보 추출
        Mono<HtmlExtractionResult> htmlMono =
                Mono.fromCallable(() -> htmlExtractor.extract(url)) // blocking
                        .subscribeOn(Schedulers.boundedElastic());

        // DB에서 속성/값 가져오기
        Mono<com.team3.otboo.domain.clothing.service.AttributeReadService.AttributeSnapshot> snapMono =
                Mono.fromCallable(attributeReadService::loadAllForExtraction) // blocking
                        .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(htmlMono, snapMono)
                .flatMap(tuple -> {
                    var html = tuple.getT1();
                    var snap = tuple.getT2();

                    // LLM 입력 준비
                    List<String> defNames = snap.defs().stream()
                            .map(com.team3.otboo.domain.clothing.service.AttributeReadService.AttributeDefSnap::name)
                            .toList();
                    Map<String, List<String>> opts = snap.optionsByDef();

                    LlmRoute route = routingPolicy.choose(currentTenantOrDefault()); // 사용할 LLM 서비스, 모델을 고르는 기능. 구현 x
                    String idem = UUID.randomUUID().toString(); // 멱등성 처리를 위한 키 생성

                    return llmGateway.analyzeVision(
                                    html.imageUrl(),
                                    html.title(),
                                    html.description(),
                                    defNames,
                                    opts,
                                    "ko-KR",
                                    route.provider(),
                                    route.model(),
                                    idem
                            )
                            .timeout(Duration.ofSeconds(30))
                            .retryWhen(Retry.backoff(1, Duration.ofMillis(250))
                                    .filter(this::isRetryable))
                            .map((VisionAnalysisResult vision) -> {
                                var mapped = attributeMapper.mapFromVision(vision, snap.defs()); // 스냅샷 사용(DB 재조회 X)
                                return clothingDtoMerger.merge(html, vision, mapped);
                            });
                });
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof WebClientResponseException w) {
            int s = w.getRawStatusCode();
            return s == 429 || (s >= 500 && s < 600);
        }
        return e instanceof java.net.ConnectException
                || e instanceof java.net.SocketTimeoutException
                || e instanceof reactor.netty.http.client.PrematureCloseException
                || e instanceof java.io.IOException;
    }

    private String currentTenantOrDefault() { return "default"; }
}
