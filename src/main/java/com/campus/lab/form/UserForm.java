package com.campus.lab.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员新增 / 编辑用户表单
 */
@Data
public class UserForm {

    private Long id;

    @NotBlank(message = "请输入登录账号")
    @Size(min = 3, max = 20, message = "账号长度需为 3-20 个字符")
    private String username;

    /** 新增时必填，编辑时留空表示不修改 */
    private String password;

    @NotBlank(message = "请输入真实姓名")
    private String realName;

    @NotBlank(message = "请选择角色")
    private String role;

    private String studentNo;

    private String department;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
