package com.campus.lab.repository;

import com.campus.lab.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 系统配置数据访问层
 */
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {

    Optional<SystemConfig> findFirstByOrderByIdAsc();
}
