package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.models.CartItem;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class CartController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/cart".equals(path) && "GET".equals(method)) {
                handleGetCart(exchange);
            } else if ("/api/cart/add".equals(path) && "POST".equals(method)) {
                handleAddToCart(exchange);
            } else if ("/api/cart/update".equals(path) && ("PUT".equals(method) || "POST".equals(method))) {
                handleUpdateQuantity(exchange);
            } else if ("/api/cart/remove".equals(path) && ("DELETE".equals(method) || "POST".equals(method))) {
                handleRemoveItem(exchange);
            } else if ("/api/cart/clear".equals(path) && "POST".equals(method)) {
                handleClearCart(exchange);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private void handleGetCart(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        int userId = query.get("userId") != null ? Integer.parseInt(query.get("userId")) : 4; // default demo buyer

        List<CartItem> items = new ArrayList<>();
        double subtotal = 0.0;

        String sql = "SELECT c.id as cart_id, c.user_id, c.product_id, c.quantity, " +
                     "p.title, p.price, p.discount_price, p.image_url, p.seller_id, p.seller_name, p.stock " +
                     "FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double effectivePrice = rs.getDouble("discount_price") > 0 ? rs.getDouble("discount_price") : rs.getDouble("price");
                    CartItem item = new CartItem(
                            rs.getInt("cart_id"),
                            rs.getInt("user_id"),
                            rs.getInt("product_id"),
                            rs.getString("title"),
                            effectivePrice,
                            rs.getString("image_url"),
                            rs.getInt("seller_id"),
                            rs.getString("seller_name"),
                            rs.getInt("quantity"),
                            rs.getInt("stock")
                    );
                    items.add(item);
                    subtotal += effectivePrice * item.getQuantity();
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("items", items);
        res.put("itemCount", items.size());
        res.put("subtotal", subtotal);
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleAddToCart(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Empty body"));
            return;
        }

        int userId = JsonUtils.getInt(body.get("userId"), 4);
        int productId = JsonUtils.getInt(body.get("productId"), 0);
        int quantity = JsonUtils.getInt(body.get("quantity"), 1);

        try (Connection conn = Database.getConnection()) {
            // Check if already in cart
            int existingId = 0;
            int existingQty = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, quantity FROM cart WHERE user_id = ? AND product_id = ?")) {
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                        existingQty = rs.getInt("quantity");
                    }
                }
            }

            if (existingId > 0) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE cart SET quantity = ? WHERE id = ?")) {
                    ps.setInt(1, existingQty + quantity);
                    ps.setInt(2, existingId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, productId);
                    ps.setInt(3, quantity);
                    ps.executeUpdate();
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Product added to NAMITHAMART cart");
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleUpdateQuantity(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Empty body"));
            return;
        }

        int cartId = JsonUtils.getInt(body.get("cartId"), 0);
        int quantity = JsonUtils.getInt(body.get("quantity"), 1);

        if (quantity <= 0) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE id = ?")) {
                ps.setInt(1, cartId);
                ps.executeUpdate();
            }
        } else {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE cart SET quantity = ? WHERE id = ?")) {
                ps.setInt(1, quantity);
                ps.setInt(2, cartId);
                ps.executeUpdate();
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Cart updated");
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleRemoveItem(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String idStr = query.get("id");
        int cartId = 0;
        if (idStr != null) {
            cartId = Integer.parseInt(idStr);
        } else {
            Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
            if (body != null) {
                cartId = JsonUtils.getInt(body.get("id"), 0);
            }
        }

        if (cartId <= 0) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Cart item ID required"));
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE id = ?")) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Item removed from cart");
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleClearCart(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        int userId = query.get("userId") != null ? Integer.parseInt(query.get("userId")) : 4;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Cart cleared");
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }
}
