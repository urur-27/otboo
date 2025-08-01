package com.team3.otboo.domain.recommendation.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.weather.enums.SkyStatus;
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

  @Enumerated(EnumType.STRING)
  private SkyStatus skyStatus;

  @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<RuleOption> recommendedOptions = new ArrayList<>();

  @Builder
  public RecommendationRule(String ruleName, Double minTemp, Double maxTemp, SkyStatus skyStatus) {
    this.ruleName = ruleName;
    this.minTemp = minTemp;
    this.maxTemp = maxTemp;
    this.skyStatus = skyStatus;
  }
}
