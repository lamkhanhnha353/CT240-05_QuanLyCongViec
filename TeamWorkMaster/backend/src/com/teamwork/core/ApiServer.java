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

        // Tạo đường dẫn API
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/tasks", new TaskHandler()); // Quản lý công việc

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
                    username = requestBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    password = requestBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Du lieu khong hop le\"}");
                    return;
                }

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
                try {
                    String username = requestBody.split("\"username\"\\s*:\\s*\"")[1].split("\"")[0];
                    String password = requestBody.split("\"password\"\\s*:\\s*\"")[1].split("\"")[0];
                    String fullName = requestBody.split("\"fullName\"\\s*:\\s*\"")[1].split("\"")[0];
                    String email = requestBody.split("\"email\"\\s*:\\s*\"")[1].split("\"")[0];

                    boolean isSuccess = userDAO.register(username, password, fullName, email);

                    if (isSuccess) {
                        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Dang ky thanh cong\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Tai khoan hoac email da ton tai\"}");
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
    // 4. LỚP XỬ LÝ QUẢN LÝ CÔNG VIỆC (/api/tasks) -> CẬP NHẬT FULL CRUD
    // ==========================================
    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            // Thêm PUT và DELETE vào danh sách cho phép
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            String method = exchange.getRequestMethod();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                // LẤY DANH SÁCH (GET)
                if ("GET".equals(method)) {
                    java.util.List<com.teamwork.db.Task> tasks = taskDAO.getAllTasks();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < tasks.size(); i++) {
                        com.teamwork.db.Task t = tasks.get(i);
                        json.append("{")
                            .append("\"id\":").append(t.getId()).append(",")
                            .append("\"title\":\"").append(escapeJson(t.getTitle())).append("\",")
                            .append("\"notes\":\"").append(escapeJson(t.getDescription())).append("\",")
                            .append("\"priority\":\"").append(escapeJson(t.getPriority())).append("\",")
                            .append("\"progress\":\"").append(escapeJson(t.getStatus())).append("\",")
                            .append("\"projectId\":").append(t.getProjectId() != null ? t.getProjectId() : "null").append(",")
                            .append("\"assignee\":\"").append(t.getAssigneeId() != null ? "User " + t.getAssigneeId() : "").append("\",")
                            .append("\"deadline\":").append(t.getDeadline() != null ? "\"" + t.getDeadline().toString() + "\"" : "null")
                            .append("}");
                        if (i < tasks.size() - 1) json.append(",");
                    }
                    json.append("]");
                    sendResponse(exchange, 200, json.toString());

                // TẠO CÔNG VIỆC (POST)
                } else if ("POST".equals(method)) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    try {
                        com.teamwork.db.Task newTask = new com.teamwork.db.Task();
                        newTask.setTitle(requestBody.split("\"title\"\\s*:\\s*\"")[1].split("\"")[0]);
                        
                        if (requestBody.contains("\"notes\"")) 
                            newTask.setDescription(requestBody.split("\"notes\"\\s*:\\s*\"")[1].split("\"")[0]);
                        
                        if (requestBody.contains("\"priority\"")) 
                            newTask.setPriority(requestBody.split("\"priority\"\\s*:\\s*\"")[1].split("\"")[0]);
                        
                        if (requestBody.contains("\"assignee\"")) {
                            String assigneeStr = requestBody.split("\"assignee\"\\s*:\\s*\"")[1].split("\"")[0].trim();
                            try { newTask.setAssigneeId(Integer.parseInt(assigneeStr)); } catch(Exception e) {}
                        }
                        
                        if (requestBody.contains("\"projectId\"")) {
                            String pidStr = requestBody.split("\"projectId\"\\s*:\\s*\"?")[1].split("[\",}]")[0].trim();
                            try { if (!pidStr.isEmpty()) newTask.setProjectId(Integer.parseInt(pidStr)); } catch(Exception e) {}
                        }

                        if (requestBody.contains("\"deadline\"")) {
                            String dl = requestBody.split("\"deadline\"\\s*:\\s*\"")[1].split("\"")[0].trim();
                            if (!dl.isEmpty()) newTask.setDeadline(java.sql.Timestamp.valueOf(dl + " 00:00:00"));
                        }
                        
                        newTask.setStatus("TODO"); // Mặc định
                        boolean isSuccess = taskDAO.insertTask(newTask);

                        if (isSuccess) sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Tao cong viec thanh cong\"}");
                        else sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Loi khi luu vao DB\"}");
                    } catch (Exception e) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Du lieu gui len khong hop le\"}");
                    }

                // XÓA CÔNG VIỆC (DELETE)
                } else if ("DELETE".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("id=")) {
                        try {
                            int id = Integer.parseInt(query.split("id=")[1].split("&")[0]);
                            boolean isSuccess = taskDAO.deleteTask(id);
                            if (isSuccess) sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Xóa thành công!\"}");
                            else sendResponse(exchange, 404, "{\"success\": false, \"message\": \"Không tìm thấy!\"}");
                        } catch (Exception e) {
                            sendResponse(exchange, 400, "{\"success\": false, \"message\": \"ID không hợp lệ!\"}");
                        }
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Thiếu tham số ID!\"}");
                    }

                // SỬA CÔNG VIỆC (PUT)
                } else if ("PUT".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("id=")) {
                        try {
                            int id = Integer.parseInt(query.split("id=")[1].split("&")[0]);
                            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                            
                            com.teamwork.db.Task task = new com.teamwork.db.Task();
                            task.setId(id); // Set ID quan trọng nhất để UPDATE
                            
                            // Parse giống như lúc Thêm
                            task.setTitle(requestBody.split("\"title\"\\s*:\\s*\"")[1].split("\"")[0]);
                            if (requestBody.contains("\"notes\"")) task.setDescription(requestBody.split("\"notes\"\\s*:\\s*\"")[1].split("\"")[0]);
                            if (requestBody.contains("\"priority\"")) task.setPriority(requestBody.split("\"priority\"\\s*:\\s*\"")[1].split("\"")[0]);
                            if (requestBody.contains("\"progress\"")) task.setStatus(requestBody.split("\"progress\"\\s*:\\s*\"")[1].split("\"")[0]); // Sửa trạng thái
                            
                            if (requestBody.contains("\"assignee\"")) {
                                String assigneeStr = requestBody.split("\"assignee\"\\s*:\\s*\"")[1].split("\"")[0].trim();
                                try { task.setAssigneeId(Integer.parseInt(assigneeStr)); } catch(Exception e) {}
                            }
                            if (requestBody.contains("\"projectId\"")) {
                                String pidStr = requestBody.split("\"projectId\"\\s*:\\s*\"?")[1].split("[\",}]")[0].trim();
                                try { if (!pidStr.isEmpty()) task.setProjectId(Integer.parseInt(pidStr)); } catch(Exception e) {}
                            }
                            if (requestBody.contains("\"deadline\"")) {
                                String dl = requestBody.split("\"deadline\"\\s*:\\s*\"")[1].split("\"")[0].trim();
                                if (!dl.isEmpty()) task.setDeadline(java.sql.Timestamp.valueOf(dl + " 00:00:00"));
                            }

                            boolean isSuccess = taskDAO.updateTask(task);
                            if (isSuccess) sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Cập nhật thành công!\"}");
                            else sendResponse(exchange, 404, "{\"success\": false, \"message\": \"Không tìm thấy công việc!\"}");
                        } catch (Exception e) {
                            sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Dữ liệu lỗi!\"}");
                        }
                    } else {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Thiếu ID!\"}");
                    }

                } else {
                    sendResponse(exchange, 405, "{\"success\": false, \"message\": \"Phương thức không hỗ trợ\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Lỗi Server\"}");
            }
        }

        private String escapeJson(String data) {
            if (data == null) return "";
            return data.replace("\"", "\\\"");
        }
    }
}