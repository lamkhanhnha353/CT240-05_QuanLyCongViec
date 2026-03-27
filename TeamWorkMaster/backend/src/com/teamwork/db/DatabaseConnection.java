package com.teamwork.db;

import com.teamwork.core.GlobalExceptionHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.File;

public class DatabaseConnection {


    private static volatile DatabaseConnection instance;

    private final String DB_NAME = "teamwork_master";
    private final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useUnicode=true&characterEncoding=UTF-8";
    private final String USER = "root";

    // Mật khẩu mặc định an toàn (sẽ dùng nếu không tìm thấy file .env)
    private String password = "";

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[HỆ THỐNG] Nạp thành công Driver MySQL đa luồng!");

            //  TỰ ĐỘNG ĐỌC MẬT KHẨU TỪ FILE .env
            if (System.getenv("DB_PASSWORD") != null) {
                this.password = System.getenv("DB_PASSWORD");
                System.out.println("[BẢO MẬT] Đã lấy mật khẩu từ Biến môi trường Windows!");
            } else {
                try {
                    Properties props = new Properties();

                 
                    File envFile = new File(".env"); 
                    if (!envFile.exists())
                        envFile = new File("backend/.env"); 
                    if (!envFile.exists())
                        envFile = new File("TeamWorkMaster/backend/.env"); 

                    if (envFile.exists()) {
                        props.load(new FileInputStream(envFile));
                        if (props.getProperty("DB_PASSWORD") != null) {
                            this.password = props.getProperty("DB_PASSWORD");
                            System.out.println("[BAO MAT] Da lay mat khau tu file .env tai: " + envFile.getPath());
                        }
                    } else {
                        System.out.println("[BAO MAT] Khong tim thay file .env, dung mat khau mac dinh.");
                    }
                } catch (Exception e) {
                    System.out.println("[BAO MAT] Loi doc file .env, dung mat khau mac dinh an toan.");
                }
            }
        } catch (ClassNotFoundException e) {
            GlobalExceptionHandler.handle("DatabaseConnection", new Exception("Không tìm thấy Driver MySQL!"));
        }
    }

  
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

 
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, this.password);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("DatabaseConnection", new Exception("Lỗi kết nối CSDL: " + e.getMessage()));
            return null;
        }
    }
}