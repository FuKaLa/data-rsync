-- 创建rsync_sink数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS rsync_sink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用rsync_sink数据库
USE rsync_sink;

-- 创建users表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    age INT COMMENT '年龄',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试数据
INSERT INTO users (name, email, phone, age) VALUES
('张三', 'zhangsan@example.com', '13800138001', 25),
('李四', 'lisi@example.com', '13800138002', 30),
('王五', 'wangwu@example.com', '13800138003', 35),
('赵六', 'zhaoliu@example.com', '13800138004', 28),
('钱七', 'qianqi@example.com', '13800138005', 32);

-- 查看插入的数据
SELECT * FROM users;
