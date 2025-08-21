package com.team3.otboo.domain.notification.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team3.otboo.domain.notification.service.SseService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import com.team3.otboo.fixture.UserFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(SseController.class)
public class SseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SseService sseService;

  @Test
  @DisplayName("인증된 사용자가 SSE 구독을 요청하면 성공적으로 연결된다")
  void subscribe_Success() throws Exception {
    //given:
    CustomUserDetails userPrincipal = new CustomUserDetails(UserFixture.createDefaultUser());
    UUID userId = userPrincipal.getId();

    when(sseService.subscribe(userId)).thenReturn(new SseEmitter());

    //when & then:
    mockMvc.perform(get("/api/sse")
            .with(user(userPrincipal)))
        .andExpect(status().isOk())
        .andExpect(request().asyncStarted());
  }
}
