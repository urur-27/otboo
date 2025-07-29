package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothing extends BaseEntity {

    @Column(nullable = false)
    private String name;
    private String imageUrl;
    private String purchaseUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "clothing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClothingAttributeValue> attributeValues = new ArrayList<>();

    @Builder
    public Clothing(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public void addAttributeValue(ClothingAttributeValue attributeValue) {
        this.attributeValues.add(attributeValue);
        attributeValue.setClothing(this);
    }
}
