package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.FixtureLoader;
import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.infrastructure.llm.LlmGateway;
import com.team3.otboo.domain.clothing.infrastructure.llm.LlmRoute;
import com.team3.otboo.domain.clothing.infrastructure.llm.LlmRoutingPolicy;
import com.team3.otboo.domain.clothing.mapper.ClothingDtoMerger;
import com.team3.otboo.domain.clothing.analyzers.HtmlExtractor;
import com.team3.otboo.domain.clothing.mapper.AttributeMapper;
import com.team3.otboo.domain.clothing.service.AttributeReadService.AttributeDefSnap;
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
    private final FixtureLoader fixtureLoader; // 추가

    private final com.team3.otboo.domain.clothing.service.AttributeReadService attributeReadService;


    @Override
    public Mono<ClothesDto> extractFromUrlReactive(String url) {

        Mono<HtmlExtractionResult> htmlMono;

        if (url.startsWith("fixture:")) {
            String name = url.substring("fixture:".length());
            htmlMono = Mono.fromCallable(() -> {
                        String html = fixtureLoader.loadHtml(name);     // blocking I/O
                        return htmlExtractor.extractFromHtml(html);     // in-memory parse
                    })
                    .subscribeOn(Schedulers.boundedElastic());
        } else {
            htmlMono = Mono.fromCallable(() -> htmlExtractor.extract(url)) // blocking fetch
                    .subscribeOn(Schedulers.boundedElastic());
        }

        Mono<AttributeReadService.AttributeSnapshot> snapMono =
                Mono.fromCallable(attributeReadService::loadAllForExtraction) // blocking DB
                        .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(htmlMono, snapMono)
                .flatMap(tuple -> {
                    var html = tuple.getT1();
                    var snap = tuple.getT2();

                    List<String> defNames = snap.defs().stream().map(AttributeDefSnap::name).toList();
                    Map<String, List<String>> opts = snap.optionsByDef();

                    LlmRoute route = routingPolicy.choose(currentTenantOrDefault());
                    String idem = UUID.randomUUID().toString();

                    return llmGateway.analyzeVision(
                                    html.imageUrl(), html.title(), html.description(),
                                    defNames, opts, "ko-KR",
                                    route.provider(), route.model(), idem)
                            .timeout(Duration.ofSeconds(35))
                            .retryWhen(Retry.backoff(1, Duration.ofMillis(250)).filter(this::isRetryable))
                            .map(vision -> {
                                var mapped = attributeMapper.mapFromVision(vision, snap.defs());
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
