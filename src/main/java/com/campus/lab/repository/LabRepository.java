package com.campus.lab.repository;

import com.campus.lab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 实验室数据访问层
 */
public interface LabRepository extends JpaRepository<Lab, Long>, JpaSpecificationExecutor<Lab> {

    Optional<Lab> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Lab> findByStatusOrderByIdAsc(String status);

    List<Lab> findAllByOrderByIdAsc();
}
