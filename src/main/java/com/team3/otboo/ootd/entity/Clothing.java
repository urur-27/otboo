package com.team3.otboo.ootd.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;

@Entity
public class Clothing {
    @Id
    @GeneratedValue
    private Long id;

    private String name; // 의상 이름
    private String imageUrl; // 이미지 저장 경로 or 외부 링크
    private String purchaseUrl; // 구매 링크

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner; // 의상을 등록한 사용자

    @OneToMany(mappedBy = "clothing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClothingAttributeValue> attributeValues = new ArrayList<>(); // 의상 속성
}