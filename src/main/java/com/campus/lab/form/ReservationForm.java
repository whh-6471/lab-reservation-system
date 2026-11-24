package com.campus.lab.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 预约提交表单
 */
@Data
public class ReservationForm {

    @NotNull(message = "请选择实验室")
    private Long labId;

    @NotNull(message = "请选择预约日期")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reservationDate;

    @NotNull(message = "请选择开始时间")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "请选择结束时间")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "请输入实验名称")
    @Size(max = 100, message = "实验名称不能超过 100 字")
    private String title;

    @NotBlank(message = "请填写实验用途 / 实验内容")
    @Size(max = 500, message = "实验用途不能超过 500 字")
    private String purpose;

    @NotNull(message = "请填写参与人数")
    @Min(value = 1, message = "参与人数至少为 1 人")
    private Integer participantCount = 1;

    @Size(max = 255, message = "备注不能超过 255 字")
    private String remark;
}
