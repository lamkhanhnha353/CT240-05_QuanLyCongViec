package com.teamwork.plugins;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class EmailService {

    // CHÚ Ý BẢO MẬT: Nhớ giữ kín mã này nhé!
    private static final String API_KEY = "DA_XOA_DE_PUSH_GIT";

    public static void sendEmail(String toEmail, String subject, String message) {
        try {
            URI uri = URI.create("https://api.sendgrid.com/v3/mail/send");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // BƯỚC SỬA LỖI 400: Làm sạch chuỗi, thay thế dấu xuống dòng (\n) thành (\\n) để
            // JSON không bị vỡ
            String safeMessage = message.replace("\"", "\\\"").replace("\n", "\\n");

            String json = "{"
                    + "\"personalizations\": [{\"to\": [{\"email\": \"" + toEmail + "\"}]}],"
                    + "\"from\": {\"email\": \"gmail của bạn\"},"
                    + "\"subject\": \"" + subject + "\","
                    + "\"content\": [{\"type\": \"text/plain\", \"value\": \"" + safeMessage + "\"}]"
                    + "}";

            OutputStream os = conn.getOutputStream();
            // Ép chuẩn UTF-8 để không bị lỗi font tiếng Việt
            os.write(json.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 202) {
                System.out.println("📧 Email gửi thành công tới " + toEmail);
            } else {
                System.out.println("❌ Gửi email thất bại. Mã lỗi SendGrid: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}