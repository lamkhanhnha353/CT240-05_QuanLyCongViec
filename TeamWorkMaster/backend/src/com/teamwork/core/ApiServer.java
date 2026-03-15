package com.teamwork.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.teamwork.db.UserDAO;
import com.teamwork.db.CommentDAO;
import com.teamwork.model.Comment;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiServer {
    private HttpServer server;
    private UserDAO userDAO;
    private CommentDAO commentDAO;

    public ApiServer(int port) throws IOException {
        userDAO = new UserDAO();
        commentDAO = new CommentDAO();

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/statistics", new StatisticsHandler());
        server.createContext("/api/comments", new CommentsHandler());
        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("[API SERVER] Da khoi dong thanh cong tren cong " + server.getAddress().getPort());
        System.out.println("[API SERVER] Dang lang nghe yeu cau tu Web Vue.js...");
    }

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
                String responseJson = isValid ? "{\"success\": true, \"message\": \"Dang nhap thanh cong\"}"
                        : "{\"success\": false, \"message\": \"Sai tai khoan hoac mat khau\"}";
                sendResponse(exchange, 200, responseJson);
            } else {
                sendResponse(exchange, 405, "{\"success\": false, \"message\": \"Phuong thuc khong ho tro\"}");
            }
        }
    }

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

    class StatisticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                String jsonResult = "{ \"labels\": [\"To Do\", \"In Progress\", \"Done\"], \"data\": [12, 5, 20] }";
                sendResponse(exchange, 200, jsonResult);
            } else {
                sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
            }
        }
    }

    class CommentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                List<Comment> list = commentDAO.getAllComments();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Comment c = list.get(i);
                    json.append(String.format("{\"id\":\"%s\",\"user\":\"%s\",\"content\":\"%s\",\"time\":\"%s\"}",
                            c.getId(), c.getUser(), c.getContent(), c.getTime()));
                    if (i < list.size() - 1)
                        json.append(",");
                }
                json.append("]");
                sendResponse(exchange, 200, json.toString());
            } else if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                System.out.println("\n--- CO BINH LUAN MOI TU VUE.JS ---");
                System.out.println("[JSON GOC]: " + requestBody);

                try {
                    String user = requestBody.split("\"user\"\\s*:\\s*\"")[1].split("\"")[0];
                    String content = requestBody.split("\"content\"\\s*:\\s*\"")[1].split("\"")[0];
                    String time = requestBody.split("\"time\"\\s*:\\s*\"")[1].split("\"")[0];

                    System.out.println("[BOC TACH]: User=" + user + " | Content=" + content + " | Time=" + time);

                    Comment newComment = new Comment("0", user, content, time);
                    boolean success = commentDAO.addComment(newComment);

                    if (success) {
                        System.out.println("[KET QUA]: Thanh cong luu vao MySQL!");
                        sendResponse(exchange, 200, "{\"success\": true}");
                    } else {
                        System.out.println("[KET QUA]: LỖI! commentDAO.addComment() tra ve false.");
                        System.out.println(
                                "[GỢI Ý]: Kiem tra lai ket noi MySQL (DatabaseConnection.java) hoac loi truy van SQL.");
                        sendResponse(exchange, 500, "{\"success\": false}");
                    }
                } catch (Exception e) {
                    System.out.println("[KET QUA]: LỖI BOC TACH JSON! " + e.getMessage());
                    e.printStackTrace();
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Loi doc du lieu\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"message\": \"Method Not Allowed\"}");
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}