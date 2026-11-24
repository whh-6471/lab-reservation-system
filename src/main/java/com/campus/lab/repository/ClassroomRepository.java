package com.campus.lab.repository;

import com.campus.lab.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 教室数据访问层
 */
public interface ClassroomRepository extends JpaRepository<Classroom, Long>, JpaSpecificationExecutor<Classroom> {

    Optional<Classroom> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);

    long countByStatus(String status);

    List<Classroom> findAllByOrderByFloorNoAscRoomNoAsc();
}
