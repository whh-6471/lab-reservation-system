package com.campus.lab.service;

import com.campus.lab.common.ReservationStatus;
import com.campus.lab.common.Role;
import com.campus.lab.entity.Reservation;
import com.campus.lab.repository.EquipmentRepository;
import com.campus.lab.repository.LabRepository;
import com.campus.lab.repository.ReservationRepository;
import com.campus.lab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员数据看板统计业务
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final LabRepository labRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public Map<String, Object> overview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("studentCount", userRepository.countByRole(Role.STUDENT));
        stats.put("teacherCount", userRepository.countByRole(Role.TEACHER));
        stats.put("userCount", userRepository.count());
        stats.put("labCount", labRepository.count());
        stats.put("equipmentCount", equipmentRepository.count());
        stats.put("reservationCount", reservationRepository.count());
        stats.put("todayCount", reservationRepository.countByReservationDate(LocalDate.now()));

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (String status : List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED,
                ReservationStatus.REJECTED, ReservationStatus.CANCELLED, ReservationStatus.EXPIRED)) {
            statusCounts.put(status, reservationRepository.countByStatus(status));
        }
        stats.put("statusCounts", statusCounts);
        stats.put("pendingCount", statusCounts.get(ReservationStatus.PENDING));
        stats.put("approvedCount", statusCounts.get(ReservationStatus.APPROVED));

        stats.put("trend", buildTrend());
        stats.put("rank", buildRank());

        List<Reservation> recent = reservationRepository.findTop5ByOrderByCreateTimeDesc();
        reservationService.fillLabNames(recent);
        stats.put("recent", recent);
        return stats;
    }

    /** 近 7 天预约量趋势，含柱状图高度百分比 */
    private List<Map<String, Object>> buildTrend() {
        LocalDate start = LocalDate.now().minusDays(6);
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Object[] row : reservationRepository.countByDateSince(start)) {
            Object dateValue = row[0];
            LocalDate date;
            if (dateValue instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else if (dateValue instanceof LocalDate localDate) {
                date = localDate;
            } else {
                date = LocalDate.parse(String.valueOf(dateValue));
            }
            counts.put(date, ((Number) row[1]).longValue());
        }
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            long count = counts.getOrDefault(date, 0L);
            int percent = max == 0 ? 0 : (int) Math.round(count * 100.0 / max);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("label", String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth()));
            item.put("count", count);
            item.put("percent", count == 0 ? 0 : Math.max(percent, 8));
            trend.add(item);
        }
        return trend;
    }

    /** 实验室预约量排行 */
    private List<Map<String, Object>> buildRank() {
        List<Map<String, Object>> rank = new ArrayList<>();
        for (Object[] row : reservationRepository.rankLabs()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("labName", row[0]);
            item.put("total", ((Number) row[1]).longValue());
            rank.add(item);
        }
        return rank;
    }
}
