package com.team3.otboo.ootd.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;

@Entity
public class Attribute {
    @Id
    @GeneratedValue
    private Long id;

    private String name; // 예: 스타일, 컬러, 촉감 등

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeOption> options = new ArrayList<>();
}
