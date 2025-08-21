package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesDto;
import reactor.core.publisher.Mono;

public interface ClothingExtractionService {
    Mono<ClothesDto> extractFromUrlReactive(String url);
}
