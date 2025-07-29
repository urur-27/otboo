package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.mapper.ClothingAttributeDefMapper;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    @Override
    public CursorPageResponse<ClothingAttributeDefDto> getAttributes(
            String cursor,
            int limit,
            String sortBy,
            String sortDirection,
            String keyword
    ) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.DESC);
        CursorPageResponse<Attribute> result = attributeRepository.findAllByCursor(cursor, limit, sortBy, direction, keyword);
        List<ClothingAttributeDefDto> dtoList = result.data().stream()
                .map(mapper::toDto)
                .toList();

        return new CursorPageResponse<>(
                dtoList,
                result.nextCursor(),
                result.sortBy(),
                result.sortDirection(),
                result.totalCount()
        );
    }
}
