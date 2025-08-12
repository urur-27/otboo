package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesDto;

public interface ClothingExtractionService {
    ClothesDto extractFromUrl(String url);
}
