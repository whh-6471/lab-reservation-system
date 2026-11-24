package com.campus.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 预约审核记录
 */
@Data
@Entity
@Table(name = "audit_record")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "auditor_id", nullable = false)
    private Long auditorId;

    @Column(name = "auditor_name", nullable = false, length = 50)
    private String auditorName;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    public String getActionText() {
        return "APPROVE".equals(action) ? "审核通过" : "审核驳回";
    }

    public String getCreateTimeText() {
        return createTime == null ? "-" : createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
