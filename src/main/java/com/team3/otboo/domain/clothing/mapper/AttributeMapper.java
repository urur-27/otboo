package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAttributeItem;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.common.util.StringSimilarityUtils;
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

    public List<ClothesAttributeWithDefDto> mapFromVision(VisionAnalysisResult vision) {
        if (vision == null || vision.attributes() == null) return List.of();

        // DB 정의 로드 (캐싱 고려 가능)
        List<Attribute> allDefs = attributeRepository.findAll();
        // DB에 저장된 원래 이름 기준으로 매핑
        Map<String, Attribute> exactMap = allDefs.stream()
                .collect(Collectors.toMap(Attribute::getName, Function.identity(), (a,b)->a));

        // 소문자, 공백 제거 등 정규화된 이름 기준 매핑
        Map<String, Attribute> normalizedMap = allDefs.stream()
                .collect(Collectors.toMap(
                        a -> StringSimilarityUtils.normalize(a.getName()),
                        Function.identity(),
                        (a,b)->a
                ));

        List<ClothesAttributeWithDefDto> result = new ArrayList<>();

        for (VisionAttributeItem item : vision.attributes()) {
            if (item == null) continue;

            String defNameRaw = Optional.ofNullable(item.definitionName()).orElse("").trim();
            String valueRaw   = Optional.ofNullable(item.value()).orElse("").trim();

            if (defNameRaw.isBlank() || valueRaw.isBlank()) {
                log.debug("속성 스킵(definitionName/value 누락): {}", item);
                continue;
            }

            // 속성 찾기: 정확 → 정규화 일치 → 유사 매칭
            Attribute def = exactMap.get(defNameRaw);
            if (def == null) {
                def = normalizedMap.get(StringSimilarityUtils.normalize(defNameRaw));
            }
            if (def == null) {
                def = findDefinitionBySimilarity(defNameRaw, allDefs);
            }
            if (def == null) {
                log.warn("정의 매칭 실패 - Vision: [{}], value=[{}]", defNameRaw, valueRaw);
                continue;
            }

            //  옵션 매칭: 정의에 옵션이 없으면 raw 값 그대로 사용
            List<String> options = def.getOptions().stream()
                    .map(AttributeOption::getValue)
                    .toList();

            List<String> selectableValues = options; // ← 드롭다운 리스트 그대로 내려줌

            String finalValue;
            if (!options.isEmpty()) {
                finalValue = chooseSingleOption(valueRaw, options); // ← 여러 값이 있는 경우 하나로 고정
                if (finalValue == null) {
                    // 폴백 전략: 최고 유사 옵션 or raw
                    finalValue = StringSimilarityUtils.findBestBySimilarity(valueRaw, options);
                }
            } else {
                // 옵션이 아예 없는 자유입력형 속성이라면 raw 값을 그대로 사용
                finalValue = valueRaw;
            }

            result.add(new ClothesAttributeWithDefDto(
                    def.getId(),
                    def.getName(),
                    selectableValues,  // 드롭다운 목록
                    finalValue         // 실제 선택 값(단일)
            ));
        }

        return result;
    }

    private Attribute findDefinitionBySimilarity(String targetName, List<Attribute> allDefs) {
        String nTarget = StringSimilarityUtils.normalize(targetName);

        return allDefs.stream()
                .map(a -> Map.entry(a, StringSimilarityUtils.similarity(nTarget, StringSimilarityUtils.normalize(a.getName()))))
                .filter(e -> e.getValue() >= DEF_SIM_THRESH)
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
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
}
