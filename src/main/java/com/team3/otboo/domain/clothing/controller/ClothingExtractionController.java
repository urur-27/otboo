package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.service.ClothingExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/extractions")
public class ClothingExtractionController {

    private final ClothingExtractionService extractionService;

    @GetMapping
    public Mono<ResponseEntity<ClothesDto>> extractClothingInfo(@RequestParam("url") String url) {
        return extractionService.extractFromUrlReactive(url)
                .map(ResponseEntity::ok);
    }
}
