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


    @PostMapping("/{userId}/profiles")
    public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable("userId") UUID userId,
            @RequestPart("profileUpdateRequest") @Valid ProfileUpdateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ){
        Optional<BinaryContentCreateRequest> profileImageRequest = Optional.ofNullable(profile)
                        .flatMap(binaryContentFactory::resolveProfileRequest);

        ProfileDto profileDto = profileService.updateProfile(userId, request, profileImageRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(profileDto);
    }


    private Optional<BinaryContentCreateRequest> resolveProfileRequest(MultipartFile profileFile) {
        if (profileFile.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
                        profileFile.getOriginalFilename(),
                        profileFile.getContentType(),
                        profileFile.getBytes()
                );
                return Optional.of(binaryContentCreateRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
