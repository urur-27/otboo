package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.mapper.ClothingAttributeDefMapper;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.global.exception.attribute.AttributeNotFoundException;
import com.team3.otboo.global.exception.attribute.AttributeOptionEmptyException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothingAttributeDefServiceImpl implements ClothingAttributeDefService {

    private final AttributeRepository attributeRepository;
    private final ClothingAttributeDefMapper mapper;

    @Override
    @Transactional
    public ClothingAttributeDefDto create(ClothingAttributeDefCreateRequest request) {
        if (attributeRepository.existsByName(request.name())) {
            throw new AttributeNotFoundException();
        }

        Attribute attribute = mapper.toEntity(request);
        // 연관관계 보완
        attribute.getOptions().forEach(option -> option.assignAttribute(attribute));
        Attribute saved = attributeRepository.save(attribute);
        return mapper.toDto(saved);
    }

    @Override
    public CursorPageResponse<ClothingAttributeDefDto> getAttributes(
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            Direction direction,
            String keyword
    ) {
        CursorPageResponse<Attribute> result = attributeRepository.findAllByCursor(
                cursor, idAfter, limit, sortBy, direction, keyword
        );

        List<ClothingAttributeDefDto> dtoList = result.data().stream()
                .map(mapper::toDto)
                .toList();

        return new CursorPageResponse<>(
                dtoList,
                result.nextCursor(),
                result.nextIdAfter(),
                result.sortBy(),
                result.sortDirection(),
                result.totalCount(),
                result.hasNext()
        );
    }

    @Override
    @Transactional
    public void deleteAttribute(UUID definitionId) {
        Attribute attribute = attributeRepository.findById(definitionId)
                .orElseThrow(AttributeNotFoundException::new);

        attributeRepository.delete(attribute);
    }

    @Override
    @Transactional
    public ClothingAttributeDefDto updateAttribute(UUID definitionId,
            ClothingAttributeDefCreateRequest request) {
        Attribute attribute = attributeRepository.findById(definitionId)
                .orElseThrow(AttributeNotFoundException::new);

        // 옵션 리스트 비어있을 경우
        if (request.selectableValues().isEmpty()) {
            throw new AttributeOptionEmptyException();
        }

        // 이름 변경
        attribute.updateName(request.name());
        attribute.replaceOptions(request.selectableValues());

        return mapper.toDto(attribute);
    }
}
