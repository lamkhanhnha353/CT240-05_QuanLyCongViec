package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.teamwork.db.DashboardDAO;
import com.teamwork.db.TaskDAO;
import java.io.IOException;

public class DashboardHandler extends BaseHandler {

    public void handleDashboardSummary(HttpExchange ex) throws IOException {
        handleCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String userIdStr = ex.getRequestHeaders().getFirst("User-ID");
            int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr) : 0;
            sendResponse(ex, 200, new DashboardDAO().getDashboardSummary(userId));
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public void handleStatistics(HttpExchange exchange) throws IOException {
        handleCors(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            int projectId = 0;
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("projectId="))
                projectId = Integer.parseInt(query.split("projectId=")[1].split("&")[0]);

            int[] stats = new TaskDAO().getTaskStatistics(projectId);
            String json = String.format("{\"todo\": %d, \"inProgress\": %d, \"done\": %d}", stats[0], stats[1],
                    stats[2]);
            sendResponse(exchange, 200, json);
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}