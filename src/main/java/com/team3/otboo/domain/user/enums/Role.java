package com.team3.otboo.domain.user.enums;

public enum Role {
    USER, ADMIN;

    public static boolean contains(Role newRole) {
        for (Role role : Role.values()) {
            if (role.equals(newRole)) {
                return true;
            }
        }
        return false;
    }

}
