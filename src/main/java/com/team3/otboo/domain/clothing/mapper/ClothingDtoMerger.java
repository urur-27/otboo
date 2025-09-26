package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ClothingDtoMerger {

    public ClothesDto merge(
            HtmlExtractionResult html,
            @Nullable VisionAnalysisResult vision,
            List<ClothesAttributeWithDefDto> normalizedAttributes
    ) {
        String name = (vision != null && notBlank(vision.name()))
                ? vision.name().trim()
                : safe(html.title());

        // type은 vision이 주지 않으면 null 유지(프론트에서 기존 선택값/placeholder)
        String type = (vision != null && notBlank(vision.type()))
                ? vision.type().trim()
                : null;

        return new ClothesDto(
                null,                  // id는 저장 시 할당
                null,
                name,
                safe(html.imageUrl()),
                type,
                normalizedAttributes    // 정규화된 속성 사용
        );
    }
    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static String safe(String s) { return s == null ? "" : s; }
}
