package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.models.Review;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ReviewController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        try {
            if ("GET".equals(method)) {
                handleGetReviews(exchange);
            } else if ("POST".equals(method)) {
                handleCreateReview(exchange);
            } else {
                JsonUtils.sendJsonResponse(exchange, 405, Collections.singletonMap("error", "Method not allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private void handleGetReviews(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String productIdStr = query.get("productId");

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "productId required"));
            return;
        }

        int productId = Integer.parseInt(productIdStr);
        List<Review> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM reviews WHERE product_id = ? ORDER BY id DESC")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("product_id"),
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getString("created_at")
                    ));
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("reviews", list);
        res.put("count", list.size());
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleCreateReview(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Empty body"));
            return;
        }

        int productId = JsonUtils.getInt(body.get("productId"), 0);
        int userId = JsonUtils.getInt(body.get("userId"), 4);
        String userName = body.get("userName") != null ? (String) body.get("userName") : "NAMITHAMART Customer";
        int rating = JsonUtils.getInt(body.get("rating"), 5);
        String comment = (String) body.get("comment");

        if (comment == null || comment.trim().isEmpty()) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Review comment cannot be empty"));
            return;
        }

        try (Connection conn = Database.getConnection()) {
            // Insert review
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO reviews (product_id, user_id, user_name, rating, comment) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, productId);
                ps.setInt(2, userId);
                ps.setString(3, userName);
                ps.setInt(4, rating);
                ps.setString(5, comment.trim());
                ps.executeUpdate();
            }

            // Recalculate average rating & review count for product
            try (PreparedStatement ps = conn.prepareStatement("SELECT AVG(rating) as avg_r, COUNT(*) as cnt FROM reviews WHERE product_id = ?")) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double avgRating = rs.getDouble("avg_r");
                        int count = rs.getInt("cnt");

                        try (PreparedStatement psUp = conn.prepareStatement("UPDATE products SET rating = ?, review_count = ? WHERE id = ?")) {
                            psUp.setDouble(1, Math.round(avgRating * 10.0) / 10.0);
                            psUp.setInt(2, count);
                            psUp.setInt(3, productId);
                            psUp.executeUpdate();
                        }
                    }
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Review submitted successfully on NAMITHAMART");
        JsonUtils.sendJsonResponse(exchange, 201, res);
    }
}
