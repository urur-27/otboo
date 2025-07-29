package com.team3.otboo.fixture;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;

public class UserFixture {

  public static User createDefaultUser() {
    return User.builder()
        .username("testuser")
        .email("test@example.com")
        .password("password")
        .role(Role.USER)
        .build();
  }
}
