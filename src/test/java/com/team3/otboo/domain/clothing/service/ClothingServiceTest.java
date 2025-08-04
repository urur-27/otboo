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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.team3.otboo.fixture.ClothingFixture;

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
    private UserRepository userRepository;

    @Nested
    @DisplayName("의상 등록 테스트")
    class RegisterClothingTest {
        @Test
        @DisplayName("registerClothing 성공 테스트")
        void registerClothing_success() {
            // given
            MultipartFile image = mock(MultipartFile.class);
            String imageUrl = "http://mocked-image-url.com";
            UUID attributeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID clothingId = UUID.randomUUID();

            User mockUser = mock(User.class);
            ClothingCreateRequest request = ClothingFixture.sampleCreateRequest(attributeId);
            Attribute mockAttribute = mock(Attribute.class);
            when(mockAttribute.getId()).thenReturn(attributeId);
            AttributeOption mockOption = mock(AttributeOption.class);
            Clothing mockClothing = mock(Clothing.class);
            ClothingDto expectedDto = ClothingFixture.sampleExpectedDto(clothingId, userId, attributeId, imageUrl);

            // stub 정의
            when(imageStorage.upload(image)).thenReturn(imageUrl);
            when(clothingMapper.toEntity(request)).thenReturn(mockClothing);
            when(clothingMapper.toDto(mockClothing)).thenReturn(expectedDto);
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(mockAttribute));
            when(attributeOptionRepository.findByAttributeIdAndValue(attributeId, "청색")).thenReturn(Optional.of(mockOption));

            // when
            ClothingDto result = clothingService.registerClothing(mockUser, request, image);

            // then
            verify(clothingRepository).save(mockClothing);
            verify(imageStorage).upload(image);
            verify(attributeRepository).findById(attributeId);
            verify(attributeOptionRepository).findByAttributeIdAndValue(attributeId, "청색");
            verify(clothingMapper).toDto(mockClothing);
            verify(mockClothing).updateOwner(mockUser);
            verify(mockClothing).updateImageUrl(imageUrl);

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
            ClothingCreateRequest request = ClothingFixture.sampleCreateRequest(UUID.randomUUID());
            User user = mock(User.class);

            when(imageStorage.upload(image)).thenThrow(new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED));

            // when & then
            assertThatThrownBy(() -> clothingService.registerClothing(user, request, image))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.IMAGE_UPLOAD_FAILED.getMessage());

            verify(imageStorage).upload(image);
            verifyNoMoreInteractions(clothingRepository);
        }

        @Test
        @DisplayName("registerClothing 실패 테스트 - Attribute가 없는 경우")
        void registerClothing_fail_whenAttributeNotFound() {
            // given
            MultipartFile image = mock(MultipartFile.class);
            UUID attributeId = UUID.randomUUID();
            ClothingCreateRequest request = ClothingFixture.sampleCreateRequest(attributeId);
            User user = mock(User.class);

            String imageUrl = "http://mocked-image-url.com";
            Clothing mockClothing = mock(Clothing.class);
            // 이전 로직
            when(imageStorage.upload(image)).thenReturn(imageUrl);
            when(clothingMapper.toEntity(request)).thenReturn(mockClothing);

            // 존재하지 않는 attributeId 처리
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothingService.registerClothing(user, request, image))
                    .isInstanceOf(AttributeNotFoundException.class);

            verify(attributeRepository).findById(attributeId);
            verifyNoMoreInteractions(attributeOptionRepository, clothingRepository);
        }

        @Test
        @DisplayName("registerClothing 실패 테스트 - AttributeOption이 없는 경우")
        void registerClothing_fail_whenAttributeOptionNotFound() {
            // given
            MultipartFile image = mock(MultipartFile.class);
            UUID attributeId = UUID.randomUUID();
            User user = mock(User.class);
            ClothingCreateRequest request = ClothingFixture.sampleCreateRequest(attributeId);

            Attribute mockAttribute = mock(Attribute.class);
            when(mockAttribute.getId()).thenReturn(attributeId);
            when(mockAttribute.getId()).thenReturn(attributeId);
            Clothing mockClothing = mock(Clothing.class);

            String imageUrl = "http://mocked-image-url.com";
            when(imageStorage.upload(image)).thenReturn(imageUrl);
            when(clothingMapper.toEntity(request)).thenReturn(mockClothing);
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(mockAttribute));

            // 옵션이 존재하지 않는 경우를 설정
            when(attributeOptionRepository.findByAttributeIdAndValue(attributeId, "청색"))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothingService.registerClothing(user, request, image))
                    .isInstanceOf(AttributeOptionNotFoundException.class);

            // verify
            verify(attributeRepository).findById(attributeId);
            verify(attributeOptionRepository).findByAttributeIdAndValue(attributeId, "청색");
            verifyNoMoreInteractions(clothingRepository); // 저장 안 돼야 함
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
            ClothingDto dto1 = mock(ClothingDto.class);
            ClothingDto dto2 = mock(ClothingDto.class);
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
        void updateClothing_success() {
            // given
            UUID clothingId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();

            Clothing clothing = mock(Clothing.class);
            Attribute attribute = mock(Attribute.class);
            AttributeOption option = mock(AttributeOption.class);
            MultipartFile image = new MockMultipartFile("image", "shirt.jpg", "image/jpeg", "data".getBytes());

            ClothingUpdateRequest request = ClothingFixture.sampleUpdateRequest(attributeId);
            ClothingDto expectedDto = ClothingFixture.sampleUpdatedDto(clothingId, userId, attributeId, "/uploads/shirt.jpg");

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(imageStorage.upload(any())).thenReturn("/uploads/shirt.jpg");
            when(attributeRepository.findById(attributeId)).thenReturn(Optional.of(attribute));
            when(attributeOptionRepository.findByAttributeAndValue(attribute, "흰색")).thenReturn(Optional.of(option));
            when(clothingMapper.toDto(clothing)).thenReturn(expectedDto);

            // when
            ClothingDto result = clothingService.updateClothing(clothingId, request, image);

            // then
            assertThat(result).isEqualTo(expectedDto);
            verify(clothing).updateName("셔츠");
            verify(clothing).updateType("TOP");
            verify(clothing).updateImageUrl("/uploads/shirt.jpg");
            verify(clothingRepository).findById(clothingId);
            verify(imageStorage).upload(image);
        }

        @DisplayName("updateClothing 성공 테스트 - 이미지와 속성 없이 이름과 타입만 수정")
        @Test
        void updateClothing_onlyNameAndTypeChanged() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = mock(Clothing.class);

            ClothingUpdateRequest request = new ClothingUpdateRequest("셔츠", "TOP", null);
            MultipartFile image = null;

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(clothingMapper.toDto(clothing)).thenReturn(mock(ClothingDto.class));

            // when
            ClothingDto result = clothingService.updateClothing(clothingId, request, image);

            // then
            verify(clothing).updateName("셔츠");
            verify(clothing).updateType("TOP");
            verifyNoInteractions(imageStorage);
            verify(clothingMapper).toDto(clothing);
        }

        @DisplayName("updateClothing 성공 테스트 - 이미지만 수정 (기존 이미지 존재)")
        @Test
        void updateClothing_onlyImageChanged_withOldImage() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = mock(Clothing.class);
            MultipartFile image = new MockMultipartFile("image", "shirt.jpg", "image/jpeg", "data".getBytes());

            when(clothing.getImageUrl()).thenReturn("/uploads/old.jpg");

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(imageStorage.upload(any())).thenReturn("/uploads/shirt.jpg");
            when(clothingMapper.toDto(clothing)).thenReturn(mock(ClothingDto.class));

            // when
            ClothingDto result = clothingService.updateClothing(clothingId, new ClothingUpdateRequest(null, null, null), image);

            // then
            verify(imageStorage).delete("/uploads/old.jpg");
            verify(imageStorage).upload(image);
            verify(clothing).updateImageUrl("/uploads/shirt.jpg");
        }

        @DisplayName("updateClothing 성공 테스트 - 이미지 파일이 비어 있을 때 이미지 변경 무시")
        @Test
        void updateClothing_skipImageUpdateWhenEmpty() {
            // given
            UUID clothingId = UUID.randomUUID();
            Clothing clothing = mock(Clothing.class);

            MultipartFile emptyImage = new MockMultipartFile("image", "", "image/jpeg", new byte[0]); // isEmpty = true

            when(clothingRepository.findById(clothingId)).thenReturn(Optional.of(clothing));
            when(clothingMapper.toDto(clothing)).thenReturn(mock(ClothingDto.class));

            // when
            ClothingDto result = clothingService.updateClothing(clothingId, new ClothingUpdateRequest(null, null, null), emptyImage);

            // then
            verify(imageStorage, never()).upload(any());
            verify(clothingMapper).toDto(clothing);
        }

        @DisplayName("updateClothing 실패 테스트 - 존재하지 않는 의상 ID로 수정 시 예외 발생")
        @Test
        void updateClothing_fail_whenClothingNotFound() {
            // given
            UUID clothingId = UUID.randomUUID();
            when(clothingRepository.findById(clothingId)).thenReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() ->
                    clothingService.updateClothing(clothingId, mock(ClothingUpdateRequest.class), null)
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
            ClothingUpdateRequest request = new ClothingUpdateRequest(
                    "셔츠", "TOP",
                    List.of(new ClothingAttributeDto(attributeId, "청색"))
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

            ClothingUpdateRequest request = new ClothingUpdateRequest(
                    "셔츠", "TOP",
                    List.of(new ClothingAttributeDto(attributeId, "청색"))
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
}