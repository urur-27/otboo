package com.team3.otboo.domain.recommendation.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import com.team3.otboo.domain.weather.enums.WindSpeedLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationRule extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String ruleName;

  private Double minTemp;
  private Double maxTemp;

  private Double minHumidity;
  private Double maxHumidity;

  private Integer score;

  @Enumerated(EnumType.STRING)
  private SkyStatus skyStatus;

  @Enumerated(EnumType.STRING)
  private PrecipitationType precipitationType;

  @Enumerated(EnumType.STRING)
  private WindSpeedLevel windSpeedLevel;

  @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<RuleToOption> recommendedOptions = new ArrayList<>();

  @Builder
  public RecommendationRule(String ruleName, Double minTemp, Double maxTemp, Double minHumidity,
      Double maxHumidity, SkyStatus skyStatus, PrecipitationType precipitationType,
      WindSpeedLevel windSpeedLevel, Integer score) {
    this.ruleName = ruleName;
    this.minTemp = minTemp;
    this.maxTemp = maxTemp;
    this.minHumidity = minHumidity;
    this.maxHumidity = maxHumidity;
    this.skyStatus = skyStatus;
    this.precipitationType = precipitationType;
    this.windSpeedLevel = windSpeedLevel;
    this.score = score;
  }
}
