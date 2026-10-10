package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.models.Product;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ProductController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/products/categories".equals(path) && "GET".equals(method)) {
                handleGetCategories(exchange);
            } else if ("/api/products".equals(path)) {
                if ("GET".equals(method)) {
                    handleGetProducts(exchange);
                } else if ("POST".equals(method)) {
                    handleCreateProduct(exchange);
                } else {
                    JsonUtils.sendJsonResponse(exchange, 405, Collections.singletonMap("error", "Method not allowed"));
                }
            } else if (path.startsWith("/api/products/")) {
                String idStr = path.substring("/api/products/".length());
                int productId = Integer.parseInt(idStr);

                if ("GET".equals(method)) {
                    handleGetProductById(exchange, productId);
                } else if ("PUT".equals(method)) {
                    handleUpdateProduct(exchange, productId);
                } else if ("DELETE".equals(method)) {
                    handleDeleteProduct(exchange, productId);
                } else {
                    JsonUtils.sendJsonResponse(exchange, 405, Collections.singletonMap("error", "Method not allowed"));
                }
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Endpoint not found"));
            }
        } catch (NumberFormatException nfe) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Invalid product ID format"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private void handleGetProducts(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> params = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String category = params.get("category");
        String search = params.get("search");
        String sellerIdStr = params.get("sellerId");
        String sort = params.get("sort");

        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> args = new ArrayList<>();

        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql.append(" AND LOWER(category) = LOWER(?)");
            args.add(category.trim());
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(category) LIKE ?)");
            String term = "%" + search.trim().toLowerCase() + "%";
            args.add(term);
            args.add(term);
            args.add(term);
        }

        if (sellerIdStr != null && !sellerIdStr.trim().isEmpty()) {
            try {
                int sellerId = Integer.parseInt(sellerIdStr);
                sql.append(" AND seller_id = ?");
                args.add(sellerId);
            } catch (Exception ignored) {}
        }

        if ("price_asc".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY price ASC");
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY price DESC");
        } else if ("rating".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY rating DESC");
        } else {
            sql.append(" ORDER BY id DESC");
        }

        List<Product> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("count", list.size());
        res.put("products", list);
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleGetProductById(HttpExchange exchange, int id) throws IOException, SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = mapRowToProduct(rs);
                    Map<String, Object> res = new HashMap<>();
                    res.put("success", true);
                    res.put("product", p);
                    JsonUtils.sendJsonResponse(exchange, 200, res);
                    return;
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", "Product not found");
        JsonUtils.sendJsonResponse(exchange, 404, res);
    }

    private void handleGetCategories(HttpExchange exchange) throws IOException, SQLException {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT category, COUNT(*) as count FROM products GROUP BY category ORDER BY count DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("name", rs.getString("category"));
                cat.put("count", rs.getInt("count"));
                categories.add(cat);
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("categories", categories);
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleCreateProduct(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Request body empty"));
            return;
        }

        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String category = (String) body.get("category");
        double price = JsonUtils.getDouble(body.get("price"), 0.0);
        double discountPrice = JsonUtils.getDouble(body.get("discountPrice"), 0.0);
        int stock = JsonUtils.getInt(body.get("stock"), 10);
        String imageUrl = (String) body.get("imageUrl");
        int sellerId = JsonUtils.getInt(body.get("sellerId"), 2);
        String sellerName = (String) body.get("sellerName");

        if (title == null || title.trim().isEmpty() || category == null || price <= 0) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Title, category, and valid price are required"));
            return;
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80";
        }

        if (sellerName == null || sellerName.trim().isEmpty()) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT store_name, name FROM users WHERE id = ?")) {
                ps.setInt(1, sellerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String sn = rs.getString("store_name");
                        sellerName = (sn != null && !sn.isEmpty()) ? sn : rs.getString("name");
                    } else {
                        sellerName = "NAMITHAMART Verified Seller";
                    }
                }
            }
        }

        String sql = "INSERT INTO products (title, description, category, price, discount_price, stock, image_url, seller_id, seller_name, rating, review_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 5.0, 0)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title.trim());
            ps.setString(2, description != null ? description.trim() : "");
            ps.setString(3, category.trim());
            ps.setDouble(4, price);
            ps.setDouble(5, discountPrice > 0 ? discountPrice : price);
            ps.setInt(6, stock);
            ps.setString(7, imageUrl.trim());
            ps.setInt(8, sellerId);
            ps.setString(9, sellerName);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    Map<String, Object> res = new HashMap<>();
                    res.put("success", true);
                    res.put("message", "Product added to NAMITHAMART successfully");
                    res.put("productId", id);
                    JsonUtils.sendJsonResponse(exchange, 201, res);
                    return;
                }
            }
        }

        JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", "Failed to create product"));
    }

    private void handleUpdateProduct(HttpExchange exchange, int id) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Request body empty"));
            return;
        }

        String sql = "UPDATE products SET title = ?, description = ?, category = ?, price = ?, discount_price = ?, stock = ?, image_url = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (String) body.get("title"));
            ps.setString(2, (String) body.get("description"));
            ps.setString(3, (String) body.get("category"));
            ps.setDouble(4, JsonUtils.getDouble(body.get("price"), 0.0));
            ps.setDouble(5, JsonUtils.getDouble(body.get("discountPrice"), 0.0));
            ps.setInt(6, JsonUtils.getInt(body.get("stock"), 10));
            ps.setString(7, (String) body.get("imageUrl"));
            ps.setInt(8, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Product updated successfully");
                JsonUtils.sendJsonResponse(exchange, 200, res);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Product not found"));
            }
        }
    }

    private void handleDeleteProduct(HttpExchange exchange, int id) throws IOException, SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Product deleted from NAMITHAMART");
                JsonUtils.sendJsonResponse(exchange, 200, res);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Product not found"));
            }
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getDouble("discount_price"),
                rs.getInt("stock"),
                rs.getString("image_url"),
                rs.getInt("seller_id"),
                rs.getString("seller_name"),
                rs.getDouble("rating"),
                rs.getInt("review_count"),
                rs.getString("created_at")
        );
    }
}
