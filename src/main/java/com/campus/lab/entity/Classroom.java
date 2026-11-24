package com.campus.lab.entity;

import com.campus.lab.common.ClassroomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教学楼教室（用于空教室查询）
 */
@Data
@Entity
@Table(name = "classroom")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 楼号，例如 1 表示一号教学楼 */
    @Column(name = "building_no", nullable = false)
    private Integer buildingNo;

    @Column(name = "building_name", nullable = false, length = 50)
    private String buildingName;

    /** 楼层，1~5 */
    @Column(name = "floor_no", nullable = false)
    private Integer floorNo;

    /** 楼层内序号，1~20 */
    @Column(name = "room_no", nullable = false)
    private Integer roomNo;

    /** 教室编号，例如 1101 */
    @Column(name = "room_code", nullable = false, unique = true, length = 20)
    private String roomCode;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "media_type", length = 30)
    private String mediaType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    public String getStatusText() {
        return ClassroomStatus.label(status);
    }

    public String getStatusBadge() {
        return ClassroomStatus.badge(status);
    }

    public String getFloorText() {
        return floorNo == null ? "-" : floorNo + " 层";
    }
}
