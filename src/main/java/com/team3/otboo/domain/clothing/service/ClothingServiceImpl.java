package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.request.ClothesCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothesUpdateRequest;
import com.team3.otboo.domain.clothing.dto.response.ClothingDtoCursorResponse;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.clothing.repository.AttributeOptionRepository;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.clothing.repository.ClothingRepository;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.event.ClothingChangedEvent;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.attribute.AttributeNotFoundException;
import com.team3.otboo.global.exception.attributeoption.AttributeOptionNotFoundException;
import com.team3.otboo.global.exception.clothing.ClothingNotFoundException;
import com.team3.otboo.storage.BinaryContentUploader;
import com.team3.otboo.storage.ImageStorage;
import com.team3.otboo.storage.dto.ImageMaybeOrphanedEvent;
import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.repository.BinaryContentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothingServiceImpl implements ClothingService {

  private final ClothingRepository clothingRepository;
  private final ClothingMapper clothingMapper;
  private final ImageStorage imageStorage;
  private final AttributeRepository attributeRepository;
  private final AttributeOptionRepository attributeOptionRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final BinaryContentUploader binaryContentUploader;
  private final ApplicationEventPublisher publisher;

  @Override
  @Transactional(readOnly = true)
  public List<Clothing> getClothesByOwner(User user) {
    return clothingRepository.findByOwner(user);
  }

  @Override
  @Transactional
  public ClothesDto registerClothing(User user, ClothesCreateRequest request,
          MultipartFile image) {
    log.info("의상 등록 서비스 시작");

    Clothing clothing = clothingMapper.toEntity(request);
    clothing.setOwner(user);

    if (image != null && !image.isEmpty()) {
      BinaryContent bin = binaryContentUploader.upload(image);
      clothing.changeImage(bin);
    }

    // attributeValues 직접 생성 및 연관관계 연결
    for (ClothesAttributeDto attrReq : request.attributes()) {
      Attribute attribute = attributeRepository.findById(attrReq.definitionId())
              .orElseThrow(AttributeNotFoundException::new);

      String rawValue = attrReq.value();

      if (rawValue == null || rawValue.isBlank()) {
        //  미확정 값은 스킵 or null 저장
        log.warn("[ATTR] 속성 값 없음 → 미선택 처리 (defId={}, defName={})",
                attribute.getId(), attribute.getName());
        continue; // 또는 ClothingAttributeValue.of(clothing, attribute, null);
      }

      AttributeOption option = attributeOptionRepository
              .findByAttributeIdAndValue(attribute.getId(), rawValue)
              .orElse(null);

      if (option == null) {
        // 강제 예외 대신 미선택 처리
        log.warn("[ATTR] 옵션 매칭 실패 → 미선택 처리 (defId={}, defName={}, value='{}')",
                attribute.getId(), attribute.getName(), rawValue);
        continue;
      }

      ClothingAttributeValue.of(clothing, attribute, option);
    }

    clothingRepository.save(clothing);
    publisher.publishEvent(new ClothingChangedEvent(clothing.getId()));
    return clothingMapper.toDto(clothing);
  }

  @Cacheable(
          cacheNames = "clothingList",
          key = "T(java.util.Objects).hash(#ownerId, #cursor, #idAfter, #limit, #typeEqual, #direction)"
  )
  @Override
  @Transactional(readOnly = true)
  public ClothingDtoCursorResponse getClothesByCursor(
          UUID ownerId,
          String cursor,
          UUID idAfter,
          int limit,
          String typeEqual,
          Sort.Direction direction
  ) {
    CursorPageResponse<Clothing> page = clothingRepository.findAllByCursor(
            ownerId, cursor, idAfter, limit, typeEqual, direction
    );
    // 엔티티 → DTO 변환
    List<ClothesDto> data = page.data().stream()
            .map(clothing -> {
              ClothesDto dto = clothingMapper.toDto(clothing);
              if (dto == null) {
                throw new BusinessException(ErrorCode.CLOTHING_MAPPER_CONVERSION_FAILED, "Clothing → DTO 변환 실패");
              }
              return dto;
            })
            .toList();

    return new ClothingDtoCursorResponse(
            data,
            page.nextCursor(),
            page.nextIdAfter(),
            page.hasNext(),
            page.totalCount(),
            page.sortBy(),
            page.sortDirection()
    );
  }

  @Override
  @Transactional
  public void deleteClothing(UUID clothesId) {
    Clothing clothing = clothingRepository.findById(clothesId)
            .orElseThrow(ClothingNotFoundException::new);
    publisher.publishEvent(new ClothingChangedEvent(clothesId));
    clothingRepository.delete(clothing);
  }

  @Override
  @Transactional
  public ClothesDto updateClothing(UUID id, ClothesUpdateRequest req, MultipartFile image){
    Clothing clothing = clothingRepository.findById(id)
            .orElseThrow(ClothingNotFoundException::new);

    // 기본 정보 업데이트
    if (req.name() != null) clothing.setName(req.name());
    if (req.type() != null) clothing.setType(req.type());

    if(image != null && !image.isEmpty()){
      // 이전 이미지 참조 보관(정리용)
      BinaryContent old = clothing.getImage();

      // 새 이미지 업로드 → FK 교체
      BinaryContent fresh = binaryContentUploader.upload(image);
      clothing.changeImage(fresh);

      // 커밋 후 안전 삭제 예약 (다른 곳에서 참조할 경우 주의)
      if (old != null) applicationEventPublisher.publishEvent(
              new ImageMaybeOrphanedEvent(old.getId())
      );
    }

    // 속성 업데이트(전체 교체)
    if (req.attributes() != null) {
      clothing.getAttributeValues().clear();

      for (ClothesAttributeDto attrDto : req.attributes()) {
        Attribute attribute = attributeRepository.findById(attrDto.definitionId())
                .orElseThrow(AttributeNotFoundException::new);

        String rawValue = attrDto.value();
        if (rawValue == null || rawValue.isBlank()) {
          log.warn("[ATTR] 업데이트 시 속성 값 없음 → 미선택 처리 (defId={}, defName={})",
                  attribute.getId(), attribute.getName());
          continue;
        }

        AttributeOption option = attributeOptionRepository.findByAttributeAndValue(attribute, rawValue)
                .orElse(null);

        if (option == null) {
          log.warn("[ATTR] 업데이트 시 옵션 매칭 실패 → 미선택 처리 (defId={}, defName={}, value='{}')",
                  attribute.getId(), attribute.getName(), rawValue);
          continue;
        }

        ClothingAttributeValue.of(clothing, attribute, option);
      }
    }
    publisher.publishEvent(new ClothingChangedEvent(id));
    return clothingMapper.toDto(clothing);
  }


}
