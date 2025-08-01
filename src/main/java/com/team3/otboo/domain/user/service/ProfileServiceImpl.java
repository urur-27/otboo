package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.ProfileDto;
import java.util.UUID;

import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.user.ProfileNotFoundException;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final ProfileRepository profileRepository;
  private final UserRepository userRepository;

  @Override
    public ProfileDto getProfile(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Profile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(ProfileNotFoundException::new);

        return ProfileDto.of(
                  profile.getUser().getId(),
                  profile.getUser().getUsername(),
                  profile.getGender(),
                  profile.getBirthDate(),
                  profile.getLocation(),
                  profile.getTemperatureSensitivity(),
                  profile.getProfileImageUrl()
                );
    }
}
