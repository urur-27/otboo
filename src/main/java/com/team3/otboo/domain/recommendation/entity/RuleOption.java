package com.team3.otboo.domain.recommendation.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleOption extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rule_id")
  private RecommendationRule rule;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "option_id")
  private AttributeOption option;

  @Builder
  public RuleOption(RecommendationRule rule, AttributeOption option) {
    this.rule = rule;
    this.option = option;
  }
}
