package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingAttributeValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clothing_id")
    private Clothing clothing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id")
    private AttributeOption option;

    @Builder
    public ClothingAttributeValue(Clothing clothing, Attribute attribute, AttributeOption option) {
        this.clothing = clothing;
        this.attribute = attribute;
        this.option = option;
    }

    // 연관관계 편의 메서드를 위한 package-private setter
    void setClothing(Clothing clothing) {
        this.clothing = clothing;
    }
}
