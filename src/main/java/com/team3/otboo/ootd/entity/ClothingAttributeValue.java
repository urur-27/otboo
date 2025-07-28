package com.team3.otboo.ootd.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ClothingAttributeValue {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Clothing clothing; // 어떤 의상에 대한 값인가?

    @ManyToOne(fetch = FetchType.LAZY)
    private Attribute attribute; // 속성 - 색상, 사이즈

    @ManyToOne(fetch = FetchType.LAZY)
    private AttributeOption option; // 상세옵션 - 검은색, L
}
