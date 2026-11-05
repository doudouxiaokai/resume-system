CREATE DATABASE IF NOT EXISTS resume_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE resume_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 简历信息表（增加性别和年龄字段）
CREATE TABLE IF NOT EXISTS resumes (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       user_id BIGINT,
                                       original_filename VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT,
    name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    gender VARCHAR(10) COMMENT '性别：男/女',
    age INT COMMENT '年龄',
    skills JSON,
    education JSON,
    work_experience JSON,
    raw_text LONGTEXT,
    status TINYINT DEFAULT 0,
    parse_result JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_name (name),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_gender (gender),
    INDEX idx_age (age)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
INSERT INTO users (username, password, email) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBoVc.7JzQ/FW6', 'admin@resume-system.com');

INSERT INTO resumes (user_id, name, phone, email, gender, age, skills, status) VALUES
                                                                                   (1, '张三', '13800138000', 'zhangsan@example.com', '男', 25, '["Java", "Spring Boot", "MySQL"]', 2),
                                                                                   (1, '李四', '13900139000', 'lisi@example.com', '男', 28, '["Python", "Django", "PostgreSQL"]', 2),
                                                                                   (1, '王五', '13700137000', 'wangwu@example.com', '女', 26, '["JavaScript", "Vue.js", "Node.js"]', 2),
                                                                                   (1, '赵六', '13600136000', 'zhaoliu@example.com', '女', 30, '["Java", "Spring Cloud", "Redis"]', 2),
                                                                                   (1, '孙七', '13500135000', 'sunqi@example.com', '男', 22, '["Python", "Flask", "MySQL"]', 2),
                                                                                   (1, '周八', '13400134000', 'zhouba@example.com', '女', 35, '["Go", "Docker", "Kubernetes"]', 2),
                                                                                   (1, '吴九', '13300133000', 'wujiu@example.com', '男', 29, '["Java", "微服务", "Redis"]', 2),
                                                                                   (1, '郑十', '13200132000', 'zhengshi@example.com', '男', 32, '["Python", "机器学习", "TensorFlow"]', 2);