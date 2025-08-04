package com.team3.otboo.storage.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    @Column(length = 100, nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BinaryContentUploadStatus uploadStatus;

    @Column
    private String imageUrl;

    public BinaryContent(String fileName, Long size, String contentType, BinaryContentUploadStatus uploadStatus) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.uploadStatus = uploadStatus;
    }


    public void updateImageUrl(String newImageUrl) {
        this.imageUrl = newImageUrl;
    }
}
