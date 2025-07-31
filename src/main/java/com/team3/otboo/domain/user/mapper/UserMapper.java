package com.team3.otboo.domain.user.mapper;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user){
        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getLinkedOAuthProviders(),
                user.isLocked()
        );
    }
}
