package com.campus.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统预约规则配置（单条记录）
 */
@Data
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "max_duration_hours", nullable = false)
    private Integer maxDurationHours;

    @Column(name = "advance_days", nullable = false)
    private Integer advanceDays;

    @Column(name = "booking_rules", columnDefinition = "TEXT")
    private String bookingRules;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;
}
