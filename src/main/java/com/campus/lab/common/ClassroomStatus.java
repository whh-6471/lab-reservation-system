package com.campus.lab.common;

/**
 * 教室状态常量
 */
public final class ClassroomStatus {

    public static final String FREE = "FREE";
    public static final String IN_USE = "IN_USE";
    public static final String MAINTENANCE = "MAINTENANCE";

    private ClassroomStatus() {
    }

    public static String label(String status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case FREE -> "空闲";
            case IN_USE -> "使用中";
            case MAINTENANCE -> "维护中";
            default -> "未知";
        };
    }

    public static String badge(String status) {
        if (status == null) {
            return "badge";
        }
        return switch (status) {
            case FREE -> "badge badge-approved";
            case IN_USE -> "badge badge-pending";
            case MAINTENANCE -> "badge badge-cancelled";
            default -> "badge";
        };
    }

    public static boolean isValid(String status) {
        return FREE.equals(status) || IN_USE.equals(status) || MAINTENANCE.equals(status);
    }
}
