//package com.team3.otboo.domain.clothing.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.anyInt;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.team3.otboo.domain.clothing.dto.ClothesAttributeDefDto;
//import com.team3.otboo.domain.clothing.dto.request.ClothesAttributeDefCreateRequest;
//import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
//import com.team3.otboo.domain.clothing.entity.Attribute;
//import com.team3.otboo.domain.clothing.mapper.ClothingAttributeDefMapper;
//import com.team3.otboo.domain.clothing.repository.AttributeRepository;
//import com.team3.otboo.fixture.ClothingAttributeDefFixture;
//import com.team3.otboo.global.exception.attribute.AttributeNotFoundException;
//import com.team3.otboo.global.exception.attribute.AttributeOptionEmptyException;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//public class ClothingAttributeDefServiceTest {
//
//    @InjectMocks
//    private ClothingAttributeDefServiceImpl service;
//
//    @Mock
//    private AttributeRepository attributeRepository;
//
//    @Mock
//    private ClothingAttributeDefMapper mapper;
//
//    @Nested
//    @DisplayName("속성 등록 테스트")
//    class CreateTest {
//
//        @Test
//        @DisplayName("create 성공")
//        void create_success() {
//            // given
//            ClothesAttributeDefCreateRequest request = ClothingAttributeDefFixture.sampleCreateRequest();
//            Attribute attribute = ClothingAttributeDefFixture.sampleAttribute();
//
//            when(attributeRepository.existsByName(request.name())).thenReturn(false);
//            when(mapper.toEntity(request)).thenReturn(attribute);
//            when(attributeRepository.save(attribute)).thenReturn(attribute);
//            when(mapper.toDto(attribute)).thenReturn(
//                    ClothingAttributeDefFixture.sampleDto(attribute.getId()));
//
//            // when
//            ClothesAttributeDefDto result = service.create(request);
//
//            // then
//            assertThat(result.name()).isEqualTo("색상");
//            verify(attributeRepository).save(attribute);
//        }
//
//        @Test
//        @DisplayName("create 실패 - 이미 존재하는 이름으로 생성 시 예외 발생")
//        void create_fail_whenDuplicateName() {
//            // given
//            ClothesAttributeDefCreateRequest request = ClothingAttributeDefFixture.sampleCreateRequest();
//
//            when(attributeRepository.existsByName(request.name())).thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> service.create(request))
//                    .isInstanceOf(AttributeNotFoundException.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("속성 목록 조회 테스트")
//    class GetAttributesTest {
//
//        @Test
//        @DisplayName("getAttributes 성공")
//        void getAttributes_success() {
//            // given
//            Attribute attr = ClothingAttributeDefFixture.sampleAttribute();
//            UUID id = ClothingAttributeDefFixture.definitionId();
//            ClothesAttributeDefDto dto = ClothingAttributeDefFixture.sampleDto(id);
//
//            CursorPageResponse<Attribute> response = ClothingAttributeDefFixture.samplePage(attr);
//
//            when(attributeRepository.findAllByCursor(any(), any(), anyInt(), any(), any(), any()))
//                    .thenReturn(response);
//
//            when(mapper.toDto(attr)).thenReturn(dto);
//
//            // when
//            CursorPageResponse<ClothesAttributeDefDto> result = service.getAttributes(
//                    "cursor", UUID.randomUUID(), 10, "name", Direction.ASC, "색상"
//            );
//
//            // then
//            assertThat(result.data()).hasSize(1);
//            assertThat(result.data().getFirst().name()).isEqualTo("색상");
//            assertThat(result.hasNext()).isFalse();
//
//            verify(attributeRepository).findAllByCursor(any(), any(), anyInt(), any(), any(),
//                    any());
//            verify(mapper).toDto(attr);
//        }
//
//        @Test
//        @DisplayName("getAttributes 성공 - 결과 없음")
//        void getAttributes_success_emptyResult() {
//            // given
//            CursorPageResponse<Attribute> emptyResponse = new CursorPageResponse<>(
//                    List.of(),
//                    null,
//                    null,
//                    "name",
//                    "ASC",
//                    0L,
//                    false
//            );
//
//            when(attributeRepository.findAllByCursor(any(), any(), anyInt(), any(), any(), any()))
//                    .thenReturn(emptyResponse);
//
//            // when
//            CursorPageResponse<ClothesAttributeDefDto> result = service.getAttributes(
//                    null, null, 10, "name", Direction.ASC, null
//            );
//
//            // then
//            assertThat(result.data()).isEmpty();
//            assertThat(result.totalCount()).isEqualTo(0L);
//            assertThat(result.hasNext()).isFalse();
//
//            verify(attributeRepository).findAllByCursor(any(), any(), anyInt(), any(), any(),
//                    any());
//        }
//
//    }
//
//    @Nested
//    @DisplayName("속성 삭제 테스트")
//    class DeleteAttributeTest {
//
//        @Test
//        @DisplayName("deleteAttribute 성공")
//        void deleteAttribute_success() {
//            // given
//            UUID id = ClothingAttributeDefFixture.definitionId();
//            Attribute attribute = ClothingAttributeDefFixture.sampleAttribute();
//
//            when(attributeRepository.findById(id)).thenReturn(Optional.of(attribute));
//
//            // when
//            service.deleteAttribute(id);
//
//            // then
//            verify(attributeRepository).delete(attribute);
//        }
//
//        @Test
//        @DisplayName("deleteAttribute 실패 - 존재하지 않는 ID")
//        void deleteAttribute_fail_whenNotFound() {
//            // given
//            UUID id = ClothingAttributeDefFixture.definitionId();
//            when(attributeRepository.findById(id)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> service.deleteAttribute(id))
//                    .isInstanceOf(AttributeNotFoundException.class);
//
//            verify(attributeRepository, never()).delete(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("속성 업데이트 테스트")
//    class UpdateAttributeTest {
//
//        @Test
//        @DisplayName("updateAttribute 성공")
//        void update_success() {
//            // given
//            UUID definitionId = ClothingAttributeDefFixture.definitionId();
//            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
//                    "스타일", List.of("캐주얼", "포멀")
//            );
//            Attribute attribute = ClothingAttributeDefFixture.sampleAttribute();
//
//            when(attributeRepository.findById(definitionId)).thenReturn(Optional.of(attribute));
//            when(mapper.toDto(attribute)).thenReturn(
//                    new ClothesAttributeDefDto(definitionId, "스타일", List.of("캐주얼", "포멀"))
//            );
//
//            // when
//            ClothesAttributeDefDto result = service.updateAttribute(definitionId, request);
//
//            // then
//            assertThat(result.name()).isEqualTo("스타일");
//            assertThat(result.selectableValues()).containsExactly("캐주얼", "포멀");
//            verify(attributeRepository).findById(definitionId);
//        }
//
//        @Test
//        @DisplayName("updateAttribute 실패 - 존재하지 않는 속성 ID")
//        void update_fail_whenAttributeNotFound() {
//            // given
//            UUID definitionId = ClothingAttributeDefFixture.definitionId();
//            ClothesAttributeDefCreateRequest request = ClothingAttributeDefFixture.sampleCreateRequest();
//
//            when(attributeRepository.findById(definitionId)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> service.updateAttribute(definitionId, request))
//                    .isInstanceOf(AttributeNotFoundException.class);
//        }
//
//        @Test
//        @DisplayName("updateAttribute 실패 - 옵션 리스트가 비어 있음")
//        void update_fail_whenEmptyOptions() {
//            // given
//            UUID definitionId = ClothingAttributeDefFixture.definitionId();
//            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("스타일",
//                    List.of());
//            Attribute attribute = ClothingAttributeDefFixture.sampleAttribute();
//
//            when(attributeRepository.findById(definitionId)).thenReturn(Optional.of(attribute));
//
//            // when & then
//            assertThatThrownBy(() -> service.updateAttribute(definitionId, request))
//                    .isInstanceOf(AttributeOptionEmptyException.class);
//        }
//    }
//}
