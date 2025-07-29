package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeOption extends BaseEntity {

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @Builder
    public AttributeOption(String value, Attribute attribute) {
        this.value = value;
        this.attribute = attribute;
    }

    // 연관관계 편의 메서드를 위한 package-private setter
    void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }
}
