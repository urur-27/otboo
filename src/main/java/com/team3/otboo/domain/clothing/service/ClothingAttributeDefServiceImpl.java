package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothesAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.mapper.ClothingAttributeDefMapper;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.clothing.repository.ClothingAttributeValueRepository;
import com.team3.otboo.event.NewAttributeAddedEvent;
import com.team3.otboo.global.exception.attribute.AttributeNameDuplicatedException;
import com.team3.otboo.global.exception.attribute.AttributeNotFoundException;
import com.team3.otboo.global.exception.attribute.AttributeOptionEmptyException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothingAttributeDefServiceImpl implements ClothingAttributeDefService {

    private final AttributeRepository attributeRepository;
    private final ClothingAttributeDefMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ClothingAttributeValueRepository cavRepository;

    @Override
    @Transactional
    public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {
        if (attributeRepository.existsByName(request.name())) {
            throw new AttributeNameDuplicatedException();
        }

        Attribute attribute = mapper.toEntity(request);
        // 연관관계 보완
        attribute.getOptions().forEach(option -> option.assignAttribute(attribute));
        Attribute saved = attributeRepository.save(attribute);

        eventPublisher.publishEvent(new NewAttributeAddedEvent(saved.getName()));

        return mapper.toDto(saved);
    }

    @Override
    public CursorPageResponse<ClothesAttributeDefDto> getAttributes(
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

        List<ClothesAttributeDefDto> dtoList = result.data().stream()
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
    public ClothesAttributeDefDto updateAttribute(UUID definitionId,
            ClothesAttributeDefCreateRequest request) {
        Attribute attribute = attributeRepository.findById(definitionId)
                .orElseThrow(AttributeNotFoundException::new);

        List<String> values = request.selectableValues();
        // 옵션 리스트 비어있을 경우
        if (values == null || values.isEmpty()) {
            throw new AttributeOptionEmptyException();
        }

        // 이름 변경
        attribute.setName(request.name());

        // diff 기반 동기화
        attribute.syncOptions(values, cavRepository::countByOption_Id);

        return mapper.toDto(attribute);
    }
}
