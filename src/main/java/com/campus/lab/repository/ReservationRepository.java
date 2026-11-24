package com.campus.lab.repository;

import com.campus.lab.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

/**
 * 预约数据访问层
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    long countByStatus(String status);

    long countByReservationDate(LocalDate date);

    long countByLabId(Long labId);

    boolean existsByApplicantId(Long applicantId);

    boolean existsByLabId(Long labId);

    List<Reservation> findTop5ByOrderByCreateTimeDesc();

    List<Reservation> findByLabIdAndReservationDateAndStatusIn(Long labId, LocalDate date, Collection<String> statuses);

    List<Reservation> findByApplicantIdAndStatusIn(Long applicantId, Collection<String> statuses);

    /** 时段冲突检测：同实验室、同日、有效状态、时间区间重叠 */
    @Query("""
            select count(r) from Reservation r
            where r.labId = :labId
              and r.reservationDate = :date
              and r.status in ('PENDING', 'APPROVED')
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    long countConflict(@Param("labId") Long labId,
                       @Param("date") LocalDate date,
                       @Param("startTime") LocalTime startTime,
                       @Param("endTime") LocalTime endTime);

    /** 条件更新审核结果，避免并发重复审核 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Reservation r
            set r.status = :status,
                r.auditUserId = :auditUserId,
                r.auditUserName = :auditUserName,
                r.auditTime = :auditTime,
                r.rejectReason = :rejectReason
            where r.id = :id and r.status = 'PENDING'
            """)
    int auditReservation(@Param("id") Long id,
                         @Param("status") String status,
                         @Param("auditUserId") Long auditUserId,
                         @Param("auditUserName") String auditUserName,
                         @Param("auditTime") LocalDateTime auditTime,
                         @Param("rejectReason") String rejectReason);

    /** 将已结束的待审核 / 已通过预约自动置为已过期 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update reservation
            set status = 'EXPIRED'
            where status in ('PENDING', 'APPROVED')
              and timestamp(reservation_date, end_time) < now()
            """, nativeQuery = true)
    int expireFinished();

    /** 实验室预约量排行 */
    @Query(value = """
            select l.name as lab_name, count(r.id) as total
            from lab_info l
            left join reservation r on r.lab_id = l.id and r.status in ('PENDING', 'APPROVED')
            group by l.id, l.name
            order by count(r.id) desc, l.id asc
            limit 6
            """, nativeQuery = true)
    List<Object[]> rankLabs();

    /** 指定日期之后的每日预约量（用于近 7 天趋势） */
    @Query(value = """
            select reservation_date, count(*)
            from reservation
            where reservation_date >= :start
            group by reservation_date
            """, nativeQuery = true)
    List<Object[]> countByDateSince(@Param("start") LocalDate start);
}
