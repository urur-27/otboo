package com.team3.otboo.domain.recommendation.service.strategy;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.recommendation.repository.RecommendationRuleRepository;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("dataDrivenStrategy")
@RequiredArgsConstructor
public class DataDrivenStrategy implements RecommendationStrategy{

  private final RecommendationRuleRepository ruleRepository;
  private final ClothingMapper clothingMapper;

  @Override
  public RecommendationDto recommend(ProfileDto profile, WeatherDto weather,
      List<Clothing> clothes) {
    List<RecommendationRule> matchingRules = ruleRepository.findMatchingRules(weather);

    Set<UUID> recommendedOptionIds = matchingRules.stream()
        .flatMap(rule -> rule.getRecommendedOptions().stream())
        .map(ruleOption -> ruleOption.getOption().getId())
        .collect(Collectors.toSet());

    List<Clothing> recommendedItems = clothes.stream()
        .filter(clothing -> clothing.getAttributeValues().stream()
            .anyMatch(value -> recommendedOptionIds.contains(value.getId())))
        .toList();

    List<OotdDto> recommendedOotds = clothingMapper.toOotdDtoList(recommendedItems);

    return new RecommendationDto(
        weather.getId(),
        profile.getUserId(),
        recommendedOotds
    );
  }
}
