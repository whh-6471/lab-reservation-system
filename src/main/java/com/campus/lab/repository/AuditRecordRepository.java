package com.campus.lab.repository;

import com.campus.lab.entity.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 审核记录数据访问层
 */
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    List<AuditRecord> findByReservationIdOrderByCreateTimeDesc(Long reservationId);
}
