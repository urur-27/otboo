package com.team3.otboo.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.team3.otboo.domain.notification.enums.SortDirection;
import com.team3.otboo.domain.notification.service.NotificationService;
import com.team3.otboo.domain.user.dto.UserDto;
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
    CustomUserDetails userPrincipal = new CustomUserDetails(UserFixture.createDefaultUser());
    UUID userId = userPrincipal.getId();

    NotificationDtoCursorResponse expectedResponse = new NotificationDtoCursorResponse(
        List.of(), null, null, false, 0, "createdAt", SortDirection.DESC
    );

    when(notificationService.findNotificationsByUserId(eq(userId), any()))
        .thenReturn(expectedResponse);

    // when & then:
    mockMvc.perform(get("/api/notifications")
            .with(user(userPrincipal))
            .param("limit", "10")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.hasNext").value(false));
  }
}
