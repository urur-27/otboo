package com.team3.otboo.domain.recommendation.repository;

import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, UUID>, RecommendationRuleRepositoryCustom {
}
