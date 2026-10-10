package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.models.Product;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SellerController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/seller/stats".equals(path) && "GET".equals(method)) {
                handleGetStats(exchange);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private void handleGetStats(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        int sellerId = query.get("sellerId") != null ? Integer.parseInt(query.get("sellerId")) : 2;

        int productCount = 0;
        int orderCount = 0;
        double totalRevenue = 0.0;
        int lowStockCount = 0;

        try (Connection conn = Database.getConnection()) {
            // Products count and low stock
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*), SUM(CASE WHEN stock < 10 THEN 1 ELSE 0 END) FROM products WHERE seller_id = ?")) {
                ps.setInt(1, sellerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        productCount = rs.getInt(1);
                        lowStockCount = rs.getInt(2);
                    }
                }
            }

            // Total revenue and order count for this seller's items
            String revSql = "SELECT COUNT(DISTINCT oi.order_id) as order_count, SUM(oi.price * oi.quantity) as total_rev " +
                            "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
                            "WHERE oi.seller_id = ? AND o.status != 'CANCELLED'";
            try (PreparedStatement ps = conn.prepareStatement(revSql)) {
                ps.setInt(1, sellerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderCount = rs.getInt("order_count");
                        totalRevenue = rs.getDouble("total_rev");
                    }
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productCount);
        stats.put("totalOrders", orderCount);
        stats.put("totalRevenue", totalRevenue);
        stats.put("lowStockCount", lowStockCount);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("stats", stats);
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }
}
