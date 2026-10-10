package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class AdminController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/admin/stats".equals(path) && "GET".equals(method)) {
                handleGetPlatformStats(exchange);
            } else if ("/api/admin/users".equals(path) && "GET".equals(method)) {
                handleGetUsers(exchange);
            } else if (path.startsWith("/api/admin/users/") && "DELETE".equals(method)) {
                int userId = Integer.parseInt(path.substring("/api/admin/users/".length()));
                handleDeleteUser(exchange, userId);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private void handleGetPlatformStats(HttpExchange exchange) throws IOException, SQLException {
        int userCount = 0;
        int sellerCount = 0;
        int productCount = 0;
        int orderCount = 0;
        double totalVolume = 0.0;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'buyer'")) {
                if (rs.next()) userCount = rs.getInt(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'seller'")) {
                if (rs.next()) sellerCount = rs.getInt(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next()) productCount = rs.getInt(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*), COALESCE(SUM(total_amount), 0) FROM orders WHERE status != 'CANCELLED'")) {
                if (rs.next()) {
                    orderCount = rs.getInt(1);
                    totalVolume = rs.getDouble(2);
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBuyers", userCount);
        stats.put("totalSellers", sellerCount);
        stats.put("totalProducts", productCount);
        stats.put("totalOrders", orderCount);
        stats.put("totalVolume", totalVolume);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("stats", stats);
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException, SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, name, email, role, store_name, created_at FROM users ORDER BY id ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getInt("id"));
                u.put("name", rs.getString("name"));
                u.put("email", rs.getString("email"));
                u.put("role", rs.getString("role"));
                u.put("storeName", rs.getString("store_name"));
                u.put("createdAt", rs.getString("created_at"));
                users.add(u);
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("users", users);
        res.put("count", users.size());
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleDeleteUser(HttpExchange exchange, int userId) throws IOException, SQLException {
        if (userId == 1) { // prevent deleting master admin
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Cannot delete master administrator"));
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "User account removed from NAMITHAMART");
                JsonUtils.sendJsonResponse(exchange, 200, res);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "User not found"));
            }
        }
    }
}
