package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class AttributeOption extends BaseEntity {

    private String value; // 예: 블랙, S, 캐주얼

    @ManyToOne(fetch = FetchType.LAZY)
    private Attribute attribute;
}
