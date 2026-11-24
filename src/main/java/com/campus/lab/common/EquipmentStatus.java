package com.campus.lab.common;

/**
 * 设备状态常量
 */
public final class EquipmentStatus {

    public static final String NORMAL = "NORMAL";
    public static final String REPAIRING = "REPAIRING";
    public static final String SCRAPPED = "SCRAPPED";

    private EquipmentStatus() {
    }

    public static String label(String status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case NORMAL -> "正常";
            case REPAIRING -> "维修中";
            case SCRAPPED -> "已报废";
            default -> "未知";
        };
    }

    public static String badge(String status) {
        if (status == null) {
            return "badge";
        }
        return switch (status) {
            case NORMAL -> "badge badge-approved";
            case REPAIRING -> "badge badge-pending";
            case SCRAPPED -> "badge badge-cancelled";
            default -> "badge";
        };
    }

    public static boolean isValid(String status) {
        return NORMAL.equals(status) || REPAIRING.equals(status) || SCRAPPED.equals(status);
    }
}
