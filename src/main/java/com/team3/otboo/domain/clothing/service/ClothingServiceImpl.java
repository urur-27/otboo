package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
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
import com.team3.otboo.storage.ImageStorage;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public List<Clothing> getClothesByOwner(User user) {
    return List.of();
  }

  @Override
  @Transactional
  public ClothingDto registerClothing(User user, ClothingCreateRequest request,
          MultipartFile image) {
    log.info("의상 등록 서비스 시작");
    log.info("이미지 업로드");
    String imageUrl = (image != null) ? imageStorage.upload(image) : null;

    Clothing clothing = clothingMapper.toEntity(request);

    // 연관관계 세팅
    clothing.updateOwner(user);           // 로그인 사용자
    clothing.updateImageUrl(imageUrl);    // 업로드 url

    // attributeValues 직접 생성 및 연관관계 연결
    for (ClothingAttributeDto attrReq : request.attributes()) {
      Attribute attribute = attributeRepository.findById(attrReq.definitionId())
              .orElseThrow(() -> new NoSuchElementException("해당 속성이 없습니다."));
      AttributeOption option = attributeOptionRepository.findByAttributeIdAndValue(attribute.getId(), attrReq.value())
              .orElseThrow(() -> new NoSuchElementException("해당 옵션이 없습니다."));

      ClothingAttributeValue cav = ClothingAttributeValue.of(clothing, attribute, option);
      clothing.addAttributeValue(cav);
    }

    clothingRepository.save(clothing);
      return clothingMapper.toDto(clothing);
  }

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
    List<ClothingDto> data = page.data().stream()
            .map(clothingMapper::toDto)
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
}
