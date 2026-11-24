package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.ReservationStatus;
import com.campus.lab.entity.AuditRecord;
import com.campus.lab.entity.Lab;
import com.campus.lab.entity.Reservation;
import com.campus.lab.entity.User;
import com.campus.lab.form.AuditForm;
import com.campus.lab.form.ReservationForm;
import com.campus.lab.repository.AuditRecordRepository;
import com.campus.lab.repository.LabRepository;
import com.campus.lab.repository.ReservationRepository;
import com.campus.lab.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预约全流程业务：提交、查询、审核、取消、过期
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LabRepository labRepository;
    private final UserRepository userRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final SystemConfigService systemConfigService;

    /**
     * 提交预约，提交后状态为待审核
     */
    @Transactional
    public Long create(ReservationForm form, LoginUser loginUser) {
        Lab lab = labRepository.findById(form.getLabId())
                .orElseThrow(() -> new BusinessException("实验室不存在"));
        if (!lab.isAvailable()) {
            throw new BusinessException("该实验室当前" + lab.getStatusText() + "，暂不可预约");
        }
        var config = systemConfigService.get();
        LocalDate date = form.getReservationDate();
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BusinessException("预约日期不能早于今天");
        }
        if (date.isAfter(today.plusDays(config.getAdvanceDays()))) {
            throw new BusinessException("最多只能提前 " + config.getAdvanceDays() + " 天预约");
        }
        LocalTime start = form.getStartTime();
        LocalTime end = form.getEndTime();
        if (end.isBefore(start) || end.equals(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        if (start.isBefore(lab.getOpenStart()) || end.isAfter(lab.getOpenEnd())) {
            throw new BusinessException("预约时段需在实验室开放时间 " + lab.getOpenRangeText() + " 内");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes > config.getMaxDurationHours() * 60L) {
            throw new BusinessException("单次预约时长不能超过 " + config.getMaxDurationHours() + " 小时");
        }
        if (date.equals(today) && !start.isAfter(LocalTime.now())) {
            throw new BusinessException("该时段已经开始，请选择更晚的时段");
        }
        if (form.getParticipantCount() > lab.getCapacity()) {
            throw new BusinessException("参与人数超过实验室容量（" + lab.getCapacity() + " 人）");
        }
        if (reservationRepository.countConflict(lab.getId(), date, start, end) > 0) {
            throw new BusinessException("该时段已被预约，请选择其他时段");
        }
        User applicant = userRepository.findById(loginUser.getId())
                .orElseThrow(() -> new BusinessException("登录信息已失效，请重新登录"));

        Reservation reservation = new Reservation();
        reservation.setLabId(lab.getId());
        reservation.setApplicantId(applicant.getId());
        reservation.setApplicantName(applicant.getRealName());
        reservation.setApplicantRole(applicant.getRole());
        reservation.setReservationDate(date);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setTitle(form.getTitle().trim());
        reservation.setPurpose(form.getPurpose().trim());
        reservation.setParticipantCount(form.getParticipantCount());
        reservation.setRemark(form.getRemark());
        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);
        return reservation.getId();
    }

    public Page<Reservation> myPage(LoginUser user, String status, Long labId, String keyword,
                                    LocalDate date, int pageNo) {
        Page<Reservation> page = reservationRepository.findAll(
                buildSpec(user.getId(), null, status, labId, keyword, date),
                PageRequest.of(pageNo, 10, Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id"))));
        fillLabNames(page.getContent());
        return page;
    }

    public Page<Reservation> allPage(String status, Long labId, String keyword, LocalDate date, int pageNo) {
        Page<Reservation> page = reservationRepository.findAll(
                buildSpec(null, null, status, labId, keyword, date),
                PageRequest.of(pageNo, 10, Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id"))));
        fillLabNames(page.getContent());
        return page;
    }

    public Page<Reservation> auditedPage(LoginUser teacher, String status, Long labId, String keyword,
                                         LocalDate date, int pageNo) {
        Page<Reservation> page = reservationRepository.findAll(
                buildSpec(null, teacher.getId(), status, labId, keyword, date),
                PageRequest.of(pageNo, 10, Sort.by(Sort.Order.desc("auditTime"), Sort.Order.desc("id"))));
        fillLabNames(page.getContent());
        return page;
    }

    private Specification<Reservation> buildSpec(Long applicantId, Long auditorId, String status,
                                                 Long labId, String keyword, LocalDate date) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (applicantId != null) {
                predicates.add(cb.equal(root.get("applicantId"), applicantId));
            }
            if (auditorId != null) {
                predicates.add(cb.equal(root.get("auditUserId"), auditorId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (labId != null) {
                predicates.add(cb.equal(root.get("labId"), labId));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("reservationDate"), date));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("applicantName")), like),
                        cb.like(cb.lower(root.get("purpose")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Reservation get(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));
    }

    /**
     * 教师审核：通过或驳回，已审核的预约不可重复审核
     */
    @Transactional
    public void audit(Long id, LoginUser auditor, AuditForm form) {
        String action = form.getAction();
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new BusinessException("审核操作不合法");
        }
        if ("REJECT".equals(action) && (form.getReason() == null || form.getReason().isBlank())) {
            throw new BusinessException("驳回预约必须填写驳回理由");
        }
        Reservation reservation = get(id);
        if (!ReservationStatus.PENDING.equals(reservation.getStatus())) {
            throw new BusinessException("该预约已审核，不能重复审核");
        }
        String nextStatus = "APPROVE".equals(action)
                ? ReservationStatus.APPROVED : ReservationStatus.REJECTED;
        int updated = reservationRepository.auditReservation(id, nextStatus, auditor.getId(),
                auditor.getRealName(), LocalDateTime.now(),
                "REJECT".equals(action) ? form.getReason().trim() : null);
        if (updated == 0) {
            throw new BusinessException("该预约已被其他教师审核，请刷新页面");
        }
        AuditRecord record = new AuditRecord();
        record.setReservationId(id);
        record.setAuditorId(auditor.getId());
        record.setAuditorName(auditor.getRealName());
        record.setAction(action);
        record.setReason("REJECT".equals(action) ? form.getReason().trim() : null);
        auditRecordRepository.save(record);
    }

    /**
     * 用户取消自己的预约：仅待审核 / 已通过且尚未开始
     */
    @Transactional
    public void cancel(Long id, LoginUser user, String reason) {
        Reservation reservation = get(id);
        if (!Objects.equals(reservation.getApplicantId(), user.getId())) {
            throw new BusinessException("只能取消自己的预约");
        }
        if (!ReservationStatus.PENDING.equals(reservation.getStatus())
                && !ReservationStatus.APPROVED.equals(reservation.getStatus())) {
            throw new BusinessException("当前状态的预约不可取消");
        }
        if (!LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime())
                .isAfter(LocalDateTime.now())) {
            throw new BusinessException("预约已经开始，无法取消");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelReason(reason == null || reason.isBlank() ? "申请人主动取消" : reason.trim());
        reservationRepository.save(reservation);
    }

    /**
     * 管理员强制取消
     */
    @Transactional
    public void forceCancel(Long id, String reason) {
        Reservation reservation = get(id);
        if (!ReservationStatus.PENDING.equals(reservation.getStatus())
                && !ReservationStatus.APPROVED.equals(reservation.getStatus())) {
            throw new BusinessException("只能取消待审核或已通过的预约");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelReason(reason == null || reason.isBlank() ? "管理员强制取消" : reason.trim());
        reservationRepository.save(reservation);
    }

    /**
     * 管理员批量强制取消
     */
    @Transactional
    public int batchCancel(List<Long> ids, String reason) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请先勾选需要取消的预约");
        }
        int count = 0;
        for (Long id : ids) {
            Reservation reservation = get(id);
            if (ReservationStatus.PENDING.equals(reservation.getStatus())
                    || ReservationStatus.APPROVED.equals(reservation.getStatus())) {
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservation.setCancelReason(reason == null || reason.isBlank() ? "管理员批量强制取消" : reason.trim());
                reservationRepository.save(reservation);
                count++;
            }
        }
        if (count == 0) {
            throw new BusinessException("所选预约均不是待审核或已通过状态");
        }
        return count;
    }

    public List<AuditRecord> auditRecords(Long reservationId) {
        return auditRecordRepository.findByReservationIdOrderByCreateTimeDesc(reservationId);
    }

    @Transactional
    public int expireFinished() {
        return reservationRepository.expireFinished();
    }

    public void fillLabNames(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }
        Set<Long> labIds = reservations.stream().map(Reservation::getLabId).collect(Collectors.toSet());
        Map<Long, Lab> labMap = labRepository.findAllById(labIds).stream()
                .collect(Collectors.toMap(Lab::getId, Function.identity()));
        for (Reservation reservation : reservations) {
            Lab lab = labMap.get(reservation.getLabId());
            if (lab != null) {
                reservation.setLabName(lab.getName());
                reservation.setLabLocation(lab.getLocation());
            }
        }
    }
}
