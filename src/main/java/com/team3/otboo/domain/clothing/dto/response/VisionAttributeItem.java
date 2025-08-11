package com.team3.otboo.domain.clothing.dto.response;

import java.util.List;

// Vision에서 오는 가공 전 아이템
public record VisionAttributeItem(
        String definitionName,        // "색상", "스타일", "소재" 등
        String value,                 // "블랙", "검정색", "라이트블루" 등
        List<String> selectedValues   // 여러 값일 수도 있으니 보존 (선택)
) {}
