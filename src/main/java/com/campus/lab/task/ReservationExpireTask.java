package com.campus.lab.task;

import com.campus.lab.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约过期自动流转任务：每分钟将已结束的待审核 / 已通过预约置为已过期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpireTask {

    private final ReservationService reservationService;

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    public void expireReservations() {
        try {
            int count = reservationService.expireFinished();
            if (count > 0) {
                log.info("已将 {} 条到期预约自动标记为已过期", count);
            }
        } catch (Exception e) {
            log.warn("预约过期任务执行失败：{}", e.getMessage());
        }
    }
}
