package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.models.User;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AuthController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/auth/login".equals(path) && "POST".equals(method)) {
                handleLogin(exchange);
            } else if ("/api/auth/register".equals(path) && "POST".equals(method)) {
                handleRegister(exchange);
            } else if ("/api/auth/me".equals(path) && "GET".equals(method)) {
                handleMe(exchange);
            } else if ("/api/auth/logout".equals(path) && "POST".equals(method)) {
                handleLogout(exchange);
            } else {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "Endpoint not found");
                JsonUtils.sendJsonResponse(exchange, 404, err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            JsonUtils.sendJsonResponse(exchange, 500, err);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> body = JsonUtils.readRequestBody(exchange, Map.class);
        String email = body != null ? body.get("email") : null;
        String password = body != null ? body.get("password") : null;

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", "Email and password are required");
            JsonUtils.sendJsonResponse(exchange, 400, res);
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, password.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("name", rs.getString("name"));
                    user.put("email", rs.getString("email"));
                    user.put("role", rs.getString("role"));
                    user.put("storeName", rs.getString("store_name"));

                    // Generate lightweight session token
                    String token = "NM-TOKEN-" + rs.getInt("id") + "-" + System.currentTimeMillis();

                    Map<String, Object> res = new HashMap<>();
                    res.put("success", true);
                    res.put("message", "Login successful. Welcome to NAMITHAMART!");
                    res.put("token", token);
                    res.put("user", user);

                    JsonUtils.sendJsonResponse(exchange, 200, res);
                    return;
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", "Invalid email or password");
        JsonUtils.sendJsonResponse(exchange, 401, res);
    }

    private void handleRegister(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> body = JsonUtils.readRequestBody(exchange, Map.class);
        String name = body != null ? body.get("name") : null;
        String email = body != null ? body.get("email") : null;
        String password = body != null ? body.get("password") : null;
        String role = body != null ? body.get("role") : "buyer";
        String storeName = body != null ? body.get("storeName") : "";

        if (name == null || email == null || password == null ||
                name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", "Name, email, and password are required");
            JsonUtils.sendJsonResponse(exchange, 400, res);
            return;
        }

        if (!"buyer".equalsIgnoreCase(role) && !"seller".equalsIgnoreCase(role)) {
            role = "buyer";
        }

        try (Connection conn = Database.getConnection()) {
            // Check if email exists
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                check.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> res = new HashMap<>();
                        res.put("success", false);
                        res.put("message", "An account with this email already exists on NAMITHAMART");
                        JsonUtils.sendJsonResponse(exchange, 409, res);
                        return;
                    }
                }
            }

            // Insert new user
            String sql = "INSERT INTO users (name, email, password, role, store_name) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name.trim());
                ps.setString(2, email.trim().toLowerCase());
                ps.setString(3, password.trim());
                ps.setString(4, role.toLowerCase());
                ps.setString(5, storeName != null ? storeName.trim() : "");
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", id);
                        user.put("name", name.trim());
                        user.put("email", email.trim().toLowerCase());
                        user.put("role", role.toLowerCase());
                        user.put("storeName", storeName != null ? storeName.trim() : "");

                        String token = "NM-TOKEN-" + id + "-" + System.currentTimeMillis();

                        Map<String, Object> res = new HashMap<>();
                        res.put("success", true);
                        res.put("message", "Registration successful. Welcome to NAMITHAMART!");
                        res.put("token", token);
                        res.put("user", user);

                        JsonUtils.sendJsonResponse(exchange, 201, res);
                        return;
                    }
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", "Registration failed");
        JsonUtils.sendJsonResponse(exchange, 500, res);
    }

    private void handleMe(HttpExchange exchange) throws IOException, SQLException {
        // Query param or header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String userIdStr = query.get("userId");

        int userId = -1;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try { userId = Integer.parseInt(userIdStr); } catch (Exception ignored) {}
        } else if (authHeader != null && authHeader.startsWith("Bearer NM-TOKEN-")) {
            String[] parts = authHeader.replace("Bearer NM-TOKEN-", "").split("-");
            if (parts.length > 0) {
                try { userId = Integer.parseInt(parts[0]); } catch (Exception ignored) {}
            }
        }

        if (userId > 0) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT id, name, email, role, store_name FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", rs.getInt("id"));
                        user.put("name", rs.getString("name"));
                        user.put("email", rs.getString("email"));
                        user.put("role", rs.getString("role"));
                        user.put("storeName", rs.getString("store_name"));

                        Map<String, Object> res = new HashMap<>();
                        res.put("success", true);
                        res.put("user", user);
                        JsonUtils.sendJsonResponse(exchange, 200, res);
                        return;
                    }
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", "User not authenticated");
        JsonUtils.sendJsonResponse(exchange, 401, res);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Logged out from NAMITHAMART successfully");
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }
}
