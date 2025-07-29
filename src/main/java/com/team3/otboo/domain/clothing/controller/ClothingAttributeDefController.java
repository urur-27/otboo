package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.service.ClothingAttributeDefService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
