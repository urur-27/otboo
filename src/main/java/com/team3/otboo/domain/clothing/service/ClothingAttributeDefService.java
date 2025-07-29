package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;

public interface ClothingAttributeDefService {
    ClothingAttributeDefDto create(ClothingAttributeDefCreateRequest request);
}
