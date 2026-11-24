package com.campus.lab.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 设备台账新增 / 编辑表单
 */
@Data
public class EquipmentForm {

    private Long id;

    @NotNull(message = "请选择所属实验室")
    private Long labId;

    @NotBlank(message = "请输入设备名称")
    @Size(max = 100, message = "设备名称不能超过 100 字")
    private String name;

    @NotBlank(message = "请输入设备编号")
    @Size(max = 50, message = "设备编号不能超过 50 字")
    private String code;

    @Size(max = 100, message = "规格型号不能超过 100 字")
    private String model;

    @Size(max = 50, message = "设备分类不能超过 50 字")
    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate;

    @NotBlank(message = "请选择设备状态")
    private String status = "NORMAL";

    @NotNull(message = "请输入设备数量")
    @Min(value = 1, message = "设备数量至少为 1")
    private Integer quantity = 1;

    @Size(max = 255, message = "备注不能超过 255 字")
    private String remark;
}
