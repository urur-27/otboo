package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingDto;
import java.util.UUID;

public interface ClothingExtractionService {
    ClothingDto extractFromUrl(String url, UUID ownerId);
}