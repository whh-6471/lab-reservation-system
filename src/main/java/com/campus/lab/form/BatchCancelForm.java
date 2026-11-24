package com.campus.lab.form;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量强制取消预约表单
 */
@Data
public class BatchCancelForm {

    private List<Long> ids = new ArrayList<>();

    @Size(max = 255, message = "取消理由不能超过 255 字")
    private String reason;
}
