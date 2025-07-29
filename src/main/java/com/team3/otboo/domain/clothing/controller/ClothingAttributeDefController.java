package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.service.ClothingAttributeDefService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class ClothingAttributeDefController {

    private final ClothingAttributeDefService service;

    @PostMapping
    public ResponseEntity<ClothingAttributeDefDto> create(
            @RequestBody @Valid ClothingAttributeDefCreateRequest request) {
        ClothingAttributeDefDto dto = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<ClothingAttributeDefDto>> getAttributes(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "DESCENDING") String sortDirection,
            @RequestParam(required = false) String keywordLike
    ) {
        // 커서가 없는 경우 idAfter로 대체
        String effectiveCursor = (cursor != null) ? cursor :
                (idAfter != null) ? idAfter.toString() : null;

        return ResponseEntity.ok(
                service.getAttributes(effectiveCursor, limit, sortBy, sortDirection, keywordLike)
        );
    }
}
