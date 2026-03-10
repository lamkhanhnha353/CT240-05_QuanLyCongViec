package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatisticsDAO {

    public void countTaskByStatus() {

        String sql = "SELECT Status, COUNT(*) AS Total FROM TBL_TASKS GROUP BY Status";

        try {

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String status = rs.getString("Status");
                int total = rs.getInt("Total");

                System.out.println(status + " : " + total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm main để test nhanh
    public static void main(String[] args) {

        System.out.println("=== THONG KE CONG VIEC ===");

        StatisticsDAO dao = new StatisticsDAO();
        dao.countTaskByStatus();

    }
}