package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.ProfileDto;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.mapper.ProfileMapper;
import java.util.Optional;
import java.util.UUID;

import com.team3.otboo.domain.user.dto.Request.ProfileUpdateRequest;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.user.ProfileNotFoundException;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import com.team3.otboo.storage.ImageStorage;
import com.team3.otboo.storage.dto.BinaryContentCreateRequest;
import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.entity.BinaryContentUploadStatus;
import com.team3.otboo.storage.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final ProfileRepository profileRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserRepository userRepository;
    private final ImageStorage imageStorage;
  private final ProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Profile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(ProfileNotFoundException::new);

      return profileMapper.toDto(user, profile);
    }

    @Override
    @Transactional
    public ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, Optional<BinaryContentCreateRequest> profileCreateRequest) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Profile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(ProfileNotFoundException::new);

        BinaryContent nullableProfile = profileCreateRequest
                .map(profileRequest -> {

                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length, contentType, BinaryContentUploadStatus.WAITING);
                    binaryContentRepository.save(binaryContent);
                    imageStorage.put(binaryContent.getId(), bytes);
                    String imageUrl = imageStorage.getPatch(binaryContent.getId(), contentType);
                    binaryContent.updateImageUrl(imageUrl);
                    return binaryContent;
                })
                .orElse(null);

        profile.update(request.name(), request.gender(), request.birthDate(), request.location(), request.temperatureSensitivity(), nullableProfile);

        return ProfileDto.of(
                profile.getUser().getId(),
                profile.getUser().getUsername(),
                profile.getGender(),
                profile.getBirthDate(),
                profile.getLocation(),
                profile.getTemperatureSensitivity(),
                profile.getBinaryContent()
        );
    }


}
