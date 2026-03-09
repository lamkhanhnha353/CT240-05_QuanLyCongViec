package com.teamwork.db;

import com.teamwork.core.GlobalExceptionHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối cơ sở dữ liệu MySQL. Áp dụng Mẫu thiết kế Singleton
 * (Singleton Design Pattern) để đảm bảo toàn bộ hệ thống chỉ dùng chung 1 kết
 * nối duy nhất.
 */
public class DatabaseConnection {

    // 1. Biến static lưu trữ thể hiện (instance) duy nhất của class này
    private static DatabaseConnection instance;
    private Connection connection;

    // CẤU HÌNH DATABASE
    private final String DB_NAME = "teamwork_master";
    private final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useUnicode=true&characterEncoding=UTF-8";
    private final String USER = "root";

    // ⚠️ QUAN TRỌNG: BẠN HÃY ĐỔI "123456" THÀNH MẬT KHẨU ROOT MÀ BẠN ĐÃ ĐẶT LÚC CÀI
    // MYSQL
    private final String PASSWORD = "tmk1206";

    // 2. Constructor được set là 'private' để các class khác không thể dùng từ khóa
    // 'new'
    private DatabaseConnection() {
        try {
            // Nạp Driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Mở đường truyền tới Database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DATABASE] DA KET NOI THANH CONG tới MySQL: " + DB_NAME);
        } catch (ClassNotFoundException e) {
            GlobalExceptionHandler.handle("DatabaseConnection",
                    new Exception("Không tìm thấy Driver MySQL! Hãy kiểm tra lại file .jar trong thư mục lib."));
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("DatabaseConnection",
                    new Exception("Lỗi kết nối CSDL (Sai mật khẩu hoặc MySQL chưa bật): " + e.getMessage()));
        }
    }

    // 3. Hàm public static để các nơi khác gọi và lấy cái 'instance' duy nhất ra
    // dùng
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                // Nếu kết nối bị đóng hoặc đứt mạng, tự động tạo lại kết nối mới
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                GlobalExceptionHandler.handle("DatabaseConnection", e);
            }
        }
        return instance;
    }

    // Hàm cung cấp đối tượng Connection để thực thi các câu lệnh SQL (SELECT,
    // INSERT...)
    public Connection getConnection() {
        return connection;
    }
}
