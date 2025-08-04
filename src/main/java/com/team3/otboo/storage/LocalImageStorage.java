package com.team3.otboo.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@ConditionalOnProperty(name = "otboo.storage.type", havingValue = "local")
@Component
public class LocalImageStorage implements ImageStorage {

    private final Path root;

    public LocalImageStorage(@Value("${otboo.storage.local.root-path}") Path root) {
        this.root = root;
    }

    @PostConstruct
    public void init() {
        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = root.resolve(filename);
            file.transferTo(targetPath.toFile()); // 저장

            return "/uploads/" + filename; // 서버에 따라 변경
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, e.getMessage());
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            // "/uploads/xxx.jpg" → "xxx.jpg" 추출
            String filename = Paths.get(imageUrl).getFileName().toString();
            Path filePath = root.resolve(filename);

            // uploads 디렉토리 내 파일만 삭제하도록 제한
            if (!filePath.normalize().startsWith(root.normalize())) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_PATH);
            }

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED, "이미지 삭제 실패: " + imageUrl);
        }
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        Path filePath = resolvePath(binaryContentId);
        if (Files.exists(filePath)) {
            throw new IllegalArgumentException("File with key " + binaryContentId + " already exists");
        }
        try (OutputStream outputStream = Files.newOutputStream(filePath)) {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return binaryContentId;
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        Path filePath = resolvePath(binaryContentId);
        if (Files.notExists(filePath)) {
            throw new NoSuchElementException("File with key " + binaryContentId + " does not exist");
        }
        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPatch(UUID binaryContentId, String contentType) {
        Path filePath = resolvePath(binaryContentId);
        String[] parts = contentType.split("/");
        return filePath.toString().concat(".").concat(parts[1]);
    }


    private Path resolvePath(UUID key) {
        return root.resolve(key.toString());
    }

}
