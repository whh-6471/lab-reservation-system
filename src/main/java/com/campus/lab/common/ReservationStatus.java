package com.campus.lab.common;

import java.util.List;

/**
 * 预约状态常量、中文名称与页面样式
 */
public final class ReservationStatus {

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";
    public static final String EXPIRED = "EXPIRED";

    /** 会占用实验室时段的状态 */
    public static final List<String> ACTIVE = List.of(PENDING, APPROVED);

    private ReservationStatus() {
    }

    public static String label(String status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case PENDING -> "待审核";
            case APPROVED -> "已通过";
            case REJECTED -> "已驳回";
            case CANCELLED -> "已取消";
            case EXPIRED -> "已过期";
            default -> "未知";
        };
    }

    public static String badge(String status) {
        if (status == null) {
            return "badge";
        }
        return switch (status) {
            case PENDING -> "badge badge-pending";
            case APPROVED -> "badge badge-approved";
            case REJECTED -> "badge badge-rejected";
            case CANCELLED -> "badge badge-cancelled";
            case EXPIRED -> "badge badge-expired";
            default -> "badge";
        };
    }
}
