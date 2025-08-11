package com.team3.otboo.domain.clothing.dto.response;

import java.util.List;

// LLM 응답을 받는 중간 DTO
public record VisionAnalysisResult (
        String name,
        String type,
        List<VisionAttributeItem> attributes
) {}
