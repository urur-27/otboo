package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.mapper.ClothingDtoMerger;
import com.team3.otboo.domain.clothing.analyzers.HtmlExtractor;
import com.team3.otboo.domain.clothing.analyzers.VisionAnalyzer;
import com.team3.otboo.domain.clothing.mapper.AttributeMapper;
import com.team3.otboo.domain.clothing.repository.AttributeOptionRepository;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import java.text.Collator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothingExtractionServiceImpl implements ClothingExtractionService {

    private final HtmlExtractor htmlExtractor;
    private final VisionAnalyzer visionAnalyzer;
    private final ClothingDtoMerger clothingDtoMerger;
    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;
    private final AttributeOptionRepository attributeOptionRepository;

    @Override
    public ClothingDto extractFromUrl(String url) {
        HtmlExtractionResult html = htmlExtractor.extract(url);

        // DB 정의/값 목록 조회 → 프롬프트 재료
        List<Attribute> attrs = attributeRepository.findAllWithOptions();

        List<String> defNames = attrs.stream()
                .map(Attribute::getName)
                .toList();

        Map<String, List<String>> optionsByDef = new LinkedHashMap<>();
        for (Attribute a : attrs) {
            List<String> opts = a.getOptions().stream()
                    .map(AttributeOption::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .map(String::trim)
                    .sorted(Collator.getInstance(Locale.KOREAN)) // 한국어 정렬
                    .limit(60) // 토큰 폭발 방지 (상한 설정)
                    .toList();
            optionsByDef.put(a.getName(), opts);
        }

        // LLM에 분석 요청
        VisionAnalysisResult vision = visionAnalyzer.analyze(
                html.imageUrl(),
                html.title(),
                html.description(),
                defNames,
                optionsByDef
        );

        // DB 매핑/유사매칭으로 정규화
        List<ClothingAttributeWithDefDto> mappedAttrs = attributeMapper.mapFromVision(vision);

        // merge 시 attributes를 mappedAttrs로 교체
        return clothingDtoMerger.merge(html, vision, mappedAttrs);
    }
}