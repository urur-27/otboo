package com.team3.otboo.domain.clothing.controller;

import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothingUpdateRequest;
import com.team3.otboo.domain.clothing.dto.response.ClothingDtoCursorResponse;
import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ClothingDto> createClothing(
            @RequestPart("request") ClothingCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        log.info("의상 등록 시작");

        // 인증 서비스가 설정되지 않은 상태이므로 임시로 생성
        User user = User.builder()
                .username("temp")
                .email("temp@dev.com")
                .password("secret")
                .role(Role.USER)
                .linkedOAuthProviders(Set.of())
                .build();

        ClothingDto result = clothingService.registerClothing(user, request, image);
        log.info("의상 등록 완료");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<ClothingDtoCursorResponse> getClothes(
            @RequestParam UUID ownerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(required = false) Integer limit,
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
    public ResponseEntity<ClothingDto> updateClothing(
            @PathVariable UUID clothesId,
            @RequestPart("request") ClothingUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ClothingDto response = clothingService.updateClothing(clothesId, request, image);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/extractions")
//    public ResponseEntity<ClothingDto> extractClothingInfo(@RequestParam String url){
//        ClothingDto dto = extractionsService.extractAndSave(url);
//        return ResponseEntity.ok(dto);
//    }
}