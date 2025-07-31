package com.team3.otboo.domain.user.dto.response;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.OAuthProvider;
import com.team3.otboo.domain.user.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
public class UserResponse {
    private UUID id;

    private Instant createdAt;

    private String email;

    private String name;

    private Role role;

    private Set<OAuthProvider> linkedOAuthProviders;

    private boolean locked;

    @Builder
    private UserResponse(UUID id, Instant createdAt, String email, String name, Role role, Set<OAuthProvider> linkedOAuthProviders, boolean locked) {
        this.id = id;
        this.createdAt = createdAt;
        this.email = email;
        this.name = name;
        this.role = role;
        this.linkedOAuthProviders = linkedOAuthProviders;
        this.locked = locked;
    }

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .name(user.getUsername())
                .role(user.getRole())
                .linkedOAuthProviders(user.getLinkedOAuthProviders())
                .locked(user.isLocked())
                .build();
    }

}
