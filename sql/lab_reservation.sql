-- ============================================================
-- 高校实验室预约管理系统 数据库脚本
-- 适用：MySQL 8.0
-- 执行：mysql -u root -p < lab_reservation.sql
-- 说明：脚本会重建 lab_reservation 库，请勿在正式库执行
-- ============================================================

DROP DATABASE IF EXISTS lab_reservation;
CREATE DATABASE lab_reservation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE lab_reservation;

-- ------------------------------------------------------------
-- 1. 用户表（学生 / 教师 / 管理员）
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '密码（MD5）',
    real_name   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    role        VARCHAR(20)  NOT NULL COMMENT '角色：STUDENT 学生 / TEACHER 教师 / ADMIN 管理员',
    student_no  VARCHAR(50)  DEFAULT NULL COMMENT '学号或工号',
    department  VARCHAR(100) DEFAULT NULL COMMENT '所属院系',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    email       VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1 启用 / 0 禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 2. 实验室表
-- ------------------------------------------------------------
CREATE TABLE lab_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(50)  NOT NULL COMMENT '实验室编号',
    name        VARCHAR(100) NOT NULL COMMENT '实验室名称',
    location    VARCHAR(100) NOT NULL COMMENT '所在位置',
    capacity    INT          NOT NULL DEFAULT 20 COMMENT '可容纳人数',
    open_start  TIME         NOT NULL COMMENT '每日开放开始时间',
    open_end    TIME         NOT NULL COMMENT '每日开放结束时间',
    description VARCHAR(500) DEFAULT NULL COMMENT '实验室简介',
    status      VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE 开放 / MAINTENANCE 维护 / CLOSED 停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lab_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实验室表';

-- ------------------------------------------------------------
-- 3. 预约表
-- ------------------------------------------------------------
CREATE TABLE reservation (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    lab_id            BIGINT       NOT NULL COMMENT '实验室ID',
    applicant_id      BIGINT       NOT NULL COMMENT '申请人ID',
    applicant_name    VARCHAR(50)  NOT NULL COMMENT '申请人姓名',
    applicant_role    VARCHAR(20)  NOT NULL COMMENT '申请人角色',
    reservation_date  DATE         NOT NULL COMMENT '预约日期',
    start_time        TIME         NOT NULL COMMENT '开始时间',
    end_time          TIME         NOT NULL COMMENT '结束时间',
    title             VARCHAR(100) NOT NULL COMMENT '实验名称',
    purpose           VARCHAR(500) NOT NULL COMMENT '实验用途 / 实验内容',
    participant_count INT          NOT NULL DEFAULT 1 COMMENT '参与人数',
    remark            VARCHAR(255) DEFAULT NULL COMMENT '备注',
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                      COMMENT '状态：PENDING 待审核 / APPROVED 已通过 / REJECTED 已驳回 / CANCELLED 已取消 / EXPIRED 已过期',
    audit_user_id     BIGINT       DEFAULT NULL COMMENT '审核人ID',
    audit_user_name   VARCHAR(50)  DEFAULT NULL COMMENT '审核人姓名',
    audit_time        DATETIME     DEFAULT NULL COMMENT '审核时间',
    reject_reason     VARCHAR(255) DEFAULT NULL COMMENT '驳回理由',
    cancel_reason     VARCHAR(255) DEFAULT NULL COMMENT '取消理由',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_lab_date (lab_id, reservation_date),
    KEY idx_applicant (applicant_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实验室预约表';

-- ------------------------------------------------------------
-- 4. 设备台账表
-- ------------------------------------------------------------
CREATE TABLE equipment (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    lab_id        BIGINT       NOT NULL COMMENT '所属实验室ID',
    name          VARCHAR(100) NOT NULL COMMENT '设备名称',
    code          VARCHAR(50)  NOT NULL COMMENT '设备编号',
    model         VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    category      VARCHAR(50)  DEFAULT NULL COMMENT '设备分类',
    purchase_date DATE         DEFAULT NULL COMMENT '购置日期',
    status        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL 正常 / REPAIRING 维修中 / SCRAPPED 已报废',
    quantity      INT          NOT NULL DEFAULT 1 COMMENT '数量',
    remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_equipment_code (code),
    KEY idx_lab_id (lab_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '设备台账表';

-- ------------------------------------------------------------
-- 5. 审核记录表
-- ------------------------------------------------------------
CREATE TABLE audit_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    reservation_id BIGINT       NOT NULL COMMENT '预约ID',
    auditor_id     BIGINT       NOT NULL COMMENT '审核人ID',
    auditor_name   VARCHAR(50)  NOT NULL COMMENT '审核人姓名',
    action         VARCHAR(20)  NOT NULL COMMENT '操作：APPROVE 通过 / REJECT 驳回',
    reason         VARCHAR(255) DEFAULT NULL COMMENT '驳回理由',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (id),
    KEY idx_reservation_id (reservation_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约审核记录表';

-- ------------------------------------------------------------
-- 6. 系统配置表（预约规则）
-- ------------------------------------------------------------
CREATE TABLE system_config (
    id                 INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    max_duration_hours INT      NOT NULL DEFAULT 4 COMMENT '单次预约最长时长（小时）',
    advance_days       INT      NOT NULL DEFAULT 7 COMMENT '最多提前预约天数',
    booking_rules      TEXT     COMMENT '预约规则说明',
    update_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统配置表';

-- ------------------------------------------------------------
-- 7. 公告表
-- ------------------------------------------------------------
CREATE TABLE announcement (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(100)  NOT NULL COMMENT '公告标题',
    content     VARCHAR(1000) NOT NULL COMMENT '公告内容',
    publisher   VARCHAR(50)   NOT NULL COMMENT '发布人',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统公告表';

-- ============================================================
-- 初始化数据
-- 默认密码均为 123456（MD5：e10adc3949ba59abbe56e057f20f883e）
-- ============================================================
INSERT INTO sys_user (username, password, real_name, role, student_no, department, phone, email, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN',   'A0001',   '实验室管理中心',       '13800000000', 'admin@campus.edu.cn',   1),
('t001',  'e10adc3949ba59abbe56e057f20f883e', '张建国',     'TEACHER', 'T001',    '计算机科学与技术学院', '13800000001', 'zhangjg@campus.edu.cn', 1),
('t002',  'e10adc3949ba59abbe56e057f20f883e', '刘晓梅',     'TEACHER', 'T002',    '化学与材料学院',       '13800000002', 'liuxm@campus.edu.cn',   1),
('s001',  'e10adc3949ba59abbe56e057f20f883e', '李明',       'STUDENT', '2023001', '计算机科学与技术学院', '13900000001', 'liming@campus.edu.cn',  1),
('s002',  'e10adc3949ba59abbe56e057f20f883e', '王芳',       'STUDENT', '2023002', '电子信息工程学院',     '13900000002', 'wangfang@campus.edu.cn', 1),
('s003',  'e10adc3949ba59abbe56e057f20f883e', '陈晨',       'STUDENT', '2023003', '化学与材料学院',       '13900000003', 'chenchen@campus.edu.cn', 1),
('s004',  'e10adc3949ba59abbe56e057f20f883e', '赵磊',       'STUDENT', '2023004', '机械工程学院',         '13900000004', 'zhaolei@campus.edu.cn',  0);

INSERT INTO lab_info (code, name, location, capacity, open_start, open_end, description, status) VALUES
('LAB-A201', '计算机实验室 A201', '实验楼A座 2层', 40, '08:00:00', '20:00:00', '配备高性能开发主机与网络实验环境，适用于程序设计、数据结构、人工智能等课程实验。', 'AVAILABLE'),
('LAB-B105', '电子电路实验室 B105', '实验楼B座 1层', 30, '08:00:00', '18:00:00', '提供示波器、信号发生器、焊接平台等设备，适用于模拟电路、数字电路实验。', 'AVAILABLE'),
('LAB-C302', '化学分析实验室 C302', '实验楼C座 3层', 24, '08:30:00', '17:30:00', '配备通风柜、分光光度计、pH计等分析仪器，适用于无机/有机化学及环境分析实验。', 'AVAILABLE'),
('LAB-D401', '生物医学实验室 D401', '实验楼D座 4层', 20, '09:00:00', '17:00:00', '配备生物安全柜、离心机、培养箱等设备，适用于细胞培养与微生物实验。', 'AVAILABLE'),
('LAB-E102', '机械工程实训室 E102', '实训中心 1层', 36, '08:00:00', '19:00:00', '提供数控机床、3D打印机、力学测试平台，适用于机械设计与制造实训。', 'MAINTENANCE');

INSERT INTO equipment (lab_id, name, code, model, category, purchase_date, status, quantity, remark) VALUES
(1, '台式开发工作站', 'EQ-A201-01', 'Dell OptiPlex 7090', '计算机类', '2022-09-01', 'NORMAL', 40, '配套 23.8 英寸显示器'),
(1, '千兆交换机', 'EQ-A201-02', 'H3C S5130S-28S', '计算机类', '2021-06-15', 'NORMAL', 2, '用于网络实验组网'),
(1, '投影仪', 'EQ-A201-03', 'Epson CB-X06', '计算机类', '2020-03-10', 'REPAIRING', 1, '镜头故障，返厂维修中'),
(2, '数字示波器', 'EQ-B105-01', 'Tektronix TBS1102B', '电子类', '2022-04-20', 'NORMAL', 12, '带宽 100MHz'),
(2, '函数信号发生器', 'EQ-B105-02', 'RIGOL DG1022Z', '电子类', '2023-02-18', 'NORMAL', 10, '双通道'),
(2, '恒温焊台', 'EQ-B105-03', 'Quick 936A', '电子类', '2019-11-05', 'SCRAPPED', 5, '老化报废'),
(3, '紫外可见分光光度计', 'EQ-C302-01', 'Shimadzu UV-1900i', '化学类', '2023-05-12', 'NORMAL', 2, '波长范围 190-1100nm'),
(3, '通风柜', 'EQ-C302-02', 'KJ-TFG-1500', '化学类', '2021-08-30', 'NORMAL', 6, '常规化学实验防护'),
(3, '精密pH计', 'EQ-C302-03', 'Mettler S220', '化学类', '2022-10-11', 'REPAIRING', 3, '电极更换中'),
(4, '生物安全柜', 'EQ-D401-01', 'Thermo 1300 A2', '生物类', '2021-12-01', 'NORMAL', 2, 'II 级 A2 型'),
(4, '低温离心机', 'EQ-D401-02', 'Eppendorf 5424R', '生物类', '2022-07-08', 'NORMAL', 3, '最高转速 21130rpm'),
(5, '数控车床', 'EQ-E102-01', 'CK6140', '机械类', '2018-09-20', 'NORMAL', 4, '实训教学用'),
(5, '3D打印机', 'EQ-E102-02', 'Raise3D Pro3', '机械类', '2023-03-15', 'REPAIRING', 2, '喷头堵塞维护中');

INSERT INTO system_config (max_duration_hours, advance_days, booking_rules) VALUES
(4, 7,
 '1. 实验室开放时间以各实验室公示的开放时段为准。\n2. 每次预约最长不超过 4 小时，最多可提前 7 天预约。\n3. 预约提交后需教师审核，审核通过后方可使用实验室。\n4. 请在预约开始前 30 分钟到达实验室，并携带学生证或工牌。\n5. 如需取消预约，请在预约开始前操作取消；无故缺席将影响后续预约信用。\n6. 使用期间请遵守实验室安全规范，爱护设备，保持环境卫生。');

INSERT INTO announcement (title, content, publisher, create_time) VALUES
('实验室预约系统上线通知', '为提升实验室资源使用效率，实验室预约管理系统正式上线。学生可在线提交预约申请，教师在线审核，请各位师生按流程规范使用。', '实验室管理中心', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('本周六下午实验室维护安排', '本周六 13:00-17:00 将对电子电路实验室 B105 的配电柜进行检修，期间暂停预约，已通过的预约将由管理员统一调整，请相互转告。', '系统管理员', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 预约示例数据（日期基于执行当天动态生成，便于演示）
INSERT INTO reservation
(lab_id, applicant_id, applicant_name, applicant_role, reservation_date, start_time, end_time,
 title, purpose, participant_count, remark, status, audit_user_id, audit_user_name, audit_time, reject_reason, create_time) VALUES
(1, 4, '李明', 'STUDENT', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '11:00:00',
 '数据结构课程实验', '完成二叉树与图的遍历算法验证，需要连续两小时使用开发环境。', 2, '希望使用已安装好 JDK 的机器', 'PENDING', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 5, '王芳', 'STUDENT', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '16:00:00',
 '数字电路综合实验', '完成四位加法器电路搭建与测试，需使用示波器与信号发生器。', 3, '需要两套焊接工具', 'PENDING', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(3, 6, '陈晨', 'STUDENT', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '12:00:00',
 '水质 pH 值测定实验', '对环境水样进行 pH 与吸光度测定，完成实验报告数据采集。', 2, '已预约通风柜', 'APPROVED', 3, '刘晓梅', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 5, '王芳', 'STUDENT', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:00:00', '15:00:00',
 '细胞培养观察实验', '观察传代细胞的生长状态并记录，使用生物安全柜与离心机。', 1, '需提前开启培养箱', 'APPROVED', 3, '刘晓梅', DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(1, 4, '李明', 'STUDENT', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00:00', '10:00:00',
 '算法设计上机练习', '完成排序算法性能对比实验。', 1, NULL, 'EXPIRED', 2, '张建国', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 6, '陈晨', 'STUDENT', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '15:00:00', '17:00:00',
 '机械零件测绘实训', '完成减速器零件测绘与三维建模。', 4, NULL, 'REJECTED', 2, '张建国', DATE_SUB(NOW(), INTERVAL 2 DAY), '该时段实训室设备检修，请改约其他时间', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 4, '李明', 'STUDENT', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '10:00:00', '12:00:00',
 '模拟电路仿真实验', '使用 Multisim 完成放大电路仿真。', 2, '临时课程调整', 'CANCELLED', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 5, '王芳', 'STUDENT', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00:00', '17:00:00',
 '人工智能课程实验', '训练简单神经网络模型，需 GPU 开发机。', 2, NULL, 'PENDING', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR));

UPDATE reservation SET cancel_reason = '课程临时调整，无需使用实验室' WHERE status = 'CANCELLED';

INSERT INTO audit_record (reservation_id, auditor_id, auditor_name, action, reason, create_time) VALUES
(3, 3, '刘晓梅', 'APPROVE', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 3, '刘晓梅', 'APPROVE', NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(5, 2, '张建国', 'APPROVE', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 2, '张建国', 'REJECT', '该时段实训室设备检修，请改约其他时间', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ------------------------------------------------------------------
-- 8. 教学楼教室（空教室查询）
-- 编号规则：1 + 楼层 + 两位房间序号，如 1101 = 一号教学楼 1 层 01 间
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS classroom;
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    building_no INT NOT NULL COMMENT '楼号',
    building_name VARCHAR(50) NOT NULL COMMENT '楼名',
    floor_no INT NOT NULL COMMENT '楼层',
    room_no INT NOT NULL COMMENT '楼层内序号',
    room_code VARCHAR(20) NOT NULL COMMENT '教室编号',
    capacity INT NOT NULL COMMENT '容纳人数',
    media_type VARCHAR(30) DEFAULT NULL COMMENT '教室类型',
    status VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT '状态：FREE/IN_USE/MAINTENANCE',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_code (room_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学楼教室';

INSERT INTO classroom (building_no, building_name, floor_no, room_no, room_code, capacity, media_type, status, remark)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    1,
    '一号教学楼',
    CEIL(n / 20),
    ((n - 1) % 20) + 1,
    CONCAT('1', CEIL(n / 20), LPAD(((n - 1) % 20) + 1, 2, '0')),
    CASE WHEN ((n - 1) % 20) + 1 <= 10 THEN 60 ELSE 40 END,
    CASE CEIL(n / 20) WHEN 1 THEN '多媒体教室' WHEN 2 THEN '智慧教室' ELSE '普通教室' END,
    CASE
        WHEN ((n - 1) % 20) + 1 = 18 THEN 'MAINTENANCE'
        WHEN ((n - 1) % 20) + 1 BETWEEN 15 AND 17 THEN 'IN_USE'
        ELSE 'FREE'
    END,
    CASE WHEN ((n - 1) % 20) + 1 = 18 THEN '投影设备检修中' ELSE NULL END
FROM seq
ORDER BY n;
