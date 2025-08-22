package com.team3.otboo.domain.recommendation.service.strategy;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.recommendation.entity.RuleToOption;
import com.team3.otboo.domain.recommendation.repository.RecommendationRuleRepository;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("scoringStrategy") // Bean 이름을 scoringStrategy로 지정
@RequiredArgsConstructor
public class ScoringStrategy implements RecommendationStrategy {

  private final RecommendationRuleRepository ruleRepository;
  private final ClothingMapper clothingMapper;

  @Override
  public RecommendationDto recommend(ProfileDto profile, WeatherDto weather, List<Clothing> clothes) {

    // 1. 현재 날씨에 해당하는 모든 규칙을 DB에서 가져옵니다.
    List<RecommendationRule> matchingRules = ruleRepository.findMatchingRules(weather);

    // 2. 각 옷 옵션(e.g., '반팔티')이 이번 날씨에 몇 점인지 점수 맵을 만듭니다.
    Map<UUID, Double> optionScores = calculateOptionScores(matchingRules);

    // 3. 사용자의 옷 목록을 보며, 각 옷의 최종 점수를 계산합니다.
    Map<Clothing, Double> clothingScores = calculateClothingScores(clothes, optionScores);

    // 4. 카테고리(상의, 하의 등)별로 가장 점수가 높은 옷을 하나씩 선택합니다.
    List<Clothing> recommendedItems = selectTopClothingPerCategory(clothingScores);

    // 5. 결과를 DTO로 변환하여 반환합니다.
    List<OotdDto> recommendedOotds = clothingMapper.toOotdDtoList(recommendedItems);
    return new RecommendationDto(weather.getId(), profile.getUserId(), recommendedOotds);
  }

  private Map<UUID, Double> calculateOptionScores(List<RecommendationRule> rules) {
    Map<UUID, Double> scores = new HashMap<>();
    for (RecommendationRule rule : rules) {
      for (RuleToOption link : rule.getRecommendedOptions()) {
        UUID optionId = link.getOption().getId();
        double score = rule.getScore() != null ? rule.getScore() : 0.0;
        scores.merge(optionId, score, Double::sum);
      }
    }
    return scores;
  }

  private Map<Clothing, Double> calculateClothingScores(List<Clothing> clothes, Map<UUID, Double> optionScores) {
    Map<Clothing, Double> scores = new HashMap<>();
    for (Clothing clothing : clothes) {
      double totalScore = 0.0;
      for (ClothingAttributeValue attrValue : clothing.getAttributeValues()) {
        totalScore += optionScores.getOrDefault(attrValue.getOption().getId(), 0.0);
      }
      scores.put(clothing, totalScore);
    }
    return scores;
  }

  private List<Clothing> selectTopClothingPerCategory(Map<Clothing, Double> clothingScores) {
    return clothingScores.entrySet().stream()
        .collect(Collectors.groupingBy(
            entry -> entry.getKey().getType(), // 옷의 타입(TOP, BOTTOM 등)으로 그룹핑
            Collectors.maxBy(Map.Entry.comparingByValue()) // 각 그룹에서 점수가 가장 높은 항목 찾기
        ))
        .values().stream()
        .filter(Optional::isPresent)
        .map(opt -> opt.get().getKey())
        .collect(Collectors.toList());
  }
}
