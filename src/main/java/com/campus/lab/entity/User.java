package com.campus.lab.entity;

import com.campus.lab.common.Role;
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
 * 系统用户（学生 / 教师 / 管理员）
 */
@Data
@Entity
@Table(name = "sys_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "student_no", length = 50)
    private String studentNo;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;

    public String getRoleText() {
        return Role.label(role);
    }

    public String getStatusText() {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    public String getCreateTimeText() {
        return createTime == null ? "-" : createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
