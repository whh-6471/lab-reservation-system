package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.ClassroomStatus;
import com.campus.lab.entity.Classroom;
import com.campus.lab.form.ClassroomForm;
import com.campus.lab.repository.ClassroomRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 空教室查询与教室维护
 */
@Service
@RequiredArgsConstructor
public class ClassroomService {

    private static final String BUILDING_NAME = "一号教学楼";
    private static final int BUILDING_NO = 1;

    private final ClassroomRepository classroomRepository;

    /** 按楼层、状态、编号或类型筛选教室 */
    public List<Classroom> list(Integer floorNo, String status, String keyword) {
        return classroomRepository.findAll(buildSpec(floorNo, status, keyword),
                org.springframework.data.domain.Sort.by("floorNo").ascending().and(
                        org.springframework.data.domain.Sort.by("roomNo").ascending()));
    }

    private Specification<Classroom> buildSpec(Integer floorNo, String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (floorNo != null) {
                predicates.add(cb.equal(root.get("floorNo"), floorNo));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("roomCode")), like),
                        cb.like(cb.lower(root.get("mediaType")), like),
                        cb.like(cb.lower(root.get("buildingName")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** 空教室统计 */
    public Map<String, Long> stats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", classroomRepository.count());
        stats.put("free", classroomRepository.countByStatus(ClassroomStatus.FREE));
        stats.put("inUse", classroomRepository.countByStatus(ClassroomStatus.IN_USE));
        stats.put("maintenance", classroomRepository.countByStatus(ClassroomStatus.MAINTENANCE));
        return stats;
    }

    public Classroom get(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教室不存在"));
    }

    public ClassroomForm toForm(Classroom classroom) {
        ClassroomForm form = new ClassroomForm();
        form.setId(classroom.getId());
        form.setFloorNo(classroom.getFloorNo());
        form.setRoomNo(classroom.getRoomNo());
        form.setCapacity(classroom.getCapacity());
        form.setMediaType(classroom.getMediaType());
        form.setStatus(classroom.getStatus());
        form.setRemark(classroom.getRemark());
        return form;
    }

    @Transactional
    public void save(ClassroomForm form) {
        String roomCode = form.getRoomCode();
        if (roomCode.isEmpty()) {
            throw new BusinessException("请填写楼层与房间序号");
        }
        String status = ClassroomStatus.isValid(form.getStatus()) ? form.getStatus() : ClassroomStatus.FREE;
        classroomRepository.findByRoomCode(roomCode).ifPresent(exist -> {
            if (!exist.getId().equals(form.getId())) {
                throw new BusinessException("教室 " + roomCode + " 已存在");
            }
        });
        Classroom classroom = form.getId() == null ? new Classroom() : get(form.getId());
        classroom.setBuildingNo(BUILDING_NO);
        classroom.setBuildingName(BUILDING_NAME);
        classroom.setFloorNo(form.getFloorNo());
        classroom.setRoomNo(form.getRoomNo());
        classroom.setRoomCode(roomCode);
        classroom.setCapacity(form.getCapacity());
        classroom.setMediaType(form.getMediaType());
        classroom.setStatus(status);
        classroom.setRemark(form.getRemark());
        classroomRepository.save(classroom);
    }

    @Transactional
    public void changeStatus(Long id, String status) {
        if (!ClassroomStatus.isValid(status)) {
            throw new BusinessException("教室状态不合法");
        }
        Classroom classroom = get(id);
        classroom.setStatus(status);
        classroomRepository.save(classroom);
    }

    @Transactional
    public void delete(Long id) {
        classroomRepository.delete(get(id));
    }

    public List<Integer> floors() {
        return List.of(1, 2, 3, 4, 5);
    }

    public List<String> mediaTypes() {
        return List.of("多媒体教室", "智慧教室", "普通教室", "阶梯教室");
    }
}
