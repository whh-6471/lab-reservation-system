package com.campus.lab.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

/**
 * 实验室新增 / 编辑表单
 */
@Data
public class LabForm {

    private Long id;

    @NotBlank(message = "请输入实验室编号")
    @Size(max = 50, message = "实验室编号不能超过 50 字")
    private String code;

    @NotBlank(message = "请输入实验室名称")
    @Size(max = 100, message = "实验室名称不能超过 100 字")
    private String name;

    @NotBlank(message = "请输入实验室位置")
    @Size(max = 100, message = "实验室位置不能超过 100 字")
    private String location;

    @NotNull(message = "请输入可容纳人数")
    @Min(value = 1, message = "容量至少为 1 人")
    @Max(value = 500, message = "容量不能超过 500 人")
    private Integer capacity;

    @NotNull(message = "请选择开放开始时间")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime openStart;

    @NotNull(message = "请选择开放结束时间")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime openEnd;

    @Size(max = 500, message = "实验室简介不能超过 500 字")
    private String description;

    @NotBlank(message = "请选择实验室状态")
    private String status = "AVAILABLE";
}
