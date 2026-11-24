package com.campus.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统公告
 */
@Data
@Entity
@Table(name = "announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "publisher", nullable = false, length = 50)
    private String publisher;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    public String getCreateTimeText() {
        return createTime == null ? "-" : createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getSummary() {
        if (content == null) {
            return "";
        }
        return content.length() > 60 ? content.substring(0, 60) + "..." : content;
    }
}
