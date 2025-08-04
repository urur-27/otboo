package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.dto.Request.ProfileUpdateRequest;
import com.team3.otboo.storage.dto.BinaryContentCreateRequest;

import java.util.Optional;
import java.util.UUID;

public interface ProfileService {
  ProfileDto getProfile(UUID userId);

  ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, Optional<BinaryContentCreateRequest> profileCreateRequest);

}
