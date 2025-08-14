package com.team3.otboo.domain.user.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.enums.OAuthProvider;
import com.team3.otboo.domain.user.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Column
	private String username;

	@Column
	private String email;

	@Column
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_oauth_providers", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "provider")
	private Set<OAuthProvider> linkedOAuthProviders;

    @Column
	boolean locked;

    // 임시 비밀번호, 만료시간
    private String tempPassword;
    private LocalDateTime tempPasswordExpirationDate;

    // mappedBy - 연관관계 주인 = user
    // fetch - user 조회할 때 profile 지연로딩(실제 사용시에만),
    // cascade - user의 저장, 삭제 등의 상태 변화를 profile에도 동일 적용
    // orphanRemoval - User와의 연관관계가 끊어진 profile은 자동 삭제
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

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

    public void updateUserName(String username) {this.username = username;}

    public void setProfile(Profile profile) {
        this.profile = profile;
        if (profile != null && profile.getUser() != this) {
            profile.setUser(this);
        }
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
        this.tempPasswordExpirationDate = LocalDateTime.now().plusMinutes(3);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.tempPassword = null;
        this.tempPasswordExpirationDate = null;
    }
}
