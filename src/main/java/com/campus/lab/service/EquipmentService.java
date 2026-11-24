package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.EquipmentStatus;
import com.campus.lab.entity.Equipment;
import com.campus.lab.entity.Lab;
import com.campus.lab.form.EquipmentForm;
import com.campus.lab.repository.EquipmentRepository;
import com.campus.lab.repository.LabRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备台账业务
 */
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LabRepository labRepository;

    public Page<Equipment> page(String keyword, Long labId, String category, String status, int pageNo) {
        Specification<Equipment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (labId != null) {
                predicates.add(cb.equal(root.get("labId"), labId));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("model")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Equipment> page = equipmentRepository.findAll(spec,
                PageRequest.of(pageNo, 10, Sort.by(Sort.Direction.ASC, "id")));
        fillLabNames(page.getContent());
        return page;
    }

    public List<Equipment> listForLab(Long labId) {
        List<Equipment> list = equipmentRepository.findByLabIdOrderByIdAsc(labId);
        fillLabNames(list);
        return list;
    }

    public List<String> categories() {
        return equipmentRepository.findCategories();
    }

    public Equipment get(Long id) {
        return equipmentRepository.findById(id).orElseThrow(() -> new BusinessException("设备不存在"));
    }

    public EquipmentForm toForm(Equipment equipment) {
        EquipmentForm form = new EquipmentForm();
        form.setId(equipment.getId());
        form.setLabId(equipment.getLabId());
        form.setName(equipment.getName());
        form.setCode(equipment.getCode());
        form.setModel(equipment.getModel());
        form.setCategory(equipment.getCategory());
        form.setPurchaseDate(equipment.getPurchaseDate());
        form.setStatus(equipment.getStatus());
        form.setQuantity(equipment.getQuantity());
        form.setRemark(equipment.getRemark());
        return form;
    }

    @Transactional
    public void save(EquipmentForm form) {
        if (!EquipmentStatus.isValid(form.getStatus())) {
            throw new BusinessException("设备状态不合法");
        }
        Lab lab = labRepository.findById(form.getLabId())
                .orElseThrow(() -> new BusinessException("所属实验室不存在"));
        Equipment equipment;
        if (form.getId() == null) {
            equipment = new Equipment();
            if (equipmentRepository.existsByCode(form.getCode().trim())) {
                throw new BusinessException("设备编号已存在");
            }
        } else {
            equipment = get(form.getId());
            if (equipmentRepository.existsByCodeAndIdNot(form.getCode().trim(), form.getId())) {
                throw new BusinessException("设备编号已存在");
            }
        }
        equipment.setLabId(lab.getId());
        equipment.setName(form.getName().trim());
        equipment.setCode(form.getCode().trim());
        equipment.setModel(form.getModel());
        equipment.setCategory(form.getCategory());
        equipment.setPurchaseDate(form.getPurchaseDate());
        equipment.setStatus(form.getStatus());
        equipment.setQuantity(form.getQuantity());
        equipment.setRemark(form.getRemark());
        equipmentRepository.save(equipment);
    }

    @Transactional
    public void delete(Long id) {
        equipmentRepository.delete(get(id));
    }

    @Transactional
    public void changeStatus(Long id, String status) {
        if (!EquipmentStatus.isValid(status)) {
            throw new BusinessException("设备状态不合法");
        }
        Equipment equipment = get(id);
        equipment.setStatus(status);
        equipmentRepository.save(equipment);
    }

    public void fillLabNames(List<Equipment> equipments) {
        if (equipments == null || equipments.isEmpty()) {
            return;
        }
        Set<Long> labIds = equipments.stream().map(Equipment::getLabId).collect(Collectors.toSet());
        Map<Long, Lab> labMap = labRepository.findAllById(labIds).stream()
                .collect(Collectors.toMap(Lab::getId, Function.identity()));
        for (Equipment equipment : equipments) {
            Lab lab = labMap.get(equipment.getLabId());
            if (lab != null) {
                equipment.setLabName(lab.getName());
            }
        }
    }
}
