package com.campus.lab.common;

/**
 * 系统角色常量与中文名称
 */
public final class Role {

    public static final String STUDENT = "STUDENT";
    public static final String TEACHER = "TEACHER";
    public static final String ADMIN = "ADMIN";

    private Role() {
    }

    public static String label(String role) {
        if (role == null) {
            return "未知";
        }
        return switch (role) {
            case STUDENT -> "学生";
            case TEACHER -> "教师";
            case ADMIN -> "管理员";
            default -> "未知";
        };
    }

    public static boolean isValid(String role) {
        return STUDENT.equals(role) || TEACHER.equals(role) || ADMIN.equals(role);
    }
}
