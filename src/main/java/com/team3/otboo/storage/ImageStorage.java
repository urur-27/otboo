package com.team3.otboo.storage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface ImageStorage {
    String upload(MultipartFile file);

    void delete(String imageUrl);

    UUID put(UUID binaryContentId, byte[] bytes);

    InputStream get(UUID binaryContentId);

    String getPatch(UUID binaryContentId, String contentType);

}