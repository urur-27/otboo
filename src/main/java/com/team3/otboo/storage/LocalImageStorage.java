package com.team3.otboo.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
// 현재는 프로필 설정이 적용되어 있지 않음
//@Profile("local")
public class LocalImageStorage implements ImageStorage {

    private final Path rootPath = Paths.get("uploads");

    public LocalImageStorage() throws IOException {
        Files.createDirectories(rootPath);
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = rootPath.resolve(filename);
            file.transferTo(targetPath.toFile()); // 저장
            return "/uploads/" + filename; // 서버에 따라 변경
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }
}
