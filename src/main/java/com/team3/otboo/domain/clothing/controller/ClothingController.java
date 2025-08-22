package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.request.ClothesCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothesUpdateRequest;
import com.team3.otboo.domain.clothing.dto.response.ClothingDtoCursorResponse;
import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothingService clothingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothesDto> createClothing(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart("request") ClothesCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        log.info("의상 등록 시작");

        ClothesDto result = clothingService.registerClothing(user.getUser(), request, image);
        log.info("의상 등록 완료");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<ClothingDtoCursorResponse> getClothes(
            @RequestParam UUID ownerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam Integer limit,
            @RequestParam(required = false) String typeEqual,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        int pageSize = (limit == null) ? 20 : limit;
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        ClothingDtoCursorResponse response = clothingService.getClothesByCursor(
                ownerId, cursor, idAfter, pageSize, typeEqual, direction
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteClothing(@PathVariable UUID clothesId) {
        clothingService.deleteClothing(clothesId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothesDto> updateClothing(
            @PathVariable UUID clothesId,
            @RequestPart("request") ClothesUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ClothesDto response = clothingService.updateClothing(clothesId, request, image);
        return ResponseEntity.ok(response);
    }
}
