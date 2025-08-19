package com.team3.otboo.storage;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.entity.BinaryContentUploadStatus;
import com.team3.otboo.storage.repository.BinaryContentRepository;
import java.io.IOException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class BinaryContentUploader {

    private final BinaryContentRepository binaryContentRepository;
    private final ImageStorage imageStorage;

    // BinaryContent 생성(WAITING) → S3 put(id, bytes) → getPath(id, contentType) → SUCCESS
    public BinaryContent upload(MultipartFile image) {
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
