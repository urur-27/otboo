package com.team3.otboo.domain.notification.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.service.NotificationService;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import com.team3.otboo.fixture.UserFixture;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  @DisplayName("인증된 사용자가 알림 목록 조회를 요청하면, 성공(200 OK) 상태와 함께 알림 DTO 목록을 반환한다")
  void getNotifications_Success() throws Exception {
    // given:
    // 1. 테스트에 사용할 로그인된 사용자 정보를 준비합니다.
    CustomUserDetails userPrincipal = new CustomUserDetails(UserFixture.createDefaultUser());
    User user = userPrincipal.getUser();
    UUID userId = userPrincipal.getId();

    // 2. 가짜 NotificationService가 반환할 가짜 알림 목록을 미리 만들어 둡니다.
    List<NotificationDto> expectedResult = List.of(
        new NotificationDto(UUID.randomUUID(), null, userId, "알림 제목 1", "내용 1", null),
        new NotificationDto(UUID.randomUUID(), null, userId, "알림 제목 2", "내용 2", null)
    );
    when(notificationService.findNotificationsByUser(user)).thenReturn(expectedResult);

    // when & then:
    // 3. /api/notifications 엔드포인트로 GET 요청을 보내고 응답을 검증합니다.
    mockMvc.perform(get("/api/notifications")
            .with(user(userPrincipal)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답 타입이 JSON인지 확인
        .andExpect(jsonPath("$").isArray()) // 응답 본문이 배열인지 확인
        .andExpect(jsonPath("$.length()").value(2)) // 배열의 크기가 2인지 확인
        .andExpect(jsonPath("$[0].title").value("알림 제목 1")); // 첫 번째 알림의 제목 확인
  }
}
