package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name="clothing_attribute_value")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingAttributeValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Clothing clothing; // 어떤 의상에 대한 값인가?

    @ManyToOne(fetch = FetchType.LAZY)
    private Attribute attribute; // 속성 - 색상, 사이즈

    @ManyToOne(fetch = FetchType.LAZY)
    private AttributeOption option; // 상세옵션 - 검은색, L

    public static ClothingAttributeValue of(Clothing clothing, Attribute attribute, AttributeOption option) {
        ClothingAttributeValue value = new ClothingAttributeValue();
        value.attribute = attribute;
        value.option = option;

        clothing.addAttributeValue(value);
        value.clothing = clothing;

        return value;
    }
}
