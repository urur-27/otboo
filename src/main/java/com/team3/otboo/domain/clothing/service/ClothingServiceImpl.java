package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothingUpdateRequest;
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
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.attribute.AttributeNotFoundException;
import com.team3.otboo.global.exception.attributeoption.AttributeOptionNotFoundException;
import com.team3.otboo.global.exception.clothing.ClothingNotFoundException;
import com.team3.otboo.storage.ImageStorage;
import com.team3.otboo.storage.dto.ImageMaybeOrphanedEvent;
import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.entity.BinaryContentUploadStatus;
import com.team3.otboo.storage.repository.BinaryContentRepository;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public List<Clothing> getClothesByOwner(User user) {
    return List.of();
  }

  @Override
  @Transactional
  public ClothingDto registerClothing(User user, ClothingCreateRequest request,
          MultipartFile image) {
    log.info("의상 등록 서비스 시작");

    Clothing clothing = clothingMapper.toEntity(request);
    clothing.setOwner(user);
    if (image != null && !image.isEmpty()) {

      BinaryContent bin = uploadThroughBinaryContent(image);
      clothing.changeImage(bin); // ← FK + url 동시 세팅
    }

    // attributeValues 직접 생성 및 연관관계 연결
    for (ClothingAttributeDto attrReq : request.attributes()) {
      Attribute attribute = attributeRepository.findById(attrReq.definitionId())
              .orElseThrow(AttributeNotFoundException::new);
      AttributeOption option = attributeOptionRepository.findByAttributeIdAndValue(attribute.getId(), attrReq.value())
              .orElseThrow(AttributeOptionNotFoundException::new);

      ClothingAttributeValue.of(clothing, attribute, option);
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
            .map(clothing -> {
              ClothingDto dto = clothingMapper.toDto(clothing);
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
    clothingRepository.delete(clothing);
  }

  @Override
  @Transactional
  public ClothingDto updateClothing(UUID id, ClothingUpdateRequest req, MultipartFile image){
    Clothing clothing = clothingRepository.findById(id)
            .orElseThrow(ClothingNotFoundException::new);

    // 기본 정보 업데이트
    if (req.name() != null) clothing.setName(req.name());
    if (req.type() != null) clothing.setType(req.type());

    if(image != null && !image.isEmpty()){
      // 이전 이미지 참조 보관(정리용)
      BinaryContent old = clothing.getImage();

      // 새 이미지 업로드 → FK 교체
      BinaryContent fresh = uploadThroughBinaryContent(image);
      clothing.changeImage(fresh);

      // 커밋 후 안전 삭제 예약 (다른 곳에서 참조할 경우 주의)
      if (old != null) applicationEventPublisher.publishEvent(
              new ImageMaybeOrphanedEvent(old.getId())
      );
    }

    // 속성 업데이트(전체 교체)
    if (req.attributes() != null) {
      clothing.getAttributeValues().clear();

      for (ClothingAttributeDto attrDto : req.attributes()) {
        Attribute attribute = attributeRepository.findById(attrDto.definitionId())
                .orElseThrow(AttributeNotFoundException::new);
        AttributeOption option = attributeOptionRepository.findByAttributeAndValue(attribute, attrDto.value())
                .orElseThrow(AttributeOptionNotFoundException::new);

        ClothingAttributeValue.of(clothing, attribute, option);
      }
    }

    return clothingMapper.toDto(clothing);
  }

  // BinaryContent 생성(WAITING) → S3 put(id, bytes) → getPath(id, contentType) → SUCCESS
  private BinaryContent uploadThroughBinaryContent(MultipartFile image) {
    final String originalName = safeFileName(image.getOriginalFilename());
    final String contentType = safeContentType(image.getContentType(), originalName);
    final byte[] bytes = toBytes(image);

    // BinaryContent 저장 (WAITING)
    BinaryContent bin = new BinaryContent(
            originalName,
            (long) bytes.length,
            contentType,
            BinaryContentUploadStatus.WAITING
    );
    binaryContentRepository.save(bin); // UUID 발급

    try {
      // 업로드
      imageStorage.put(bin.getId(), bytes);

      // URL 생성
      String url = imageStorage.getPatch(bin.getId(), contentType);

      // 엔티티 갱신
      bin.markCompleted(url);

      return bin;
    } catch (RuntimeException ex) {
      bin.markFailed();
      throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, ex.getMessage());
    }
  }

  // --- 유틸들 ---

  private String safeFileName(String original) {
    String base = (original == null || original.isBlank()) ? "file" : original;
    return base.replaceAll("[\\\\/\\s]+", "_");
  }

  private String safeContentType(String raw, String fileName) {
    if (raw != null && !raw.isBlank())
      return raw;
    String lower = fileName.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png"))
      return "image/png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
      return "image/jpeg";
    if (lower.endsWith(".gif"))
      return "image/gif";
    if (lower.endsWith(".webp"))
      return "image/webp";
    return "application/octet-stream";
  }

  private byte[] toBytes(MultipartFile image) {
    try {
      return image.getBytes();
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "파일 읽기 실패: " + e.getMessage());
    }
  }
}
