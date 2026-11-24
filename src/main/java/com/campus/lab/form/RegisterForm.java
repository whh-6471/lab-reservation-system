package com.campus.lab.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学生注册表单
 */
@Data
public class RegisterForm {

    @NotBlank(message = "请输入登录账号")
    @Size(min = 3, max = 20, message = "账号长度需为 3-20 个字符")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 20, message = "密码长度需为 6-20 个字符")
    private String password;

    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

    @NotBlank(message = "请输入真实姓名")
    private String realName;

    private String studentNo;

    private String department;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
