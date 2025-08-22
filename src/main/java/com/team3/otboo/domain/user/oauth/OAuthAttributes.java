package com.team3.otboo.domain.user.oauth;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    // user
    private String name;
    private String email;
    // profile
    private String picture;

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle("sub", attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) kakaoProfile.get("nickname");

        // 카카오는 이메일을 자동으로 제공하지 않기에 임의로 이메일 설정
        if(email == null) {
            email = nickname + "@kakao.com";
        }

        return OAuthAttributes.builder()
                .name(nickname)
                .email(email)
                .picture((String) kakaoProfile.get("picture_image_url"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
}
