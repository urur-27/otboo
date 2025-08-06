package com.team3.otboo.domain.recommendation.controller;

import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.RecommendationService;
import com.team3.otboo.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

  private final RecommendationService recommendationService;

  @GetMapping
  public ResponseEntity<RecommendationDto> getRecommendation(@RequestParam UUID weatherId, @AuthenticationPrincipal
      CustomUserDetails userPrincipal) {
    UUID userId = userPrincipal.getId();
    RecommendationDto recommendation = recommendationService.recommend(weatherId, userId);
    return ResponseEntity.ok(recommendation);
  }
}
