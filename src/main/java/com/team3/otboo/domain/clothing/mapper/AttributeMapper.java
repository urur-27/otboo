package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.common.util.StringSimilarityUtils;
import com.team3.otboo.domain.clothing.service.AttributeReadService.AttributeDefSnap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeMapper {
    // 정의명 유사 매칭 임계치 (0~1)
    private static final double DEF_SIM_THRESH = 0.75;
    // 옵션값 유사 매칭 임계치
    private static final double OPT_SIM_THRESH = 0.65;

    /**
     * 변경점:
     * - 반환 리스트를 "defs 순회"로 빌드한다. (항목 누락 X)
     * - vision 값이 없거나 옵션 미일치면 value=null을 세팅하되, 항목은 남긴다.
     */
    public List<ClothesAttributeWithDefDto> mapFromVision(
            VisionAnalysisResult vision,
            List<AttributeDefSnap> defs
    ) {
        // LLM 결과를 맵으로 준비 (definitionName → value)
        Map<String, String> vmap = Optional.ofNullable(vision)
                .map(VisionAnalysisResult::attributes)
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        it -> safe(it.definitionName()),
                        it -> safe(it.value()),
                        (a, b) -> a
                ));

        // 결과는 반드시 defs 크기와 동일
        List<ClothesAttributeWithDefDto> result = new ArrayList<>(defs.size());

        for (AttributeDefSnap def : defs) {
            String defName = def.name();

            // LLM에서 같은 정의명(또는 유사명)으로 온 raw 값 찾기
            String raw = null;

            // 우선 정확히 맞는지
            raw = vmap.get(defName);

            if (isBlank(raw)) {
                // 정규화 키 매칭 (예: 공백/기호 차이)
                String normKey = StringSimilarityUtils.normalize(defName);
                // vmap의 키들을 순회하며 가장 유사한 정의명을 찾는다.
                String bestKey = vmap.keySet().stream()
                        .filter(k -> !isBlank(k))
                        .max(Comparator.comparingDouble(k ->
                                StringSimilarityUtils.similarity(
                                        StringSimilarityUtils.normalize(k), normKey)))
                        .orElse(null);

                if (bestKey != null) {
                    double sim = StringSimilarityUtils.similarity(
                            StringSimilarityUtils.normalize(bestKey), normKey);
                    if (sim >= DEF_SIM_THRESH) {
                        raw = vmap.get(bestKey);
                    }
                }
            }

            // 옵션 검증/정규화
            String finalValue = null;
            List<String> options = def.options();

            if (!isBlank(raw)) {
                if (options != null && !options.isEmpty()) {
                    // 옵션형: 가장 근접한 단일 옵션으로 스냅
                    finalValue = chooseSingleOption(raw, options);
                    if (finalValue == null) {
                        // 토큰 분해로도 못 고르면 전체 문자열 유사치 기반으로 1개 고르되
                        // 너무 엉뚱하면 null 유지 (옵션 오매핑 방지)
                        String best = StringSimilarityUtils.findBestBySimilarity(raw, options);
                        double sim = StringSimilarityUtils.similarity(
                                StringSimilarityUtils.normalize(raw),
                                StringSimilarityUtils.normalize(best));
                        finalValue = (sim >= OPT_SIM_THRESH) ? best : null;
                    }
                } else {
                    // 자유입력형: raw 그대로 (공백이면 null)
                    finalValue = raw.trim();
                    if (finalValue.isEmpty()) finalValue = null;
                }
            }

            // 항목은 항상 추가 (finalValue가 null이어도)
            result.add(new ClothesAttributeWithDefDto(
                    def.id(), defName, options, finalValue
            ));
        }

        return result;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private String chooseSingleOption(String valueRaw, List<String> options) {
        if (isBlank(valueRaw) || options == null || options.isEmpty()) return null;

        String normalized = valueRaw
                .replaceAll("\\d+\\s*%"," ")
                .replaceAll("[\\/|,&·+]+"," ")
                .replaceAll("\\s+"," ")
                .trim();

        String[] tokens = normalized.split(" ");
        Map<String, Double> score = new HashMap<>();

        for (String token : tokens) {
            if (isBlank(token)) continue;
            String best = StringSimilarityUtils.findBestBySimilarity(token, options);
            double sim  = StringSimilarityUtils.similarity(token, best);
            if (sim < OPT_SIM_THRESH) continue;
            score.merge(best, sim, Double::sum);
        }

        if (score.isEmpty()) {
            String best = StringSimilarityUtils.findBestBySimilarity(valueRaw, options);
            double sim  = StringSimilarityUtils.similarity(
                    StringSimilarityUtils.normalize(valueRaw),
                    StringSimilarityUtils.normalize(best));
            return (sim >= OPT_SIM_THRESH) ? best : null;
        }

        return score.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
