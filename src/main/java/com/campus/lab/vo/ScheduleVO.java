package com.campus.lab.vo;

import com.campus.lab.entity.Lab;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 实验室某一天的时段视图
 */
@Data
public class ScheduleVO {

    private Lab lab;

    private LocalDate date;

    private String dateText;

    private List<Slot> slots = new ArrayList<>();

    private int availableCount;

    /**
     * 单个时段（默认 1 小时）
     */
    @Data
    public static class Slot {

        private LocalTime start;

        private LocalTime end;

        private boolean occupied;

        private boolean ended;

        public String getStartText() {
            return start == null ? "" : start.toString();
        }

        public String getEndText() {
            return end == null ? "" : end.toString();
        }

        public String getStateText() {
            if (ended) {
                return "已结束";
            }
            return occupied ? "已占用" : "可预约";
        }

        public String getStateClass() {
            if (ended) {
                return "slot slot-ended";
            }
            return occupied ? "slot slot-occupied" : "slot slot-free";
        }
    }
}
