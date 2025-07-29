package com.team3.otboo.domain.user.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDto (
    Long id,
    LocalDateTime createdAt,
    String email,
    String name,
    String role,
    Set<String> linkedOAuthProviders,
    boolean locked
){

}
