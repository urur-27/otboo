package com.team3.otboo.domain.recommendation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.RecommendationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
public class RecommendationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RecommendationService recommendationService;

  @Test
  @WithMockUser
  @DisplayName("의상 추천 API를 호출하면, 성공(200 OK) 상태와 함계 추천 DTO를 반환한다")
  void getRecommendation_Success() throws Exception {
    // given:
    UUID userId = UUID.randomUUID();
    RecommendationDto expectedReuslt = new RecommendationDto(UUID.randomUUID(), userId, List.of());

    when(recommendationService.recommend(userId)).thenReturn(expectedReuslt);

    mockMvc.perform(get("/api/recommendations")
            .param("userId", userId.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.userId").value(userId.toString()));
  }
}
