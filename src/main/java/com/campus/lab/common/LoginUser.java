package com.campus.lab.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户信息，保存在 HttpSession 中
 */
@Data
public class LoginUser implements Serializable {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String department;

    public String getRoleText() {
        return Role.label(role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    public boolean isTeacher() {
        return Role.TEACHER.equals(role);
    }

    public boolean isStudent() {
        return Role.STUDENT.equals(role);
    }
}
