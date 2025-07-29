package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public void removeOption(UUID optionId) {
        this.options.removeIf(option -> option.getId().equals(optionId));
    }

    public void updateOptionValue(UUID optionId, String newValue) {
        this.options.stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .ifPresent(option -> option.updateValue(newValue));
    }
}
