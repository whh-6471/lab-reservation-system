package com.campus.lab.entity;

import com.campus.lab.common.LabStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 实验室信息
 */
@Data
@Entity
@Table(name = "lab_info")
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "open_start", nullable = false)
    private LocalTime openStart;

    @Column(name = "open_end", nullable = false)
    private LocalTime openEnd;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    public String getOpenRangeText() {
        if (openStart == null || openEnd == null) {
            return "-";
        }
        return openStart + " - " + openEnd;
    }

    public String getStatusText() {
        return LabStatus.label(status);
    }

    public String getStatusBadge() {
        return LabStatus.badge(status);
    }

    public boolean isAvailable() {
        return LabStatus.AVAILABLE.equals(status);
    }
}
