package com.team3.otboo.domain.user.user_details;

import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.OAuthProvider;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.oauth.OAuthAttributes;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        // OAuth2UserRequest에 들어있는 내용은 로그인을 제공해준 소셜로그인(google, kakao) 관련 정보

        // 기본 OAuth2USer 정보 받기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 소셜 구분 (google, kakao)
        String registerationId = userRequest.getClientRegistration().getRegistrationId();
        // 소셜 로그인별로 받은 데이터 -> enum 값으로 변환
        OAuthAttributes attributes = OAuthAttributes.of(registerationId, oAuth2User.getAttributes());
        // DB에서 사용자 조회 및 저장
        User user = saveOrUpdate(attributes, registerationId);
        // 쿠키로 전송

        // 인증서 발급
        return new CustomUserDetails(user, attributes.getAttributes());
    }

    private User saveOrUpdate(OAuthAttributes attributes, String registerationId) {
        // 기존 회원일 경우 -> Set linkedOAuthProviders에 현재 소셜 로그인 정보 추가
        // 신규 회원일 경우 -> User, Profile Entity를 새로 생성하고 User.password 필드에는 임의 값을 할당
        User user = userRepository.findByEmail(attributes.getEmail())
                // 기존 회원일 경우
                .map(entity -> {
                    Set<OAuthProvider> providers  = new HashSet<>(entity.getLinkedOAuthProviders());
                    // GOOGLE, KAKAO로 저장해놔서 upper해야함
                    providers.add(OAuthProvider.valueOf(registerationId.toUpperCase()));
                    // todo: 업데이트 로직 추가 ?
                    // entity.updateProfile(attributes.getName(), attributes.getPicture());
                    return userRepository.save(entity);
                })
                // 신규 회원일 경우
                .orElseGet(() -> {
                    String username = attributes.getName();
                    // username이 null일 경우 이메일 앞부분을 이름으로 지정
                    if(username == null || username.isBlank()){
                        username = attributes.getEmail().split("@")[0];
                    }

                    OAuthProvider provider = OAuthProvider.valueOf(registerationId.toUpperCase());
                    Set<OAuthProvider> providers = new HashSet<>();
                    providers.add(provider);
                    log.info("OAuth 신규회원 생성");

                    // User, Profile 생성
                    User newUser = User.builder()
                            .username(username)
                            .email(attributes.getEmail())
                            // 소셜 로그인 사용자는 비밀번호가 없으니 랜덤값 부여
                            .password(UUID.randomUUID().toString())
                            .role(Role.USER)
                            .linkedOAuthProviders(providers)
                            .build();
                    Profile newProfile = Profile.builder()
                            .user(newUser)
                            .build();

                    newUser.setProfile(newProfile);
                    userRepository.save(newUser);
                    profileRepository.save(newProfile);
                    return newUser;
                });
        return user;
    }
}
