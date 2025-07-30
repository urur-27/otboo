package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.ProfileDto;
import java.util.UUID;

public interface ProfileService {

  ProfileDto getProfile(UUID userId);
}
