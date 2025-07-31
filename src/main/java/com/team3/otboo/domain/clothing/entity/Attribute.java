package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Attribute extends BaseEntity {
    private String name; // 예: 스타일, 컬러, 촉감 등

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeOption> options = new ArrayList<>();

    public static Attribute of(String name, List<String> values) {
        Attribute attribute = new Attribute();
        attribute.name = name;

        values.forEach(attribute::addOption);
        return attribute;
    }

    public void addOption(String value) {
        AttributeOption option = AttributeOption.of(value, this);
        this.options.add(option);
    }

    public void updateName(String newName) {
        this.name = newName;
    }

    public void replaceOptions(List<String> newValues) {
        this.options.clear(); // 기존 옵션 모두 제거됨 (orphanRemoval)
        newValues.forEach(this::addOption);
    }
}
