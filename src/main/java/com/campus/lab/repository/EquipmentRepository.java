package com.campus.lab.repository;

import com.campus.lab.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 设备台账数据访问层
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByLabId(Long labId);

    long countByLabId(Long labId);

    List<Equipment> findByLabIdOrderByIdAsc(Long labId);

    @Query("select distinct e.category from Equipment e where e.category is not null and e.category <> '' order by e.category")
    List<String> findCategories();
}
