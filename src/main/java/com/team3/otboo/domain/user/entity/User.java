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

    @Column(length = 20, unique = true, nullable = false)
    private String username;
    @Column(length = 50, unique = true, nullable = false)
    private String email;
    @Column(length = 50, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_oauth_providers", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "provider")
    private Set<OAuthProvider> linkedOAuthProviders;

    boolean locked;

    @Builder
    public User(String username, String email, String password, Role role, Set<OAuthProvider> linkedOAuthProviders) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.linkedOAuthProviders = linkedOAuthProviders;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateLocked(boolean locked) {
        this.locked = locked;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}
