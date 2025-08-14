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

@ConditionalOnProperty(name = "otboo.storage.type", havingValue = "local")
@Component
public class LocalImageStorage implements ImageStorage {

    private final Path root;

    public LocalImageStorage(
            @Value("${otboo.storage.local.root-path}") String rootProp) {
        Path r = Paths.get(rootProp);
        if (!r.isAbsolute()) {
            r = Paths.get(System.getProperty("user.dir")).resolve(r).toAbsolutePath();
        }
        this.root = r.normalize();
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
    public void delete(UUID id) {
            throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED, "로컬 이미지 삭제 로직 구현되지 않음" );
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
        String ext = toExt(contentType); // image/jpeg -> jpg 등
        Path base = resolvePath(binaryContentId);                 // root/{uuid}
        Path finalFile = base.resolveSibling(base.getFileName() + "." + ext); // root/{uuid}.jpg

        try {
            // 최초 호출 시 확장자 없는 파일을 확장자 있는 파일명으로 이동
            if (Files.exists(base) && Files.notExists(finalFile)) {
                Files.move(base, finalFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일명 변경 실패", e);
        }

        // 웹에서 접근 가능한 URL (/uploads/**)
        Path rel = root.relativize(finalFile);
        return "/uploads/" + rel.toString().replace("\\", "/");
    }

    private String toExt(String contentType) {
        if (contentType == null) return "bin";
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            default -> "bin";
        };
    }

    private Path resolvePath(UUID key) {
        return root.resolve(key.toString());
    }

}
