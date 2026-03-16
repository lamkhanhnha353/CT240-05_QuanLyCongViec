package com.teamwork.plugins;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class EmailService {

    private static final String API_KEY = System.getenv("SENDGRID_API_KEY");

    public static void sendEmail(String toEmail, String subject, String message) {

        try {

            URI uri = URI.create("https://api.sendgrid.com/v3/mail/send");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{"
                    + "\"personalizations\": [{\"to\": [{\"email\": \"" + toEmail + "\"}]}],"
                    + "\"from\": {\"email\": \"saturn6542@gmail.com\"},"
                    + "\"subject\": \"" + subject + "\","
                    + "\"content\": [{\"type\": \"text/plain\", \"value\": \"" + message + "\"}]"
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 202) {
                System.out.println("📧 Email gửi thành công tới " + toEmail);
            } else {
                System.out.println("❌ Gửi email thất bại: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}