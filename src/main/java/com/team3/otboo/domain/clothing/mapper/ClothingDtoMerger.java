package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClothingDtoMerger {

    public ClothingDto merge(
            HtmlExtractionResult html,
            VisionAnalysisResult vision,
            List<ClothingAttributeWithDefDto> normalizedAttributes
    ) {
        String name = (vision.name() != null && !vision.name().isBlank())
                ? vision.name()
                : html.title();

        return new ClothingDto(
                null,                  // id는 저장 시 할당
                null,
                name,
                html.imageUrl(),
                vision.type(),
                normalizedAttributes    // 정규화된 속성 사용
        );
    }
}
