package com.team3.otboo.domain.recommendation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.RecommendationService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import com.team3.otboo.fixture.UserFixture;
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
  @DisplayName("인증된 사용자가 의상 추천 API를 호출하면, 성공(200 OK) 상태와 함께 추천 DTO를 반환한다")
  void getRecommendation_Success_WithAuthenticatedUser() throws Exception {
    // given:
    CustomUserDetails userPrincipal = new CustomUserDetails(UserFixture.createDefaultUser());
    UUID userId = userPrincipal.getId();
    UUID weatherId = UUID.randomUUID();

    RecommendationDto expectedResult = new RecommendationDto(weatherId, userId, List.of());

    when(recommendationService.recommend(weatherId, userId)).thenReturn(expectedResult);

    // when & then:
    mockMvc.perform(get("/api/recommendations")
            .param("weatherId", weatherId.toString())
            .with(user(userPrincipal)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.userId").value(userId.toString()));
  }
}
