package com.team3.otboo.domain.user.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.enums.OAuthProvider;
import com.team3.otboo.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_oauth_providers", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "provider")
    private Set<OAuthProvider> linkedOAuthProviders;

    boolean locked;

    @Builder
    private User(String email, String password, Role role, Set<OAuthProvider> linkedOAuthProviders) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.linkedOAuthProviders = linkedOAuthProviders;
    }
}
