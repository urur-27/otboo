package com.team3.otboo.domain.user.enums;

public enum SortDirection {
    ASCENDING,   // 오름차순
    DESCENDING;  // 내림차순

    public boolean isAscending() {
        return this == ASCENDING;
    }
}