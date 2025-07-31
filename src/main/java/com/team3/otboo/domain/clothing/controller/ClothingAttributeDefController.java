package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.service.ClothingAttributeDefService;
import com.team3.otboo.domain.clothing.service.Direction;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PreAuthorize("hasRole('ADMIN')")
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
        // "ASCENDING"/"DESCENDING" → Enum으로 변환
        Direction direction = Direction.fromApi(sortDirection);

        return ResponseEntity.ok(
                service.getAttributes(cursor, idAfter, limit, sortBy, direction, keywordLike)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{definitionId}")
    public ResponseEntity<Void> deleteAttributeDefinition(@PathVariable UUID definitionId) {
        service.deleteAttribute(definitionId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{definitionId}")
    public ResponseEntity<ClothingAttributeDefDto> updateAttributeDef(
            @PathVariable UUID definitionId,
            @RequestBody ClothingAttributeDefCreateRequest request) {

        ClothingAttributeDefDto response = service.updateAttribute(definitionId, request);
        return ResponseEntity.ok(response);
    }

}
