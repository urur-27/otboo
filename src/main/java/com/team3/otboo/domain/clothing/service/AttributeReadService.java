package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttributeReadService {

    private final AttributeRepository attributeRepository;

    @Cacheable(cacheNames = "attrSnapshot", key = "'v1'")
    @Transactional(readOnly = true)
    public AttributeSnapshot loadAllForExtraction() {
        List<Attribute> all = attributeRepository.findAllWithOptions();

        List<AttributeDefSnap> defs = all.stream()
                .map(a -> new AttributeDefSnap(
                        a.getId(),
                        a.getName(),
                        a.getOptions().stream()
                                .map(AttributeOption::getValue)
                                .filter(v -> v != null && !v.isBlank())
                                .map(String::trim)
                                .toList()
                ))
                .toList();

        Collator collator = Collator.getInstance(Locale.KOREAN);
        Map<String, List<String>> optionsByDef = new LinkedHashMap<>();
        for (AttributeDefSnap d : defs) {
            List<String> opts = d.options().stream()
                    .sorted(collator)
                    .limit(60)
                    .toList();
            optionsByDef.put(d.name(), opts);
        }

        return new AttributeSnapshot(defs, optionsByDef);
    }

    // Dto
    public record AttributeDefSnap(UUID id, String name, List<String> options) {}
    public record AttributeSnapshot(List<AttributeDefSnap> defs, Map<String, List<String>> optionsByDef) {}
}
