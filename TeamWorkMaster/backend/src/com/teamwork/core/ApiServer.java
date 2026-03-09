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
    private com.teamwork.db.TaskDAO taskDAO;

    public ApiServer(int port) throws IOException {
        userDAO = new UserDAO();
        taskDAO = new com.teamwork.db.TaskDAO();
        // Mở cổng mạng (Ví dụ: 8080)
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Tạo đường dẫn API (Endpoint) cho chức năng Đăng nhập & Đăng ký
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        //Đăng ký đường dẫn quản lý công việc
        server.createContext("/api/tasks", new TaskHandler());

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
    // ==========================================
    // 4. LỚP XỬ LÝ QUẢN LÝ CÔNG VIỆC (/api/tasks)
    // ==========================================
    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            String method = exchange.getRequestMethod();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equals(method)) {
                    // LẤY DANH SÁCH TASK
                    java.util.List<com.teamwork.db.Task> tasks = taskDAO.getAllTasks();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < tasks.size(); i++) {
                        com.teamwork.db.Task t = tasks.get(i);
                        json.append("{")
                            .append("\"id\":").append(t.getId()).append(",")
                            .append("\"title\":\"").append(escapeJson(t.getTitle())).append("\",")
                            .append("\"status\":\"").append(escapeJson(t.getStatus())).append("\",")
                            .append("\"priority\":\"").append(escapeJson(t.getPriority())).append("\",")
                            .append("\"deadline\":").append(t.getDeadline() != null ? "\"" + t.getDeadline().toString() + "\"" : "null")
                            .append("}");
                        if (i < tasks.size() - 1) json.append(",");
                    }
                    json.append("]");
                    sendResponse(exchange, 200, json.toString());

                } else if ("POST".equals(method)) {
                    // TẠO MỚI TASK
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("[API SERVER] Nhan yeu cau tao Task...");

                    try {
                        // Tự parse JSON thủ công theo style của nhóm
                        String title = requestBody.split("\"title\"\\s*:\\s*\"")[1].split("\"")[0];
                        String description = ""; // Có thể trống
                        if (requestBody.contains("\"description\"")) {
                            description = requestBody.split("\"description\"\\s*:\\s*\"")[1].split("\"")[0];
                        }
                        String deadlineStr = "";
                        if(requestBody.contains("\"deadline\"")) {
                            deadlineStr = requestBody.split("\"deadline\"\\s*:\\s*\"")[1].split("\"")[0];
                        }

                        com.teamwork.db.Task newTask = new com.teamwork.db.Task();
                        newTask.setTitle(title);
                        newTask.setDescription(description);
                        if (!deadlineStr.isEmpty()) {
                            // Cần chuỗi định dạng yyyy-[m]m-[d]d hh:mm:ss
                            newTask.setDeadline(java.sql.Timestamp.valueOf(deadlineStr + " 00:00:00")); 
                        }

                        boolean isSuccess = taskDAO.insertTask(newTask);

                        if (isSuccess) {
                            sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Tao cong viec thanh cong\"}");
                        } else {
                            sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Loi khi luu vao DB\"}");
                        }
                    } catch (Exception e) {
                        System.err.println("[LỖI POST TASK]: " + e.getMessage());
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Du lieu gui len khong hop le\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"success\": false, \"message\": \"Phuong thuc khong ho tro\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Loi he thong\"}");
            }
        }

        // Hàm tiện ích hỗ trợ tránh lỗi dấu ngoặc kép trong chuỗi JSON
        private String escapeJson(String data) {
            if (data == null) return "";
            return data.replace("\"", "\\\"");
        }
    }
}