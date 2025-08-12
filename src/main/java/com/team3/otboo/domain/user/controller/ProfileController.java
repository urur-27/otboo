package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.dto.Request.ProfileUpdateRequest;
import com.team3.otboo.domain.user.service.ProfileService;
import com.team3.otboo.storage.BinaryContentFactory;
import com.team3.otboo.storage.dto.BinaryContentCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService profileService;
    private final BinaryContentFactory binaryContentFactory;

    @GetMapping("/{userId}/profiles")
    public ResponseEntity<ProfileDto> listUsers(
            @PathVariable("userId") UUID userId
    ) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }


    @PatchMapping(
            value = "/{userId}/profiles") public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable UUID userId,
            @RequestPart("request") @Valid ProfileUpdateRequest request,   // ← 이름 맞춤
            @RequestPart(value = "image", required = false) MultipartFile image // ← 이름 맞춤
    ){
        Optional<BinaryContentCreateRequest> profileImageRequest =
                Optional.ofNullable(image).flatMap(binaryContentFactory::resolveProfileRequest);

        ProfileDto profileDto = profileService.updateProfile(userId, request, profileImageRequest);
        return ResponseEntity.ok(profileDto); // 200으로
    }

}
