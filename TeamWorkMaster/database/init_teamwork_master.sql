
-- 1. Tạo Database nếu chưa có và chọn để sử dụng
CREATE DATABASE IF NOT EXISTS teamwork_master DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE teamwork_master;

-- 2. Tạo bảng Người dùng (Users)
CREATE TABLE IF NOT EXISTS TBL_USERS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    FullName VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Role ENUM('ADMIN', 'MEMBER') DEFAULT 'MEMBER',
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tạo bảng Dự án (Projects)
CREATE TABLE IF NOT EXISTS TBL_PROJECTS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ProjectName VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    Description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    StartDate DATE,
    EndDate DATE,
    Status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELED') DEFAULT 'PENDING',
    CreatedBy INT NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CreatedBy) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tạo bảng Công việc (Tasks)
CREATE TABLE IF NOT EXISTS TBL_TASKS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    Description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    Priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    Deadline DATE,
    Status ENUM('TODO', 'IN_PROGRESS', 'DONE', 'CANCEL') DEFAULT 'TODO',
    AssigneeID INT,
    ProjectID INT NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (AssigneeID) REFERENCES TBL_USERS(ID) ON DELETE SET NULL,
    FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Tạo sẵn tài khoản Admin mặc định để test (Mật khẩu: 123456)
-- Dùng INSERT IGNORE để tránh lỗi trùng lặp nếu chạy file này nhiều lần
INSERT IGNORE INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role) 
VALUES ('admin', '123456', 'Quản Trị Viên', 'admin@teamwork.com', 'ADMIN');