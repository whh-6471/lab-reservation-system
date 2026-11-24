package com.campus.lab.repository;

import com.campus.lab.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 公告数据访问层
 */
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findTop3ByOrderByCreateTimeDesc();

    List<Announcement> findAllByOrderByCreateTimeDesc();
}
