package com.team3.otboo.fixture;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

  /**
   * ID가 이미 부여된, Mock 객체가 반환할 가짜 User를 만들 때 사용.
   * (Service 단위 테스트용)
   */
  public static User createDefaultUser() {
    User user = User.builder()
        .username("testuser")
        .email("test@example.com")
        .password("password")
        .role(Role.USER)
        .build();
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  /**
   * DB에 새로 저장하기 위한, ID가 없는 User를 만들 때 사용.
   * (Repository 저장 테스트용)
   */
  public static User createUnsavedUser() {
    return User.builder()
        .username("testuser")
        .email("test@example.com")
        .password("password")
        .role(Role.USER)
        .build();
  }

  public static User createAnotherUser() {
    User user = User.builder()
        .username("testuser2")
        .email("test2@example.com")
        .password("password")
        .role(Role.USER)
        .build();
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  public static User createThirdUser() {
    User user = User.builder()
        .username("testuser3")
        .email("test3@example.com")
        .password("password")
        .role(Role.USER)
        .build();
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }
}
