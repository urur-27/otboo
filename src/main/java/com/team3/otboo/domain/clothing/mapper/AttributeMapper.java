package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAttributeItem;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.common.util.StringSimilarityUtils;
import com.team3.otboo.domain.clothing.service.AttributeReadService.AttributeDefSnap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeMapper {

    private final AttributeRepository attributeRepository;

    // 정의명 유사 매칭 임계치 (0~1)
    private static final double DEF_SIM_THRESH = 0.75;
    // 옵션값 유사 매칭 임계치
    private static final double OPT_SIM_THRESH = 0.65;

    /**
     * Vision 모델이 반환한 속성 리스트를, 미리 스냅샷(defs)으로 전달받은 속성 정의(AttributeDefSnap)들과 매핑한다.
     *
     * 동작 흐름:
     *  1. Vision 결과(VisionAnalysisResult)의 각 attribute item(definitionName, value)을 순회한다.
     *  2. definitionName을 기준으로 AttributeDefSnap을 찾는다:
     *     - 정확 일치 → 정규화(normalize) 일치 → 유사도 매칭 순으로 탐색
     *  3. 해당 정의(def)가 존재할 경우:
     *     - def에 사전 정의된 옵션이 있으면 valueRaw를 가장 근접한 단일 옵션으로 매핑
     *       (chooseSingleOption → fallback: best similarity)
     *     - 옵션이 없으면 자유 입력형으로 valueRaw 그대로 사용
     *  4. 매핑 결과를 ClothesAttributeWithDefDto(defId, defName, options, finalValue)로 만들어 반환 리스트에 추가
     *
     * 결과적으로 Vision 결과의 raw definitionName/value를
     *  - DB/스냅샷 정의에 정규화·유사도 기반으로 매핑하고
     *  - 선택 가능한 옵션이 있다면 단일 선택값으로 정리하여
     * UI 드롭다운/속성 관리에 적합한 형태의 DTO 리스트를 반환한다.
     *
     * @param vision Vision 모델이 분석해 반환한 속성 결과
     * @param defs   Attribute 정의 스냅샷 목록 (id, name, options)
     * @return Vision 결과를 정의/옵션과 매핑한 ClothesAttributeWithDefDto 리스트
     */
    public List<ClothesAttributeWithDefDto> mapFromVision(
            VisionAnalysisResult vision,
            List<AttributeDefSnap> defs
    ) {
        if (vision == null || vision.attributes() == null) return List.of();

        Map<String, AttributeDefSnap> exactMap = defs.stream()
                .collect(Collectors.toMap(AttributeDefSnap::name, Function.identity(), (a,b)->a));

        Map<String, AttributeDefSnap> normalizedMap = defs.stream()
                .collect(Collectors.toMap(
                        d -> StringSimilarityUtils.normalize(d.name()),
                        Function.identity(),
                        (a,b)->a
                ));

        List<ClothesAttributeWithDefDto> result = new ArrayList<>();
        for (VisionAttributeItem item : vision.attributes()) {
            if (item == null) continue;

            String defNameRaw = Optional.ofNullable(item.definitionName()).orElse("").trim();
            String valueRaw   = Optional.ofNullable(item.value()).orElse("").trim();
            if (defNameRaw.isBlank() || valueRaw.isBlank()) continue;

            AttributeDefSnap def = exactMap.get(defNameRaw);
            if (def == null) def = normalizedMap.get(StringSimilarityUtils.normalize(defNameRaw));
            if (def == null) def = findBySimilarity(defNameRaw, defs);
            if (def == null) continue;

            List<String> options = def.options();

            String finalValue;
            if (!options.isEmpty()) {
                finalValue = chooseSingleOption(valueRaw, options);
                if (finalValue == null) {
                    finalValue = StringSimilarityUtils.findBestBySimilarity(valueRaw, options);
                }
            } else {
                finalValue = valueRaw;
            }

            result.add(new ClothesAttributeWithDefDto(
                    def.id(), def.name(), options, finalValue
            ));
        }
        return result;
    }

    private String chooseSingleOption(String valueRaw, List<String> options) {
        if (valueRaw == null || valueRaw.isBlank() || options == null || options.isEmpty()) return null;

        // 토큰 분리: 공백, /, , 등 구분자 + 숫자/퍼센트 제거
        // 예: "면 55% 린넨 45%" -> ["면", "린넨"]
        String normalized = valueRaw
                .replaceAll("\\d+\\s*%"," ")   // "55%" 제거
                .replaceAll("[\\/|,&·+]+"," ") // 구분자 -> 공백
                .replaceAll("\\s+"," ")
                .trim();

        String[] tokens = normalized.split(" ");

        // 토큰별로 가장 유사한 옵션에 표를 누적
        Map<String, Double> score = new HashMap<>();
        for (String token : tokens) {
            if (token.isBlank()) continue;

            String best = StringSimilarityUtils.findBestBySimilarity(token, options);
            double sim  = StringSimilarityUtils.similarity(token, best);

            // 너무 낮은 유사도는 무시
            if (sim < OPT_SIM_THRESH) continue;

            score.merge(best, sim, Double::sum); // 유사도 합산(간단 가중치)
        }

        // 표가 하나도 없으면 전체 문자열 기준으로 최고 유사 옵션 선택
        if (score.isEmpty()) {
            String best = StringSimilarityUtils.findBestBySimilarity(valueRaw, options);
            return best; // 어쨌든 1개 반환
        }

        // 가장 점수가 높은 옵션 1개 선택
        return score.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private AttributeDefSnap findBySimilarity(String target, List<AttributeDefSnap> defs) {
        String n = StringSimilarityUtils.normalize(target);
        return defs.stream()
                .map(d -> Map.entry(d, StringSimilarityUtils.similarity(n, StringSimilarityUtils.normalize(d.name()))))
                .filter(e -> e.getValue() >= DEF_SIM_THRESH)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
