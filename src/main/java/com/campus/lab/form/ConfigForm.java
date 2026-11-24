package com.campus.lab.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 预约规则配置表单
 */
@Data
public class ConfigForm {

    @NotNull(message = "请输入单次最长预约时长")
    @Min(value = 1, message = "最长时长至少为 1 小时")
    @Max(value = 12, message = "最长时长不能超过 12 小时")
    private Integer maxDurationHours;

    @NotNull(message = "请输入最多提前预约天数")
    @Min(value = 1, message = "提前天数至少为 1 天")
    @Max(value = 30, message = "提前天数不能超过 30 天")
    private Integer advanceDays;

    @NotBlank(message = "请输入预约规则说明")
    @Size(max = 2000, message = "规则说明不能超过 2000 字")
    private String bookingRules;
}
