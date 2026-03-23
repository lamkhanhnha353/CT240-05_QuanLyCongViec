package com.teamwork.db;

import com.teamwork.core.GlobalExceptionHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối cơ sở dữ liệu MySQL.
 * 🟢 ĐÃ NÂNG CẤP: Hỗ trợ Đa luồng (Multi-threading). Mỗi luồng sẽ có một kết
 * nối riêng!
 */
public class DatabaseConnection {

    // 1. Biến static lưu trữ thể hiện (instance) duy nhất của class này
    private static DatabaseConnection instance;

    // 🟢 ĐÃ XÓA BIẾN "private Connection connection;" ĐỂ KHÔNG DÙNG CHUNG KẾT NỐI
    // NỮA

    // CẤU HÌNH DATABASE
    private final String DB_NAME = "teamwork_master";
    private final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useUnicode=true&characterEncoding=UTF-8";
    private final String USER = "root";

    // ⚠️ QUAN TRỌNG: BẠN HÃY ĐỔI "123456" THÀNH MẬT KHẨU ROOT MÀ BẠN ĐÃ ĐẶT LÚC CÀI
    // MYSQL
    private final String PASSWORD = "20022005nha@";

    // 2. Constructor: Giờ chỉ cần nạp Driver 1 lần lúc bật Server
    private DatabaseConnection() {
        try {
            // Nạp Driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[HỆ THỐNG] Nạp thành công Driver MySQL đa luồng!");
        } catch (ClassNotFoundException e) {
            GlobalExceptionHandler.handle("DatabaseConnection",
                    new Exception("Không tìm thấy Driver MySQL! Hãy kiểm tra lại file .jar trong thư mục lib."));
        }
    }

    // 3. Hàm public static để lấy instance
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        // 🟢 Đã bỏ đoạn check isClosed() rườm rà đi vì giờ không xài chung 1 ống nữa
        return instance;
    }

    // 4. 🟢 ĐÃ SỬA: Mỗi lần gọi là tạo MỘT KẾT NỐI MỚI.
    // Đảm bảo không bao giờ đụng hàng hay bị thằng khác đóng cửa giữa chừng!
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("DatabaseConnection",
                    new Exception("Lỗi kết nối CSDL (Sai mật khẩu hoặc MySQL chưa bật): " + e.getMessage()));
            return null; // Phải return null nếu lỗi
        }
    }
}