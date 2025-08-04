package com.team3.otboo.fixture;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import java.util.List;
import java.util.UUID;

public class ClothingAttributeDefFixture {

    public static UUID definitionId() {
        return UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    }

    public static ClothingAttributeDefCreateRequest sampleCreateRequest() {
        return new ClothingAttributeDefCreateRequest(
                "색상",
                List.of("빨강", "파랑")
        );
    }

    public static Attribute sampleAttribute() {
        Attribute attribute = Attribute.of("색상", List.of("빨강", "파랑"));
        return attribute;
    }

    public static ClothingAttributeDefDto sampleDto(UUID id) {
        return new ClothingAttributeDefDto(
                id,
                "색상",
                List.of("빨강", "파랑")
        );
    }

    public static CursorPageResponse<Attribute> samplePage(Attribute attr) {
        return new CursorPageResponse<>(
                List.of(attr),
                "nextCursor",
                UUID.randomUUID(),
                "name",
                "ASC",
                1L,
                false
        );
    }
}
