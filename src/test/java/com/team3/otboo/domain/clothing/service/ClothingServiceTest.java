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
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.clothing.repository.AttributeOptionRepository;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.clothing.repository.ClothingRepository;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.fixture.BinaryContentFixture;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.attribute.AttributeNotFoundException;
import com.team3.otboo.global.exception.attributeoption.AttributeOptionNotFoundException;
import com.team3.otboo.global.exception.clothing.ClothingNotFoundException;
import com.team3.otboo.storage.ImageStorage;
import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.entity.BinaryContentUploadStatus;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.team3.otboo.fixture.ClothingDtoFixture;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClothingServiceTest {

    @InjectMocks
    private ClothingServiceImpl clothingService;

    @Mock
    private ClothingRepository clothingRepository;

    @Mock
    private ClothingMapper clothingMapper;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeOptionRepository attributeOptionRepository;

    @Mock
    private com.team3.otboo.storage.BinaryContentUploader binaryContentUploader;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Nested
    @DisplayName("의상 등록 테스트")
    class RegisterClothingTest {
        @Test
        @DisplayName("registerClothing 성공 테스트")
        void registerClothing_success() throws Exception {
            // given
            MultipartFile image = mock(MultipartFile.class);
            UUID attributeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID clothingId = UUID.randomUUID();

            User mockUser = mock(User.class);

            // 실제 엔티티를 사용해야 changeImage로 FK + url 세팅 상태를 검증할 수 있음
            Clothing realClothing = new Clothing();
            // (필요시 ID 세팅)
            setId(realClothing, clothingId);

            ClothesCreateRequest request = ClothingDtoFixture.sampleCreateRequest(attributeId);
            Attribute mockAttribute = mock(Attribute.class);
            when(mockAttribute.getId()).thenReturn(attributeId);
            AttributeOption mockOption = mock(AttributeOption.class);

            // 업로더가 반환할 BinaryContent (SUCCESS + url)
            UUID binId = UUID.randomUUID();
            String imageUrl = "http://cdn.example.com/" + binId;
            BinaryContent bin = new BinaryContent("file.jpg", 3L, "image/jpeg", BinaryContentUploadStatus.SUCCESS);
            setId(bin, binId);
            bin.updateImageUrl(imageUrl);

            // stub 정의
            when(binaryContentUploader.upload(image)).thenReturn(bin);
            when(clothingMapper.toEntity(request)).thenReturn(realClothing);
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(mockAttribute));
            when(attributeOptionRepository.findByAttributeIdAndValue(attributeId, "청색"))
                    .thenReturn(Optional.of(mockOption));

            ClothesDto expectedDto = ClothingDtoFixture.sampleExpectedDto(clothingId, userId, attributeId, imageUrl);
            when(clothingMapper.toDto(realClothing)).thenReturn(expectedDto);

            // when
            ClothesDto result = clothingService.registerClothing(mockUser, request, image);

            // then
            verify(binaryContentUploader).upload(image); // 업로더 호출 검증
            verify(clothingRepository).save(realClothing);
            verify(attributeRepository).findById(attributeId);
            verify(attributeOptionRepository).findByAttributeIdAndValue(attributeId, "청색");
            verify(clothingMapper).toDto(realClothing);

            // changeImage에 의해 FK + url이 세팅됐는지 엔티티 상태로 검증
            assertThat(realClothing.getImage()).isNotNull();
            assertThat(realClothing.getImage().getId()).isEqualTo(binId);
            assertThat(realClothing.getImageUrl()).isEqualTo(imageUrl);

            // DTO 검증
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("청바지");
            assertThat(result.ownerId()).isEqualTo(userId);
            assertThat(result.imageUrl()).isEqualTo(imageUrl);
            assertThat(result.type()).isEqualTo("BOTTOM");
            assertThat(result.attributes()).hasSize(1);
            assertThat(result.attributes().getFirst().value()).isEqualTo("청색");
            assertThat(result.attributes().getFirst().definitionName()).isEqualTo("색상");
        }

        @Test
        @DisplayName("registerClothing 실패 테스트 - 이미지 업로드 실패")
        void registerClothing_fail_whenImageUploadFails() {
            // given
            MultipartFile image = mock(MultipartFile.class);
            ClothesCreateRequest request = ClothingDtoFixture.sampleCreateRequest(UUID.randomUUID());
            User user = mock(User.class);

            // 엔티티 생성으로 NPE 방지
            Clothing clothing = new Clothing();
            when(clothingMapper.toEntity(request)).thenReturn(clothing);

            // 업로더에서 예외 발생
            when(binaryContentUploader.upload(image))
                    .thenThrow(new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED));

            // when & then
            assertThatThrownBy(() -> clothingService.registerClothing(user, request, image))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.IMAGE_UPLOAD_FAILED.getMessage());

            verify(binaryContentUploader).upload(image);
            verifyNoInteractions(clothingRepository); // 저장 시도 없음
        }


        @Test
        @DisplayName("registerClothing 실패 테스트 - Attribute가 없는 경우")
        void registerClothing_fail_whenAttributeNotFound() throws Exception {
            // given
            MultipartFile image = mock(MultipartFile.class);
            UUID attributeId = UUID.randomUUID();
            ClothesCreateRequest request = ClothingDtoFixture.sampleCreateRequest(attributeId);
            User user = mock(User.class);

            // 엔티티 만들어 NPE 방지
            Clothing clothing = new Clothing();
            when(clothingMapper.toEntity(request)).thenReturn(clothing);

            // 업로더는 성공 (서비스 흐름상 업로드 후 속성 검증)
            BinaryContent bin = BinaryContentFixture.successWithUrl();
            when(binaryContentUploader.upload(image)).thenReturn(bin);

            // 존재하지 않는 attribute
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothingService.registerClothing(user, request, image))
                    .isInstanceOf(AttributeNotFoundException.class);

            verify(binaryContentUploader).upload(image);
            verify(attributeRepository).findById(attributeId);
            verifyNoMoreInteractions(attributeOptionRepository, clothingRepository);
        }


        @Test
        @DisplayName("registerClothing 실패 테스트 - AttributeOption이 없는 경우")
        void registerClothing_fail_whenAttributeOptionNotFound() throws Exception {
            // given
            MultipartFile image = mock(MultipartFile.class);
            UUID attributeId = UUID.randomUUID();
            User user = mock(User.class);
            ClothesCreateRequest request = ClothingDtoFixture.sampleCreateRequest(attributeId);

            Attribute attribute = mock(Attribute.class);
            when(attribute.getId()).thenReturn(attributeId);

            // 업로더 성공
            BinaryContent bin = BinaryContentFixture.successWithUrl();
            when(binaryContentUploader.upload(image)).thenReturn(bin);

            // 엔티티 생성
            Clothing clothing = new Clothing();
            when(clothingMapper.toEntity(request)).thenReturn(clothing);

            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
            // 옵션 없음
            when(attributeOptionRepository.findByAttributeIdAndValue(attributeId, "청색"))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothingService.registerClothing(user, request, image))
                    .isInstanceOf(AttributeOptionNotFoundException.class);

            verify(binaryContentUploader).upload(image);
            verify(attributeRepository).findById(attributeId);
            verify(attributeOptionRepository).findByAttributeIdAndValue(attributeId, "청색");
            verifyNoMoreInteractions(clothingRepository);
        }

    }

    @Nested
    @DisplayName("의상 조회 테스트")
    class ReadClothingTest {
        @Test
        @DisplayName("getClothesByCursor 성공 테스트")
        void getClothesByCursor_success() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID idAfter = UUID.randomUUID();
            String cursor = "2024-08-01T00:00:00";
            int limit = 10;
            String typeEqual = "TOP";
            Sort.Direction direction = Sort.Direction.DESC;

            // Clothing 엔티티 mock 리스트
            Clothing clothing1 = mock(Clothing.class);
            Clothing clothing2 = mock(Clothing.class);
            List<Clothing> mockClothes = List.of(clothing1, clothing2);

            // CursorPageResponse mock
            CursorPageResponse<Clothing> mockPage = new CursorPageResponse<>(
                    mockClothes,
                    "next-cursor",
                    UUID.randomUUID(),
                    "createdAt",
                    "DESC",
                    2L,
                    true
            );

            // ClothingDto mock 리스트
            ClothesDto dto1 = mock(ClothesDto.class);
            ClothesDto dto2 = mock(ClothesDto.class);
            when(clothingRepository.findAllByCursor(ownerId, cursor, idAfter, limit, typeEqual, direction))
                    .thenReturn(mockPage);
            when(clothingMapper.toDto(clothing1)).thenReturn(dto1);
            when(clothingMapper.toDto(clothing2)).thenReturn(dto2);

            // when
            ClothingDtoCursorResponse result = clothingService.getClothesByCursor(
                    ownerId, cursor, idAfter, limit, typeEqual, direction
            );

            // then
            assertThat(result.data()).containsExactly(dto1, dto2);
            assertThat(result.nextCursor()).isEqualTo("next-cursor");
            assertThat(result.totalCount()).isEqualTo(2L);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.sortBy()).isEqualTo("createdAt");
            assertThat(result.sortDirection()).isEqualTo("DESC");

            verify(clothingRepository).findAllByCursor(ownerId, cursor, idAfter, limit, typeEqual, direction);
            verify(clothingMapper).toDto(clothing1);
            verify(clothingMapper).toDto(clothing2);
        }

        @Test
        @DisplayName("getClothesByCursor 성공 테스트 - 조회 결과가 없을 때, 빈 리스트 반환")
        void getClothesByCursor_returnEmptyList_whenNoClothingFound() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID idAfter = null;
            String cursor = null;
            int limit = 10;
            String typeEqual = null;
            Sort.Direction direction = Sort.Direction.DESC;

            CursorPageResponse<Clothing> emptyPage = new CursorPageResponse<>(
                    List.of(),
                    null,
                    null,
                    "createdAt",
                    "DESC",
                    0L,
                    false
            );

            when(clothingRepository.findAllByCursor(ownerId, cursor, idAfter, limit, typeEqual, direction))
                    .thenReturn(emptyPage);

            // when
            ClothingDtoCursorResponse result = clothingService.getClothesByCursor(
                    ownerId, cursor, idAfter, limit, typeEqual, direction
            );

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();
        }

        @Test
        @DisplayName("getClothesByCursor 실패 테스트 - mapper가 null을 반환하면 비즈니스 예외를 발생")
        void getClothesByCursor_fail_whenMapperReturnsNull() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID idAfter = UUID.randomUUID();
            String cursor = "2024-08-01T00:00:00";
            int limit = 10;
            String typeEqual = "TOP";
            Sort.Direction direction = Sort.Direction.DESC;

            Clothing mockClothing = mock(Clothing.class);
            CursorPageResponse<Clothing> mockPage = new CursorPageResponse<>(
                    List.of(mockClothing),
                    "next-cursor",
                    UUID.randomUUID(),
                    "createdAt",
                    "DESC",
                    1L,
                    true
            );

            when(clothingRepository.findAllByCursor(ownerId, cursor, idAfter, limit, typeEqual, direction))
                    .thenReturn(mockPage);

            // toDto가 null을 반환하도록 강제
            when(clothingMapper.toDto(mockClothing)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> clothingService.getClothesByCursor(
                    ownerId, cursor, idAfter, limit, typeEqual, direction
            )).isInstanceOf(BusinessException.class);

            verify(clothingMapper).toDto(mockClothing);
        }
    }

    @Nested
    @DisplayName("의상 수정 테스트")
    class UpdateClothingTest {
        @DisplayName("updateClothing 성공 테스트")
        @Test
        void updateClothing_success() throws Exception {
            // given
            UUID clothingId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();

            Clothing clothing = new Clothing(); // ← 실제 엔티티로 상태 검증
            Attribute attribute = mock(Attribute.class);
            AttributeOption option = mock(AttributeOption.class);
            MultipartFile image = new MockMultipartFile("image", "shirt.jpg", "image/jpeg", "data".getBytes());

            ClothesUpdateRequest request = ClothingDtoFixture.sampleUpdateRequest(attributeId);

            // 업로더가 돌려줄 BinaryContent (SUCCESS + url)
            UUID binId = UUID.randomUUID();
            String imageUrl = "http://cdn.example.com/" + binId;
            BinaryContent bin = new BinaryContent("shirt.jpg", 4L, "image/jpeg", BinaryContentUploadStatus.SUCCESS);
            setId(bin, binId);
            bin.updateImageUrl(imageUrl);

            ClothesDto expectedDto =
                    ClothingDtoFixture.sampleUpdatedDto(clothingId, userId, attributeId, imageUrl);

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(binaryContentUploader.upload(image)).thenReturn(bin);              // 업로더 사용
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
            when(attributeOptionRepository.findByAttributeAndValue(attribute, "흰색"))
                    .thenReturn(Optional.of(option));
            when(clothingMapper.toDto(clothing)).thenReturn(expectedDto);

            // when
            ClothesDto result = clothingService.updateClothing(clothingId, request, image);

            // then
            assertThat(result).isEqualTo(expectedDto);
            // 이름/타입 세팅은 기존대로 검증 (실제 엔티티 사용 시 세터 호출 검증 대신 상태 검증 가능)
            // 여기서는 기존 verify 스타일을 유지하려면 spy가 필요하므로, 상태 검증으로 대체:
            // verify(clothing).setName("셔츠"); verify(clothing).setType("TOP");  ← mock일 때만 가능
            // 상태 검증:
            assertThat(clothing.getName()).isEqualTo("셔츠");
            assertThat(clothing.getType()).isEqualTo("TOP");

            // 이미지 변경 결과 검증
            verify(binaryContentUploader).upload(image);
            assertThat(clothing.getImage()).isNotNull();
            assertThat(clothing.getImage().getId()).isEqualTo(binId);
            assertThat(clothing.getImageUrl()).isEqualTo(imageUrl);

            verify(clothingRepository).findById(clothingId);
            verify(clothingMapper).toDto(clothing);

            // 더 이상 ImageStorage는 관여 x
            verifyNoInteractions(imageStorage);
        }

        @DisplayName("updateClothing 성공 테스트 - 이미지와 속성 없이 이름과 타입만 수정")
        @Test
        void updateClothing_onlyNameAndTypeChanged() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = new Clothing();

            ClothesUpdateRequest request = new ClothesUpdateRequest("셔츠", "TOP", null);
            MultipartFile image = null;

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(clothingMapper.toDto(clothing)).thenReturn(mock(ClothesDto.class));

            // when
            ClothesDto result = clothingService.updateClothing(clothingId, request, image);

            // then
            assertThat(clothing.getName()).isEqualTo("셔츠");
            assertThat(clothing.getType()).isEqualTo("TOP");
            verifyNoInteractions(binaryContentUploader); // 업로더 호출 없음
            verify(clothingMapper).toDto(clothing);
            verifyNoInteractions(imageStorage);          // 더 이상 사용 안 함
        }

        @DisplayName("updateClothing 성공 테스트 - 이미지만 수정 (기존 이미지 존재)")
        @Test
        void updateClothing_onlyImageChanged_withOldImage() throws Exception {
            // given
            UUID clothingId = UUID.randomUUID();

            // 실제 엔티티로 검증
            Clothing clothing = new Clothing();
            clothing.changeImage(existingBinWithUrl()); // 기존 이미지 있다고 가정
            MultipartFile image = new MockMultipartFile("image", "shirt.jpg", "image/jpeg", "data".getBytes());

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(clothingMapper.toDto(clothing)).thenReturn(mock(ClothesDto.class));

            // 새 업로드 결과
            UUID newBinId = UUID.randomUUID();
            String newUrl = "http://cdn.example.com/" + newBinId;
            BinaryContent newBin = new BinaryContent("shirt.jpg", 4L, "image/jpeg", BinaryContentUploadStatus.SUCCESS);
            setId(newBin, newBinId);
            newBin.updateImageUrl(newUrl);

            when(binaryContentUploader.upload(image)).thenReturn(newBin);

            // when
            ClothesDto result = clothingService.updateClothing(
                    clothingId, new ClothesUpdateRequest(null, null, null), image);

            // then
            verify(binaryContentUploader).upload(image);
            assertThat(clothing.getImage().getId()).isEqualTo(newBinId);
            assertThat(clothing.getImageUrl()).isEqualTo(newUrl);
            verifyNoInteractions(imageStorage);
        }

        @DisplayName("updateClothing 성공 테스트 - 이미지 파일이 비어 있을 때 이미지 변경 무시")
        @Test
        void updateClothing_skipImageUpdateWhenEmpty() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = new Clothing();
            MultipartFile emptyImage =
                    new MockMultipartFile("image", "", "image/jpeg", new byte[0]); // isEmpty = true

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(clothingMapper.toDto(clothing)).thenReturn(mock(ClothesDto.class));

            // when
            ClothesDto result = clothingService.updateClothing(
                    clothingId, new ClothesUpdateRequest(null, null, null), emptyImage);

            // then
            verifyNoInteractions(binaryContentUploader); // 업로더 호출 없음
            verify(clothingMapper).toDto(clothing);
            verifyNoInteractions(imageStorage);
        }

        @DisplayName("updateClothing 실패 테스트 - 존재하지 않는 의상 ID로 수정 시 예외 발생")
        @Test
        void updateClothing_fail_whenClothingNotFound() {
            // given
            UUID clothingId = UUID.randomUUID();
            when(clothingRepository.findById(clothingId)).thenReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() ->
                    clothingService.updateClothing(clothingId, mock(ClothesUpdateRequest.class), null)
            );

            // then
            assertThat(thrown)
                    .isInstanceOf(ClothingNotFoundException.class)
                    .hasMessage("해당 의상을 찾을 수 없습니다.");
        }

        @DisplayName("updateClothing 실패 - 존재하지 않는 속성 ID로 수정 시 예외 발생")
        @Test
        void updateClothing_fail_whenAttributeNotFound() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = mock(Clothing.class);
            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));

            UUID attributeId = UUID.randomUUID();
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    "셔츠", "TOP",
                    List.of(new ClothesAttributeDto(attributeId, "청색"))
            );

            when(attributeRepository.findById(attributeId)).thenReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() ->
                    clothingService.updateClothing(clothingId, request, null)
            );

            // then
            assertThat(thrown)
                    .isInstanceOf(AttributeNotFoundException.class)
                    .hasMessage("해당 속성을 찾을 수 없습니다.");
        }

        @DisplayName("updateClothing 실패 테스트 - 존재하지 않는 옵션 값으로 수정 시 예외 발생")
        @Test
        void updateClothing_fail_whenAttributeOptionNotFound() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = mock(Clothing.class);
            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));

            UUID attributeId = UUID.randomUUID();
            Attribute attribute = mock(Attribute.class);
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));

            when(attributeOptionRepository.findByAttributeAndValue(attribute, "청색"))
                    .thenReturn(Optional.empty());

            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    "셔츠", "TOP",
                    List.of(new ClothesAttributeDto(attributeId, "청색"))
            );

            // when
            Throwable thrown = catchThrowable(() ->
                    clothingService.updateClothing(clothingId, request, null)
            );

            // then
            assertThat(thrown)
                    .isInstanceOf(AttributeOptionNotFoundException.class)
                    .hasMessage("해당 속성 값을 찾을 수 없습니다.");
        }



    }

    @Nested
    @DisplayName("의상 삭제 테스트")
    class DeleteClothingTest {
        @Test
        @DisplayName("deleteClothing 성공 테스트")
        void deleteClothing_success() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing mockClothing = mock(Clothing.class);

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(mockClothing));

            // when
            clothingService.deleteClothing(clothingId);

            // then
            verify(clothingRepository).findById(clothingId);
            verify(clothingRepository).delete(mockClothing);
        }

        @Test
        @DisplayName("deleteClothing 실패 테스트 - 존재하지 않는 의상 ID")
        void deleteClothing_fail_whenClothingNotFound() {
            // given
            UUID clothingId = UUID.randomUUID();
            when(clothingRepository.findById(clothingId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothingService.deleteClothing(clothingId)).
                    isInstanceOf(ClothingNotFoundException.class);
            verify(clothingRepository).findById(clothingId);
            verify(clothingRepository, never()).delete(any());
        }
    }


    // 편의를 위한 ID 세팅 헬퍼 (BaseEntity.id에 리플렉션 접근)
    private void setId(Object entity, UUID id) throws Exception {
        Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    private BinaryContent existingBinWithUrl() throws Exception {
        UUID id = UUID.randomUUID();
        String url = "http://cdn.example.com/" + id;
        BinaryContent bin = new BinaryContent("old.jpg", 10L, "image/jpeg", BinaryContentUploadStatus.SUCCESS);
        setId(bin, id);
        bin.updateImageUrl(url);
        return bin;
    }
}