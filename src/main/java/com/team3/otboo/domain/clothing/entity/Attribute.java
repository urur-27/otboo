package com.team3.otboo.domain.clothing.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import jakarta.persistence.CascadeType;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name="attribute")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Attribute extends BaseEntity {
    @Column(nullable = false)
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

    public AttributeOption addOptionRet(String value) {
        AttributeOption option = AttributeOption.of(value, this);
        this.options.add(option);
        return option;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * 프론트로부터 받은 문자열 목록(newValues)을 기준으로
     * - 유지/복구: 기존 값 그대로 유지(비활성->활성 복구 가능)
     * - 추가: 새 옵션 생성
     * - 제거: 참조 0이면 하드 삭제, 참조 >0이면 soft delete
     */
    public void syncOptions(
            List<String> newValues,
            Function<UUID, Long> refCountProvider // optionId -> 참조 개수
    ) {
        // 정규화(공백 trim + lower)해서 비교
        Map<String, AttributeOption> byNormValue = this.options.stream()
                .collect(Collectors.toMap(
                        o -> normalize(o.getValue()),
                        Function.identity(),
                        (a, b) -> a
                ));

        // newValues 정리(공백/중복 제거)
        List<String> cleaned = newValues.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Attribute::dedupKey) // 같은 철자 중복 제거용 키
                .distinct()
                .map(k -> k.original)     // 다시 원본 보존 값 사용
                .toList();

        Set<AttributeOption> keep = new HashSet<>();

        // 추가/유지/복구
        for (String raw : cleaned) {
            String norm = normalize(raw);
            AttributeOption exist = byNormValue.get(norm);
            if (exist != null) {
                // 대소문자/공백만 다른 경우 rename
                if (!exist.getValue().equals(raw)) exist.updateValue(raw);
                exist.activate(); // 비활성 상태였다면 복구
                keep.add(exist);
            } else {
                // 신규 추가
                AttributeOption created = addOptionRet(raw);          // ⭐ 새로 만든 옵션 반환
                keep.add(created);
            }
        }

        // 제거(요청에 빠진 기존 옵션 처리)
        for (AttributeOption opt : new ArrayList<>(this.options)) {
            if (keep.contains(opt)) continue;

            // id 없는 건 신규이거나 아직 flush 전 상태 → 안전장치로 건너뜀
            if (opt.getId() == null) continue;

            long refs = refCountProvider.apply(opt.getId());
            if (refs > 0) {
                opt.deactivate();     // 사용 중 → 비활성화
            } else {
                this.options.remove(opt); // 미사용 → 실제 삭제(orphanRemoval)
            }
        }
    }

    public void replaceOptions(List<String> newValues) {
        this.options.clear(); // 기존 옵션 모두 제거됨 (orphanRemoval)
        newValues.forEach(this::addOption);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    // 중복 제거를 위해 원본/정규화 값을 함께 담는 작은 레코드
    private static DedupKey dedupKey(String s) {
        return new DedupKey(s, normalize(s));
    }
    private record DedupKey(String original, String norm) {}
}
