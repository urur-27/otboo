package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.mapper.ClothingDtoMerger;
import com.team3.otboo.domain.clothing.analyzers.HtmlExtractor;
import com.team3.otboo.domain.clothing.analyzers.VisionAnalyzer;
import com.team3.otboo.domain.clothing.mapper.AttributeMapper;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import java.util.List;
import java.util.UUID;
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

    @Override
    public ClothingDto extractFromUrl(String url, UUID ownerId) {
        HtmlExtractionResult html = htmlExtractor.extract(url);

        // DB 정의 목록 조회 → 프롬프트 재료
        List<String> defNames = attributeRepository.findAll().stream()
                .map(Attribute::getName)
                .toList();

        // LLM에 분석 요청
        VisionAnalysisResult vision = visionAnalyzer.analyze(
                html.imageUrl(),
                html.title(),
                html.description(),
                defNames
        );

        // DB 매핑/유사매칭으로 정규화
        List<ClothingAttributeWithDefDto> mappedAttrs = attributeMapper.mapFromVision(vision);

        // merge 시 attributes를 mappedAttrs로 교체
        return clothingDtoMerger.merge(ownerId, html, vision, mappedAttrs);
    }
}