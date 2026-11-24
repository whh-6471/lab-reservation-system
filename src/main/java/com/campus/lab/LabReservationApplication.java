package com.campus.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 实验室预约管理系统启动类
 */
@SpringBootApplication
@EnableScheduling
public class LabReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabReservationApplication.class, args);
        System.out.println("==================================================");
        System.out.println("  实验室预约管理系统启动成功");
        System.out.println("  访问地址：http://localhost:8080");
        System.out.println("==================================================");
    }
}
