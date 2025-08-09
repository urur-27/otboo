package com.team3.otboo.domain.user.dto;

import com.team3.otboo.domain.user.enums.OAuthProvider;
import com.team3.otboo.domain.user.enums.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto (
    UUID id,
    Instant createdAt,
    String email,
    String name,
    Role role,
    Set<OAuthProvider> linkedOAuthProviders,
    boolean locked
){

}
