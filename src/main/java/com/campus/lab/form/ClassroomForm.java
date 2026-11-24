package com.campus.lab.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 教室新增 / 编辑表单
 */
@Data
public class ClassroomForm {

    private Long id;

    @NotNull(message = "请选择楼层")
    @Min(value = 1, message = "楼层最小为 1")
    @Max(value = 5, message = "楼层最大为 5")
    private Integer floorNo;

    @NotNull(message = "请填写房间序号")
    @Min(value = 1, message = "房间序号最小为 1")
    @Max(value = 20, message = "每层房间序号最大为 20")
    private Integer roomNo;

    @NotNull(message = "请填写容纳人数")
    @Min(value = 1, message = "容纳人数至少为 1")
    @Max(value = 300, message = "容纳人数最多为 300")
    private Integer capacity;

    @Size(max = 30, message = "教室类型不能超过 30 个字符")
    private String mediaType;

    private String status;

    @Size(max = 255, message = "备注不能超过 255 个字符")
    private String remark;

    /** 由楼号 + 楼层 + 序号推导，例如 1 + 1 + 01 = 1101 */
    public String getRoomCode() {
        if (floorNo == null || roomNo == null) {
            return "";
        }
        return "1" + floorNo + String.format("%02d", roomNo);
    }

    public String getRoomName() {
        return getRoomCode().isEmpty() ? "" : "一号教学楼 " + getRoomCode();
    }
}
