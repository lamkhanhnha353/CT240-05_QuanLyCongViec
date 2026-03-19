package com.teamwork.core;

import com.sun.net.httpserver.*;
import com.teamwork.db.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class ApiServer {
    private HttpServer server;

    public ApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/api/projects/list", this::handleList);
        server.createContext("/api/projects/create", this::handleCreate);
        server.createContext("/api/projects/update", this::handleUpdate);
        server.createContext("/api/projects/delete", this::handleDelete);
        server.createContext("/api/projects/add-member", this::handleAddMember);
        
        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println(">>> BACKEND ĐANG CHẠY TẠI CỔNG 8080");
        System.out.println(">>> TRẠNG THÁI: SẴN SÀNG KẾT NỐI VỚI VUE");
    }

    private void handleList(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
        try {
            sendResponse(ex, 200, new ProjectDAO().getAllProjectsJson());
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
        
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            String name = extract(body, "name");
            String desc = extract(body, "desc");
            String start = extract(body, "startDate");
            String end = extract(body, "endDate");
            
            int pid = new ProjectDAO().createProject(name, desc, start, end, 1);
            
            if (pid != -1) {
                sendResponse(ex, 201, "{\"message\":\"Tạo thành công\", \"id\":" + pid + "}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Tạo thất bại\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleUpdate(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int pid = Integer.parseInt(extract(body, "projectId"));
            boolean ok = new ProjectDAO().updateProject(pid, extract(body, "name"), extract(body, "desc"), extract(body, "status"), extract(body, "endDate"));
            sendResponse(ex, ok ? 200 : 400, ok ? "{\"msg\":\"Xong\"}" : "{\"error\":\"Lỗi\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int pid = Integer.parseInt(extract(body, "projectId"));
            boolean ok = new ProjectDAO().deleteProject(pid);
            sendResponse(ex, ok ? 200 : 400, ok ? "{\"msg\":\"Xóa xong\"}" : "{\"error\":\"Lỗi\"}");
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleAddMember(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
        
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int projectId = Integer.parseInt(extract(body, "projectId"));
            // ĐÃ SỬA: Lấy email thay vì userId
            String email = extract(body, "email");
            String role = extract(body, "role");
            
            // Gọi hàm addMemberByEmail mới tạo
            boolean ok = new ProjectMemberDAO().addMemberByEmail(projectId, email, role);
            
            if (ok) {
                sendResponse(ex, 200, "{\"message\":\"Thêm thành viên thành công!\"}");
            } else {
                sendResponse(ex, 400, "{\"error\":\"Thêm thất bại. Email này chưa đăng ký tài khoản!\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
    }

    private String extract(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":\\s*\"?(.*?)\"?(?:[,}]|\\s)").matcher(json);
        return m.find() ? m.group(1).trim() : "";
    }

    private void sendResponse(HttpExchange ex, int code, String res) throws IOException {
        byte[] b = res.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}