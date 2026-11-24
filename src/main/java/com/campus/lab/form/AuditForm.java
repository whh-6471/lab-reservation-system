package com.campus.lab.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核表单
 */
@Data
public class AuditForm {

    /** APPROVE 通过 / REJECT 驳回 */
    @NotBlank(message = "请选择审核结果")
    private String action;

    @Size(max = 255, message = "驳回理由不能超过 255 字")
    private String reason;
}
