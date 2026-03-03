package com.teamwork.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.teamwork.db.UserDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ApiServer {
    private HttpServer server;
    private UserDAO userDAO;

    public ApiServer(int port) throws IOException {
        userDAO = new UserDAO();
        // Mở cổng mạng (Ví dụ: 8080)
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Tạo đường dẫn API (Endpoint) cho chức năng Đăng nhập & Đăng ký
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());

        server.setExecutor(null); // Sử dụng luồng mặc định
    }

    public void start() {
        server.start();
        System.out.println("[API SERVER] Da khoi dong thanh cong tren cong " + server.getAddress().getPort());
        System.out.println("[API SERVER] Dang lang nghe yeu cau tu Web Vue.js...");
    }

    // ==========================================
    // 1. LỚP XỬ LÝ ĐĂNG NHẬP (/api/login)
    // ==========================================
    // ==========================================
    // 1. LỚP XỬ LÝ ĐĂNG NHẬP (/api/login)
    // ==========================================
    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("[API SERVER] Dang xu ly Dang nhap: " + requestBody);

                String username = "";
                String password = "";
                try {
                    // Cắt chuỗi giống hệt bên Đăng ký để đảm bảo không bao giờ lỗi
                    username = requestBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    password = requestBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];
                } catch (Exception e) {
                    System.err.println("[LỖI CẮT CHUỖI JSON]: " + e.getMessage());
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Du lieu khong hop le\"}");
                    return;
                }

                // In ra xem Java thực sự đọc được chữ gì
                System.out.println("[API SERVER] Username doc duoc: [" + username + "]");
                System.out.println("[API SERVER] Password doc duoc: [" + password + "]");

                boolean isValid = userDAO.login(username, password);

                String responseJson;
                if (isValid) {
                    responseJson = "{\"success\": true, \"message\": \"Dang nhap thanh cong\"}";
                } else {
                    responseJson = "{\"success\": false, \"message\": \"Sai tai khoan hoac mat khau\"}";
                }

                sendResponse(exchange, 200, responseJson);
            } else {
                sendResponse(exchange, 405, "{\"success\": false, \"message\": \"Phuong thuc khong ho tro\"}");
            }
        }
    }

    // ==========================================
    // 2. LỚP XỬ LÝ ĐĂNG KÝ (/api/register)
    // ==========================================
    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("[API SERVER] Nhan yeu cau dang ky...");

                try {
                    String username = requestBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    String password = requestBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];
                    String fullName = requestBody.split("\"fullName\"\\s*:\\s*\"")[1].split("\"")[0];
                    String email = requestBody.split("\"email\"\\s*:\\s*\"")[1].split("\"")[0];

                    boolean isSuccess = userDAO.register(username, password, fullName, email);

                    if (isSuccess) {
                        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Dang ky thanh cong\"}");
                    } else {
                        sendResponse(exchange, 400,
                                "{\"success\": false, \"message\": \"Tai khoan hoac email da ton tai\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Du lieu gui len khong hop le\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"success\": false, \"message\": \"Phuong thuc khong ho tro\"}");
            }
        }
    }

    // ==========================================
    // 3. HÀM HỖ TRỢ GỬI PHẢN HỒI (Dùng chung)
    // ==========================================
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}