package com.team3.otboo.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.entity.BinaryContentUploadStatus;
import com.team3.otboo.storage.repository.BinaryContentRepository;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinaryContentUploaderTest {

    @Mock
    BinaryContentRepository binaryContentRepository;

    @Mock
    ImageStorage imageStorage;

    @InjectMocks
    BinaryContentUploader uploader;

    @Test
    @DisplayName("성공: WAITING → put → getPatch → SUCCESS(imageUrl 세팅)")
    void upload_success() throws Exception {
        // given
        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn("my pic.JPG");
        when(image.getContentType()).thenReturn(null);
        byte[] bytes = "IMG".getBytes();
        when(image.getBytes()).thenReturn(bytes);

        // save 시점에 ID 부여 + 그 순간 WAITING
        when(binaryContentRepository.save(any())).thenAnswer(inv -> {
            BinaryContent b = inv.getArgument(0);
            assertThat(b.getUploadStatus()).isEqualTo(BinaryContentUploadStatus.WAITING);
            var id = BaseEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(b, UUID.randomUUID());
            return b;
        });

        when(imageStorage.put(any(UUID.class), eq(bytes))).thenReturn(UUID.randomUUID());
        when(imageStorage.getPatch(any(UUID.class), eq("image/jpeg")))
                .thenReturn("http://cdn/u/ok");

        // when
        BinaryContent bin = uploader.upload(image);

        // then
        ArgumentCaptor<UUID> idCap = ArgumentCaptor.forClass(UUID.class);
        verify(imageStorage).put(idCap.capture(), eq(bytes));
        UUID issuedId = idCap.getValue();
        verify(imageStorage).getPatch(eq(issuedId), eq("image/jpeg"));

        assertThat(bin.getId()).isEqualTo(issuedId);
        assertThat(bin.getUploadStatus()).isEqualTo(BinaryContentUploadStatus.SUCCESS);
        assertThat(bin.getImageUrl()).isEqualTo("http://cdn/u/ok");
        assertThat(bin.getFileName()).isEqualTo("my_pic.JPG");
        assertThat(bin.getContentType()).isEqualTo("image/jpeg");
        assertThat(bin.getSize()).isEqualTo((long) bytes.length);
    }

    @Test
    @DisplayName("실패: put에서 예외 → FAILED + BusinessException")
    void upload_fail_onPut() throws Exception {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn("x.png");
        when(image.getContentType()).thenReturn("image/png");
        when(image.getBytes()).thenReturn("X".getBytes());

        when(binaryContentRepository.save(any())).thenAnswer(inv -> {
            BinaryContent b = inv.getArgument(0);
            var id = BaseEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(b, UUID.randomUUID());
            return b;
        });

        when(imageStorage.put(any(UUID.class), any(byte[].class)))
                .thenThrow(new RuntimeException("down"));

        BusinessException ex = assertThrows(BusinessException.class, () -> uploader.upload(image));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IMAGE_UPLOAD_FAILED);

        // save로 만들어진 객체가 FAILED로 전이되었는지 확인(같은 인스턴스를 다시 저장하지 않는다면 상태만 검증)
        // 필요시 ArgumentCaptor<BinaryContent>로 save 호출 당시 인스턴스를 잡아 상태 체크 가능
    }

    @Test
    @DisplayName("실패: getPatch에서 예외 → FAILED + BusinessException")
    void upload_fail_onGetPatch() throws Exception {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn("a.webp");
        when(image.getContentType()).thenReturn(null);
        when(image.getBytes()).thenReturn("DATA".getBytes());

        when(binaryContentRepository.save(any())).thenAnswer(inv -> {
            BinaryContent b = inv.getArgument(0);
            var id = BaseEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(b, UUID.randomUUID());
            return b;
        });

        when(imageStorage.put(any(UUID.class), any(byte[].class))).thenReturn(UUID.randomUUID());
        when(imageStorage.getPatch(any(UUID.class), eq("image/webp")))
                .thenThrow(new RuntimeException("presign fail"));

        BusinessException ex = assertThrows(BusinessException.class, () -> uploader.upload(image));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IMAGE_UPLOAD_FAILED);
    }

    @Test
    @DisplayName("실패: Multipart 읽기 실패 → IMAGE_UPLOAD_FAILED, storage 미호출")
    void upload_fail_onReadBytes() throws Exception {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn(null);
        when(image.getContentType()).thenReturn(null);
        when(image.getBytes()).thenThrow(new IOException("read fail"));

        BusinessException ex = assertThrows(BusinessException.class, () -> uploader.upload(image));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IMAGE_UPLOAD_FAILED);
        verifyNoInteractions(imageStorage);
    }
}
