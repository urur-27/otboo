package com.team3.otboo.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    String upload(MultipartFile file);

    void delete(String imageUrl);
}