package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class AttributeOption extends BaseEntity {

    private String value; // 예: 블랙, S, 캐주얼

    @ManyToOne(fetch = FetchType.LAZY)
    private Attribute attribute;

    // 연관관계 설정
    public void assignAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public static AttributeOption of(String value, Attribute attribute) {
        AttributeOption option = new AttributeOption();
        option.value = value;
        option.assignAttribute(attribute);
        return option;
    }

    public void updateValue(String newValue) {
        this.value = newValue;
    }
}
