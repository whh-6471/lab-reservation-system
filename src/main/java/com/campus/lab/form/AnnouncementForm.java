package com.campus.lab.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公告新增 / 编辑表单
 */
@Data
public class AnnouncementForm {

    private Long id;

    @NotBlank(message = "请输入公告标题")
    @Size(max = 100, message = "公告标题不能超过 100 字")
    private String title;

    @NotBlank(message = "请输入公告内容")
    @Size(max = 1000, message = "公告内容不能超过 1000 字")
    private String content;
}
