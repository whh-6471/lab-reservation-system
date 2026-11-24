package com.campus.lab.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 个人资料表单
 */
@Data
public class ProfileForm {

    @NotBlank(message = "请输入真实姓名")
    private String realName;

    private String studentNo;

    private String department;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
