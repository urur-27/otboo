package com.team3.otboo.domain.clothing.service;

public enum Direction {
    ASC, DESC;

    public static Direction fromApi(String apiValue) {
        return switch (apiValue.toUpperCase()) {
            case "ASCENDING" -> ASC;
            case "DESCENDING" -> DESC;
            default -> throw new IllegalArgumentException("정렬 방향 오류: " + apiValue);
        };
    }

    // QueryDSL/JPA용으로 변환할 때
    public boolean isAscending() {
        return this == ASC;
    }
}