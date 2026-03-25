package com.teamwork.handlers;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseHandler {

    protected void handleCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, User-ID");
    }

    protected String extract(String json, String key) {
        Matcher mString = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (mString.find())
            return mString.group(1).trim();

        Matcher mNum = Pattern.compile("\"" + key + "\"\\s*:\\s*([a-zA-Z0-9\\.]+)").matcher(json);
        if (mNum.find())
            return mNum.group(1).trim();
        return "";
    }

    protected String extractQuery(String query, String key) {
        if (query == null)
            return "";
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && pair[0].equals(key))
                return pair[1];
        }
        return "";
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected String escapeJson(String data) {
        if (data == null)
            return "";
        return data.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}