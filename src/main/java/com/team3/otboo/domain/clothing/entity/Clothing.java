package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import com.team3.otboo.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Clothing extends BaseEntity {

    private String name; // 의상 이름
    private String imageUrl; // 이미지 저장 경로 or 외부 링크
    private String purchaseUrl; // 구매 링크
    private String type; // 타입 (상의, 하의...)

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner; // 의상을 등록한 사용자

    @OneToMany(mappedBy = "clothing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClothingAttributeValue> attributeValues = new ArrayList<>(); // 의상 속성

    public static Clothing of(String name, User owner) {
        Clothing clothing = new Clothing();
        clothing.name = name;
        clothing.owner = owner;
        return clothing;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateOwner(User owner) {
        this.owner = owner;
    }

    public void updateName(String name) { this.name = name; }

    public void updateType(String type) { this.type = type; }

    public void addAttributeValue(ClothingAttributeValue attributeValue) {
        this.attributeValues.add(attributeValue);
    }
}
