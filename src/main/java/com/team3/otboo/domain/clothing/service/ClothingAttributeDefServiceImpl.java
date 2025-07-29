package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.mapper.ClothingAttributeDefMapper;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothingAttributeDefServiceImpl implements ClothingAttributeDefService {

    private final AttributeRepository attributeRepository;
    private final ClothingAttributeDefMapper mapper;

    @Override
    public ClothingAttributeDefDto create(ClothingAttributeDefCreateRequest request) {
        Attribute attribute = mapper.toEntity(request);
        Attribute saved = attributeRepository.save(attribute);
        return mapper.toDto(saved);
    }
}
