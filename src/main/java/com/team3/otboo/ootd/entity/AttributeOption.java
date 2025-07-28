package com.team3.otboo.ootd.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class AttributeOption {
    @Id
    @GeneratedValue
    private Long id;

    private String value; // 예: 블랙, S, 캐주얼

    @ManyToOne(fetch = FetchType.LAZY)
    private Attribute attribute;
}
