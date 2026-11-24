package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LabStatus;
import com.campus.lab.common.ReservationStatus;
import com.campus.lab.entity.Lab;
import com.campus.lab.entity.Reservation;
import com.campus.lab.form.LabForm;
import com.campus.lab.repository.EquipmentRepository;
import com.campus.lab.repository.LabRepository;
import com.campus.lab.repository.ReservationRepository;
import com.campus.lab.vo.ScheduleVO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 实验室管理与时段查询业务
 */
@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRepository labRepository;
    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;

    public Page<Lab> page(String keyword, String status, int pageNo) {
        Specification<Lab> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("location")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return labRepository.findAll(spec, PageRequest.of(pageNo, 10, Sort.by(Sort.Direction.ASC, "id")));
    }

    public List<Lab> listAll() {
        return labRepository.findAllByOrderByIdAsc();
    }

    public List<Lab> listAvailable() {
        return labRepository.findByStatusOrderByIdAsc(LabStatus.AVAILABLE);
    }

    public Lab get(Long id) {
        return labRepository.findById(id).orElseThrow(() -> new BusinessException("实验室不存在"));
    }

    public LabForm toForm(Lab lab) {
        LabForm form = new LabForm();
        form.setId(lab.getId());
        form.setCode(lab.getCode());
        form.setName(lab.getName());
        form.setLocation(lab.getLocation());
        form.setCapacity(lab.getCapacity());
        form.setOpenStart(lab.getOpenStart());
        form.setOpenEnd(lab.getOpenEnd());
        form.setDescription(lab.getDescription());
        form.setStatus(lab.getStatus());
        return form;
    }

    @Transactional
    public void save(LabForm form) {
        if (!LabStatus.isValid(form.getStatus())) {
            throw new BusinessException("实验室状态不合法");
        }
        if (form.getOpenStart().isAfter(form.getOpenEnd())) {
            throw new BusinessException("开放开始时间必须早于结束时间");
        }
        if (form.getOpenStart().equals(form.getOpenEnd())) {
            throw new BusinessException("开放开始时间与结束时间不能相同");
        }
        Lab lab;
        if (form.getId() == null) {
            lab = new Lab();
        } else {
            lab = get(form.getId());
        }
        if (form.getId() == null) {
            if (labRepository.existsByCode(form.getCode().trim())) {
                throw new BusinessException("实验室编号已存在");
            }
        } else if (labRepository.existsByCodeAndIdNot(form.getCode().trim(), form.getId())) {
            throw new BusinessException("实验室编号已存在");
        }
        lab.setCode(form.getCode().trim());
        lab.setName(form.getName().trim());
        lab.setLocation(form.getLocation().trim());
        lab.setCapacity(form.getCapacity());
        lab.setOpenStart(form.getOpenStart());
        lab.setOpenEnd(form.getOpenEnd());
        lab.setDescription(form.getDescription());
        lab.setStatus(form.getStatus());
        labRepository.save(lab);
    }

    @Transactional
    public void delete(Long id) {
        Lab lab = get(id);
        if (reservationRepository.existsByLabId(id)) {
            throw new BusinessException("该实验室已有预约记录，无法删除，建议改为停用");
        }
        if (equipmentRepository.existsByLabId(id)) {
            throw new BusinessException("该实验室下还有设备台账，请先处理设备后再删除");
        }
        labRepository.delete(lab);
    }

    /**
     * 生成某实验室指定日期的 1 小时时段，并标记占用 / 已结束
     */
    public ScheduleVO schedule(Long labId, LocalDate date) {
        Lab lab = get(labId);
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        List<Reservation> reservations = reservationRepository
                .findByLabIdAndReservationDateAndStatusIn(labId, targetDate, ReservationStatus.ACTIVE);

        ScheduleVO view = new ScheduleVO();
        view.setLab(lab);
        view.setDate(targetDate);
        view.setDateText(targetDate.toString());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int available = 0;
        LocalTime cursor = lab.getOpenStart();
        while (cursor.isBefore(lab.getOpenEnd())) {
            LocalTime next = cursor.plusHours(1);
            if (next.isAfter(lab.getOpenEnd())) {
                next = lab.getOpenEnd();
            }
            ScheduleVO.Slot slot = new ScheduleVO.Slot();
            final LocalTime slotStart = cursor;
            final LocalTime slotEnd = next;
            slot.setStart(slotStart);
            slot.setEnd(slotEnd);
            slot.setOccupied(reservations.stream()
                    .anyMatch(r -> r.getStartTime().isBefore(slotEnd) && r.getEndTime().isAfter(slotStart)));
            boolean ended = targetDate.isBefore(today)
                    || (targetDate.equals(today) && !cursor.isAfter(now));
            slot.setEnded(ended);
            if (!slot.isOccupied() && !slot.isEnded()) {
                available++;
            }
            view.getSlots().add(slot);
            cursor = next;
        }
        view.setAvailableCount(available);
        return view;
    }
}
