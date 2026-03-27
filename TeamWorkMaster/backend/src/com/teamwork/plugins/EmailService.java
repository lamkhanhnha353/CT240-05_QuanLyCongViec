package com.teamwork.plugins;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.File;

public class EmailService {

    // HÀM ĐỌC GIÁ TRỊ TỪ .env HOẶC HỆ THỐNG
    private static String getEnvValue(String key) {
        // Ưu tiên đọc từ biến môi trường 
        if (System.getenv(key) != null) {
            return System.getenv(key);
        }
        try {
            Properties props = new Properties();
            File envFile = new File(".env");
            if (!envFile.exists())
                envFile = new File("backend/.env");
            if (!envFile.exists())
                envFile = new File("TeamWorkMaster/backend/.env");

            if (envFile.exists()) {
                props.load(new FileInputStream(envFile));
                return props.getProperty(key);
            }
        } catch (Exception e) {
            System.out.println("[BẢO MẬT] Lỗi đọc file .env cho khóa: " + key);
        }
        return null;
    }

    public static void sendEmail(String toEmail, String subject, String message) {
      
        String apiKey = getEnvValue("SENDGRID_API_KEY");
        String fromEmail = getEnvValue("SENDGRID_FROM_EMAIL");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("❌ [LỖI EMAIL] Chưa cấu hình SENDGRID_API_KEY trong file .env!");
            return;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            System.out.println("❌ [LỖI EMAIL] Chưa cấu hình SENDGRID_FROM_EMAIL trong file .env!");
            return;
        }

        try {
            URI uri = URI.create("https://api.sendgrid.com/v3/mail/send");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

        
            String safeMessage = message.replace("\"", "\\\"").replace("\n", "\\n");

        
            String json = "{"
                    + "\"personalizations\": [{\"to\": [{\"email\": \"" + toEmail + "\"}]}],"
                    + "\"from\": {\"email\": \"" + fromEmail + "\"},"
                    + "\"subject\": \"" + subject + "\","
                    + "\"content\": [{\"type\": \"text/plain\", \"value\": \"" + safeMessage + "\"}]"
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 202 || responseCode == 200) {
                System.out.println("📧 [SendGrid] Đã gửi mail thành công tới " + toEmail);
            } else {
                System.out.println("❌ [SendGrid] Gửi email thất bại. Mã lỗi: " + responseCode);

                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    Scanner scanner = new Scanner(errorStream, "UTF-8");
                    String errorMessage = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    System.out.println("   -> Lý do từ máy chủ: " + errorMessage);
                    scanner.close();
                }
            }
            conn.disconnect();

        } catch (Exception e) {
            System.out.println("❌ [Exception] Lỗi hệ thống khi gửi mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}