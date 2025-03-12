-- 设置字符集
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS library_management DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

-- 使用数据库
USE library_management;

-- 删除已存在的表（如果存在）以避免冲突
DROP TABLE IF EXISTS borrow_records;  -- 先删除引用其他表的表
DROP TABLE IF EXISTS borrows;         -- 先删除引用其他表的表
DROP TABLE IF EXISTS books;           -- 然后删除被引用的表
DROP TABLE IF EXISTS users;           -- 然后删除被引用的表

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('ADMIN', 'READER') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    publish_date DATE,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    cover VARCHAR(255),
    location VARCHAR(50),
    status ENUM('AVAILABLE', 'BORROWED') NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 创建借阅表
CREATE TABLE IF NOT EXISTS borrows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'OVERDUE') NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 插入管理员用户（密码：admin123）
INSERT INTO users (username, password, name, email, role, status)
VALUES ('admin', 'admin123', '管理员', 'admin@example.com', 'ADMIN', 'ACTIVE');

-- 插入读者用户（密码：reader123）
INSERT INTO users (username, password, name, email, role, status)
VALUES ('reader', 'reader123', '读者用户', 'reader@example.com', 'READER', 'ACTIVE');

-- 插入示例图书数据
INSERT INTO books (isbn, title, author, publisher, publish_date, category, description, cover, location, total_copies, available_copies, status, create_time, update_time)
VALUES 
('9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', '1996-01-01', '古典文学', '《红楼梦》是中国古典四大名著之一，通过描写贾、史、王、薛四大家族的兴衰，展示了封建社会的种种矛盾和悲剧。', '/api/files/hongloumeng.jpg', 'A区-01-01', 10, 10, 'AVAILABLE', NOW(), NOW()),
('9787020008728', '三国演义', '罗贯中', '人民文学出版社', '1998-01-01', '古典文学', '《三国演义》是中国古典四大名著之一，描写了从东汉末年到西晋初年之间近百年的历史风云。', '/api/files/sanguoyanyi.jpg', 'A区-01-02', 8, 8, 'AVAILABLE', NOW(), NOW()),
('9787020002764', '水浒传', '施耐庵', '人民文学出版社', '1997-01-01', '古典文学', '《水浒传》是中国古典四大名著之一，描写了以宋江为首的108位好汉的故事。', '/api/files/shuihuzhuan.jpg', 'A区-01-03', 6, 6, 'AVAILABLE', NOW(), NOW()),
('9787020002856', '西游记', '吴承恩', '人民文学出版社', '1999-01-01', '古典文学', '《西游记》是中国古典四大名著之一，讲述了唐僧师徒四人西天取经的故事。', '/api/files/xiyouji.jpg', 'A区-01-04', 7, 7, 'AVAILABLE', NOW(), NOW()),
('9787544253994', '百年孤独', '加西亚·马尔克斯', '南海出版公司', '2011-06-01', '外国文学', '《百年孤独》是魔幻现实主义文学的代表作，描写了布恩迪亚家族七代人的传奇故事。', '/api/files/bainiangudu.jpg', 'B区-02-01', 5, 5, 'AVAILABLE', NOW(), NOW()),
('9787544291170', '追风筝的人', '卡勒德·胡赛尼', '上海人民出版社', '2006-05-01', '外国文学', '《追风筝的人》讲述了阿富汗少年阿米尔与仆人哈桑之间的故事。', '/api/files/zhuifengzhengderen.jpg', 'B区-02-02', 4, 4, 'AVAILABLE', NOW(), NOW());

-- 插入示例借阅记录
INSERT INTO borrows (user_id, book_id, borrow_date, due_date, status)
VALUES 
(2, 1, DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 'APPROVED'),
(2, 5, DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), 'APPROVED');

-- 更新图书可用数量
UPDATE books SET available_copies = available_copies - 1, status = IF(available_copies - 1 <= 0, 'BORROWED', 'AVAILABLE') WHERE id IN (1, 5); 