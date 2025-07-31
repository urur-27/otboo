package com.team3.otboo.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("prod")
public class S3ImageStorage implements ImageStorage{
    // 실제 S3Client, 환경변수 등은 나중에 주입/설정
    // private final S3Client s3Client; (예정)

    @Override
    public String upload(MultipartFile file) {
        // TODO: S3 업로드 구현 예정
        throw new UnsupportedOperationException("S3 업로드는 아직 구현되지 않았습니다.");
    }

    @Override
    public void delete(String imageUrl) {
        throw new UnsupportedOperationException("S3 삭제는 아직 구현되지 않았습니다.");
    }
}
