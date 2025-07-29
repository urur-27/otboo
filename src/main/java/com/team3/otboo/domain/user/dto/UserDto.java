package com.team3.otboo.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserDto {
    private Long id;
    private LocalDateTime createdAt;
    private String email;
    private String name;
    private String role;
    private Set<String> linkedOAuthProviders;
    private boolean locked;
}
