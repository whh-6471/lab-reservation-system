package com.campus.lab.entity;

import com.campus.lab.common.EquipmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 实验室设备台账
 */
@Data
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lab_id", nullable = false)
    private Long labId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    /** 列表展示用：所属实验室名称 */
    @Transient
    private String labName;

    public String getStatusText() {
        return EquipmentStatus.label(status);
    }

    public String getStatusBadge() {
        return EquipmentStatus.badge(status);
    }

    public String getPurchaseDateText() {
        return purchaseDate == null ? "-" : purchaseDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
