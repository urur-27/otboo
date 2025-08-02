package com.team3.otboo.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "otboo.storage.type", havingValue = "s3")
public class S3ImageStorage implements ImageStorage{

    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucket;
    private final String baseUrl;


    @Value("${otboo.storage.s3.presigned-url-expiration:600}") // 기본값 10분
    private long presignedUrlExpirationSeconds;

    public S3ImageStorage(
            @Value("${otboo.storage.s3.access-key}") String accessKey,
            @Value("${otboo.storage.s3.secret-key}") String secretKey,
            @Value("${otboo.storage.s3.region}") String region,
            @Value("${otboo.storage.s3.bucket}") String bucket,
            @Value("${otboo.storage.s3.baseUrl}") String baseUrl
    ){
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
        this.baseUrl = baseUrl;
    }

    @Override
    public String upload(MultipartFile file) {
        // TODO: S3 업로드 구현 예정
        throw new UnsupportedOperationException("S3 업로드는 아직 구현되지 않았습니다.");
    }

    @Override
    public void delete(String imageUrl) {
        throw new UnsupportedOperationException("S3 삭제는 아직 구현되지 않았습니다.");
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        String key = binaryContentId.toString();
        try {
            S3Client s3Client = getS3Client();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            log.info("S3에 파일 업로드 성공: {}", key);

            return binaryContentId;
        } catch (S3Exception e) {
            log.error("S3에 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("S3에 파일 업로드 실패: " + key, e);
        }
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        String key = binaryContentId.toString();
        try {
            S3Client s3Client = getS3Client();

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            byte[] bytes = s3Client.getObjectAsBytes(request).asByteArray();
            return new ByteArrayInputStream(bytes);
        } catch (S3Exception e) {
            log.error("S3에서 파일 다운로드 실패: {}", e.getMessage());
            throw new NoSuchElementException("File with key " + key + " does not exist");
        }
    }

    private S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    @Override
    public String getPatch(UUID binaryContentId, String contentType) {
        String[] parts = contentType.split("/");
        return baseUrl.concat("/").concat(binaryContentId.toString()).concat(".").concat(parts[1]);
    }


}
