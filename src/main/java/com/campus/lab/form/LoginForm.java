package com.campus.lab.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录表单
 */
@Data
public class LoginForm {

    @NotBlank(message = "请输入账号")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;
}
