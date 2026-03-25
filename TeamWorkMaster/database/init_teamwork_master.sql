    -- =========================================================================
    -- TẠO MỚI HOÀN TOÀN CƠ SỞ DỮ LIỆU (Xóa cái cũ nếu có)
    -- =========================================================================
    DROP DATABASE IF EXISTS teamwork_master;
    CREATE DATABASE teamwork_master CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    USE teamwork_master;

    -- =========================================================================
    -- 1. BẢNG NGƯỜI DÙNG
    -- =========================================================================
    CREATE TABLE TBL_USERS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        Username VARCHAR(50) NOT NULL UNIQUE,
        PasswordHash VARCHAR(255) NOT NULL,
        FullName VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
        Email VARCHAR(100) NOT NULL UNIQUE,
        Role ENUM('ADMIN', 'MANAGER', 'MEMBER') DEFAULT 'MEMBER',
        IsActive BOOLEAN DEFAULT TRUE,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 2. BẢNG DỰ ÁN
    -- =========================================================================
    CREATE TABLE TBL_PROJECTS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        ProjectName VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
        Description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
        StartDate DATE NOT NULL,
        EndDate DATE,
        Priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
        Status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELED') DEFAULT 'PENDING',
        OwnerID INT NOT NULL,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (OwnerID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 3. BẢNG THÀNH VIÊN DỰ ÁN
    -- =========================================================================
    CREATE TABLE TBL_PROJECT_MEMBERS (
        ProjectID INT,
        UserID INT,
        Role ENUM('OWNER', 'MANAGER', 'MEMBER') DEFAULT 'MEMBER',
        InviteStatus ENUM('PENDING', 'JOINED', 'DECLINED') DEFAULT 'JOINED',
        PRIMARY KEY (ProjectID, UserID),
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 4. BẢNG TRẠNG THÁI CỘT KANBAN
    -- =========================================================================
    CREATE TABLE TBL_STATUS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        ProjectID INT NOT NULL,
        StatusName VARCHAR(50) NOT NULL,
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 5. BẢNG CÔNG VIỆC (TASKS)
    -- =========================================================================
    CREATE TABLE TBL_TASKS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        Title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
        Description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
        Priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
        Deadline DATE,
        StartDate DATE NULL,
        Tags VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
        Status ENUM('TODO', 'IN_PROGRESS', 'DONE', 'CANCEL') DEFAULT 'TODO',
        OrderIndex INT DEFAULT 0,
        AssigneeID INT,
        ProjectID INT NOT NULL,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (AssigneeID) REFERENCES TBL_USERS(ID) ON DELETE SET NULL,
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 6. BẢNG NGƯỜI THỰC HIỆN CÔNG VIỆC (ASSIGNEES)
    -- =========================================================================
    CREATE TABLE TBL_TASK_ASSIGNEES (
        TaskID INT NOT NULL,
        UserID INT NOT NULL,
        AssignedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (TaskID, UserID),
        FOREIGN KEY (TaskID) REFERENCES TBL_TASKS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 7. BẢNG CHECKLIST CON (SUBTASKS)
    -- =========================================================================
    CREATE TABLE TBL_SUBTASKS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        TaskID INT NOT NULL,
        Title VARCHAR(255) NOT NULL,
        IsCompleted BOOLEAN DEFAULT FALSE,
        FOREIGN KEY (TaskID) REFERENCES TBL_TASKS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 8. BẢNG NHẬT KÝ HOẠT ĐỘNG CỦA CÔNG VIỆC (TASK LOGS)
    -- =========================================================================
    CREATE TABLE TBL_TASK_LOGS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        TaskID INT NOT NULL,
        UserID INT NOT NULL,
        Action VARCHAR(255) NOT NULL,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (TaskID) REFERENCES TBL_TASKS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 9. BẢNG TÀI LIỆU ĐÍNH KÈM (ATTACHMENTS)
    -- =========================================================================
    CREATE TABLE TBL_TASK_ATTACHMENTS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        TaskID INT NOT NULL,
        UserID INT NOT NULL,
        FileName VARCHAR(255) NOT NULL,
        FileUrl VARCHAR(500) NOT NULL,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (TaskID) REFERENCES TBL_TASKS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 10. BẢNG BÌNH LUẬN (COMMENTS)
    -- =========================================================================
    CREATE TABLE TBL_COMMENTS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        TaskID INT NOT NULL,
        UserID INT NOT NULL,
        Content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
        FileUrl VARCHAR(500) NULL, 
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (TaskID) REFERENCES TBL_TASKS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 11. BẢNG NHẬT KÝ HOẠT ĐỘNG DỰ ÁN (PROJECT ACTIVITY LOGS)
    -- =========================================================================
    CREATE TABLE TBL_ACTIVITY_LOGS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        ProjectID INT NOT NULL,
        UserID INT NOT NULL,
        Action VARCHAR(255) NOT NULL,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 12. BẢNG THÔNG BÁO (NOTIFICATIONS)
    -- =========================================================================
    CREATE TABLE TBL_NOTIFICATIONS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        UserID INT NOT NULL, 
        ProjectID INT NOT NULL, 
        Title VARCHAR(255) NOT NULL,
        Message TEXT,
        IsRead BOOLEAN DEFAULT FALSE,
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE,
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 13. BẢNG TIN NHẮN DỰ ÁN (CHAT)
    -- =========================================================================
    CREATE TABLE TBL_PROJECT_CHAT (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        ProjectID INT NOT NULL,
        UserID INT NOT NULL,
        Message TEXT,
        FileUrl VARCHAR(500), 
        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE,
        FOREIGN KEY (UserID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 14. BẢNG CUỘC HỌP (MEETINGS)
    -- =========================================================================
    CREATE TABLE TBL_MEETINGS (
        ID INT AUTO_INCREMENT PRIMARY KEY,
        ProjectID INT NOT NULL,
        HostID INT NOT NULL,
        MeetLink VARCHAR(255) NOT NULL,
        Notes TEXT,
        Status ENUM('ONGOING', 'ENDED') DEFAULT 'ONGOING',
        StartTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        EndTime TIMESTAMP NULL,
        FOREIGN KEY (ProjectID) REFERENCES TBL_PROJECTS(ID) ON DELETE CASCADE,
        FOREIGN KEY (HostID) REFERENCES TBL_USERS(ID) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- =========================================================================
    -- 🟢 DỮ LIỆU MẪU BAN ĐẦU (ĐÃ BĂM MẬT KHẨU BẰNG SHA-256)
    -- =========================================================================

    -- 1. TÀI KHOẢN ADMIN (Mật khẩu: 123456)
    INSERT INTO TBL_USERS (Username, PasswordHash, FullName, Email, Role) 
    VALUES ('admin', TO_BASE64(UNHEX(SHA2('123456TeamworkMaster@2026', 256))), 'Quản Trị Viên', 'admin@teamwork.com', 'ADMIN');

