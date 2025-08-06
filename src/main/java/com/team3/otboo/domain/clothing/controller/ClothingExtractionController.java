package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.service.ClothingExtractionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/extractions")
public class ClothingExtractionController {

    private final ClothingExtractionService extractionService;

    @GetMapping
    public ResponseEntity<ClothingDto> extractClothingInfo(
            @RequestParam("url") String url
//            ,@CurrentUser User user // 사용자 인증 정보를 가져온다고 가정
    ) {
        UUID fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // 테스트용 사용자 ID
        ClothingDto clothingDto = extractionService.extractFromUrl(url, fakeUserId);
        return ResponseEntity.ok(clothingDto);
    }
}