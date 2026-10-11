-- ============================================
-- 作业建库脚本：DateTest
-- 两张表：college（学院）、student（学生）
-- ============================================

CREATE DATABASE IF NOT EXISTS DateTest
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE DateTest;

-- 学院表：id, name, code
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS college;

CREATE TABLE college (
    id   INT PRIMARY KEY AUTO_INCREMENT COMMENT '学院编号',
    name VARCHAR(100) NOT NULL COMMENT '学院名称',
    code VARCHAR(20)  NOT NULL COMMENT '学院代码'
);

-- 学生表：id, name, major, age, enrollment_date, graduated, tuition
CREATE TABLE student (
    id              INT PRIMARY KEY AUTO_INCREMENT COMMENT '学号',
    name            VARCHAR(50)    NOT NULL COMMENT '姓名',
    major           VARCHAR(100)            COMMENT '专业',
    age             INT                     COMMENT '年龄',
    enrollment_date DATE                    COMMENT '入学时间',
    graduated       TINYINT(1) DEFAULT 0    COMMENT '是否毕业：0否 1是',
    tuition         DOUBLE                  COMMENT '学费（元）'
);

-- 插入测试数据
INSERT INTO college (name, code) VALUES
('信息科学与工程学院', 'CSEE'),
('土木工程学院',       'CCE');

INSERT INTO student (name, major, age, enrollment_date, graduated, tuition) VALUES
('张三', '计算机科学与技术', 20, '2024-09-01', 0, 6000.00),
('李四', '软件工程',         21, '2023-09-01', 0, 6500.00),
('王五', '道路与铁道工程',   22, '2020-09-01', 1, 5500.00);
