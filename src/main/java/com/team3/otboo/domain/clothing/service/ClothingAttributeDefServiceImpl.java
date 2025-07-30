package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.mapper.ClothingAttributeDefMapper;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothingAttributeDefServiceImpl implements ClothingAttributeDefService {

    private final AttributeRepository attributeRepository;
    private final ClothingAttributeDefMapper mapper;

    @Override
    @Transactional
    public ClothingAttributeDefDto create(ClothingAttributeDefCreateRequest request) {
        Attribute attribute = mapper.toEntity(request);
        // 연관관계 보완
        attribute.getOptions().forEach(option -> option.assignAttribute(attribute));
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

    @Override
    @Transactional
    public void deleteAttribute(UUID definitionId) {
        Attribute attribute = attributeRepository.findById(definitionId)
                .orElseThrow(() -> new NoSuchElementException("해당 속성을 찾을 수 없습니다."));

        attributeRepository.delete(attribute);
    }

    @Override
    @Transactional
    public ClothingAttributeDefDto updateAttribute(UUID definitionId,
            ClothingAttributeDefCreateRequest request) {
        Attribute attribute = attributeRepository.findById(definitionId)
                .orElseThrow(() -> new NoSuchElementException("해당 속성을 찾을 수 없습니다."));

        // 이름 변경
        attribute.updateName(request.name());
        attribute.replaceOptions(request.selectableValues());

        return mapper.toDto(attribute);
    }
}
