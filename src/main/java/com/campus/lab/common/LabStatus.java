package com.campus.lab.common;

/**
 * 实验室状态常量
 */
public final class LabStatus {

    public static final String AVAILABLE = "AVAILABLE";
    public static final String MAINTENANCE = "MAINTENANCE";
    public static final String CLOSED = "CLOSED";

    private LabStatus() {
    }

    public static String label(String status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case AVAILABLE -> "开放中";
            case MAINTENANCE -> "维护中";
            case CLOSED -> "已停用";
            default -> "未知";
        };
    }

    public static String badge(String status) {
        if (status == null) {
            return "badge";
        }
        return switch (status) {
            case AVAILABLE -> "badge badge-approved";
            case MAINTENANCE -> "badge badge-pending";
            case CLOSED -> "badge badge-cancelled";
            default -> "badge";
        };
    }

    public static boolean isValid(String status) {
        return AVAILABLE.equals(status) || MAINTENANCE.equals(status) || CLOSED.equals(status);
    }
}
