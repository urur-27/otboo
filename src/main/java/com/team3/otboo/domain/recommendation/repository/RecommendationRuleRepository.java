package com.team3.otboo.domain.recommendation.repository;

import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, UUID> {

  @Query("SELECT r FROM RecommendationRule r WHERE " +
      "(:currentTemp >= r.minTemp OR r.minTemp IS NULL) AND " +
      "(:currentTemp <= r.maxTemp OR r.maxTemp IS NULL) AND " +
      "(:skyStatus = r.skyStatus OR r.skyStatus IS NULL)")
  List<RecommendationRule> findMatchingRules(@Param("currentTemp") double currentTemp, @Param("skyStatus")
      SkyStatus skyStatus);
}
