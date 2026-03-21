package com.teamwork.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatisticsDAO {

    // Thống kê số lượng công việc theo trạng thái
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
}
