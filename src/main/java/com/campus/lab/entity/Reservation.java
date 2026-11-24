package com.campus.lab.entity;

import com.campus.lab.common.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 实验室预约记录
 */
@Data
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lab_id", nullable = false)
    private Long labId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    @Column(name = "applicant_role", nullable = false, length = 20)
    private String applicantRole;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "purpose", nullable = false, length = 500)
    private String purpose;

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "audit_user_id")
    private Long auditUserId;

    @Column(name = "audit_user_name", length = 50)
    private String auditUserName;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    /** 列表展示用：实验室名称与位置（不映射数据库列） */
    @Transient
    private String labName;

    @Transient
    private String labLocation;

    public String getReservationDateText() {
        return reservationDate == null ? "-" : reservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getStartTimeText() {
        return startTime == null ? "-" : startTime.toString();
    }

    public String getEndTimeText() {
        return endTime == null ? "-" : endTime.toString();
    }

    public String getTimeRangeText() {
        if (startTime == null || endTime == null) {
            return "-";
        }
        return startTime + " - " + endTime;
    }

    public String getDurationText() {
        if (startTime == null || endTime == null) {
            return "-";
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes % 60 == 0) {
            return (minutes / 60) + " 小时";
        }
        return String.format("%.1f 小时", minutes / 60.0);
    }

    public String getStatusText() {
        return ReservationStatus.label(status);
    }

    public String getApplicantRoleText() {
        return com.campus.lab.common.Role.label(applicantRole);
    }

    public String getStatusBadge() {
        return ReservationStatus.badge(status);
    }

    public String getCreateTimeText() {
        return createTime == null ? "-" : createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getAuditTimeText() {
        return auditTime == null ? "-" : auditTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /** 是否已开始，用于判断能否取消 */
    public boolean isStarted() {
        if (reservationDate == null || startTime == null) {
            return true;
        }
        return !LocalDateTime.of(reservationDate, startTime).isAfter(LocalDateTime.now());
    }
}
