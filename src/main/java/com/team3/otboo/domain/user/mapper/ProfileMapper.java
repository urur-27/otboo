package com.team3.otboo.domain.user.mapper;

import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.username", target = "name")
  @Mapping(source = "profile.gender", target = "gender")
  @Mapping(source = "profile.birthDate", target = "birthDate")
  @Mapping(source = "profile.location", target = "location")
  @Mapping(source = "profile.temperatureSensitivity", target = "temperatureSensitivity")
  @Mapping(source = "profile.binaryContent.imageUrl", target = "profileImageUrl")
  ProfileDto toDto(User user, Profile profile);
}
