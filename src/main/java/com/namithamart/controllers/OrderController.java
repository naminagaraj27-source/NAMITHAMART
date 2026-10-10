package com.namithamart.controllers;

import com.namithamart.Database;
import com.namithamart.models.Order;
import com.namithamart.models.OrderItem;
import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class OrderController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/orders/checkout".equals(path) && "POST".equals(method)) {
                handleCheckout(exchange);
            } else if ("/api/orders".equals(path) && "GET".equals(method)) {
                handleGetOrders(exchange);
            } else if (path.startsWith("/api/orders/") && path.endsWith("/status") && ("PUT".equals(method) || "POST".equals(method))) {
                String sub = path.substring("/api/orders/".length());
                String idStr = sub.substring(0, sub.indexOf("/status"));
                handleUpdateStatus(exchange, Integer.parseInt(idStr));
            } else if (path.startsWith("/api/orders/") && "GET".equals(method)) {
                String idStr = path.substring("/api/orders/".length());
                handleGetOrderById(exchange, Integer.parseInt(idStr));
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Endpoint not found"));
            }
        } catch (NumberFormatException nfe) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Invalid order ID format"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private void handleCheckout(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Empty checkout body"));
            return;
        }

        int userId = JsonUtils.getInt(body.get("userId"), 4);
        String customerName = (String) body.get("customerName");
        String customerEmail = (String) body.get("customerEmail");
        String shippingAddress = (String) body.get("shippingAddress");
        String paymentMethod = (String) body.get("paymentMethod");
        double totalAmount = JsonUtils.getDouble(body.get("totalAmount"), 0.0);
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) body.get("items");

        if (customerName == null || customerName.trim().isEmpty() ||
            shippingAddress == null || shippingAddress.trim().isEmpty() ||
            itemsList == null || itemsList.isEmpty()) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Missing required checkout fields or cart is empty"));
            return;
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "Cash on Delivery";
        }

        String orderNumber = "NM-" + System.currentTimeMillis() % 1000000;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert order
                String orderSql = "INSERT INTO orders (order_number, user_id, customer_name, customer_email, shipping_address, payment_method, total_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'PLACED')";
                int orderId = 0;
                try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, orderNumber);
                    ps.setInt(2, userId);
                    ps.setString(3, customerName.trim());
                    ps.setString(4, customerEmail != null ? customerEmail.trim() : "buyer@namithamart.com");
                    ps.setString(5, shippingAddress.trim());
                    ps.setString(6, paymentMethod.trim());
                    ps.setDouble(7, totalAmount);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            orderId = rs.getInt(1);
                        }
                    }
                }

                // Insert items & reduce stock
                String itemSql = "INSERT INTO order_items (order_id, product_id, product_title, product_image_url, seller_id, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
                String stockSql = "UPDATE products SET stock = MAX(0, stock - ?) WHERE id = ?";

                try (PreparedStatement psItem = conn.prepareStatement(itemSql);
                     PreparedStatement psStock = conn.prepareStatement(stockSql)) {
                    for (Map<String, Object> it : itemsList) {
                        int productId = JsonUtils.getInt(it.get("productId"), 0);
                        String title = (String) it.get("productTitle");
                        String img = (String) it.get("imageUrl");
                        int sellerId = JsonUtils.getInt(it.get("sellerId"), 2);
                        double price = JsonUtils.getDouble(it.get("price"), 0.0);
                        int qty = JsonUtils.getInt(it.get("quantity"), 1);

                        psItem.setInt(1, orderId);
                        psItem.setInt(2, productId);
                        psItem.setString(3, title != null ? title : "Product #" + productId);
                        psItem.setString(4, img != null ? img : "");
                        psItem.setInt(5, sellerId);
                        psItem.setDouble(6, price);
                        psItem.setInt(7, qty);
                        psItem.executeUpdate();

                        psStock.setInt(1, qty);
                        psStock.setInt(2, productId);
                        psStock.executeUpdate();
                    }
                }

                // Clear user cart in DB
                try (PreparedStatement psClear = conn.prepareStatement("DELETE FROM cart WHERE user_id = ?")) {
                    psClear.setInt(1, userId);
                    psClear.executeUpdate();
                }

                conn.commit();

                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Order placed successfully on NAMITHAMART!");
                res.put("orderId", orderId);
                res.put("orderNumber", orderNumber);
                res.put("totalAmount", totalAmount);
                JsonUtils.sendJsonResponse(exchange, 201, res);

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void handleGetOrders(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = JsonUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String userIdStr = query.get("userId");
        String sellerIdStr = query.get("sellerId");

        List<Order> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
        List<Object> args = new ArrayList<>();

        if (userIdStr != null && !userIdStr.trim().isEmpty()) {
            sql.append(" AND user_id = ?");
            args.add(Integer.parseInt(userIdStr));
        }

        sql.append(" ORDER BY id DESC");

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("id"),
                            rs.getString("order_number"),
                            rs.getInt("user_id"),
                            rs.getString("customer_name"),
                            rs.getString("customer_email"),
                            rs.getString("shipping_address"),
                            rs.getString("payment_method"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    );
                    orders.add(order);
                }
            }
        }

        // Populate items for orders
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM order_items WHERE order_id = ?")) {
            for (Order o : orders) {
                ps.setInt(1, o.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    List<OrderItem> items = new ArrayList<>();
                    while (rs.next()) {
                        OrderItem item = new OrderItem(
                                rs.getInt("id"),
                                rs.getInt("order_id"),
                                rs.getInt("product_id"),
                                rs.getString("product_title"),
                                rs.getString("product_image_url"),
                                rs.getInt("seller_id"),
                                rs.getDouble("price"),
                                rs.getInt("quantity")
                        );
                        items.add(item);
                    }
                    o.setItems(items);
                }
            }
        }

        // Filter seller orders if sellerId is specified
        if (sellerIdStr != null && !sellerIdStr.trim().isEmpty()) {
            int sellerId = Integer.parseInt(sellerIdStr);
            List<Order> sellerOrders = new ArrayList<>();
            for (Order o : orders) {
                boolean hasSellerItem = false;
                for (OrderItem item : o.getItems()) {
                    if (item.getSellerId() == sellerId) {
                        hasSellerItem = true;
                        break;
                    }
                }
                if (hasSellerItem) {
                    sellerOrders.add(o);
                }
            }
            orders = sellerOrders;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("orders", orders);
        res.put("count", orders.size());
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleGetOrderById(HttpExchange exchange, int id) throws IOException, SQLException {
        Order order = null;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    order = new Order(
                            rs.getInt("id"),
                            rs.getString("order_number"),
                            rs.getInt("user_id"),
                            rs.getString("customer_name"),
                            rs.getString("customer_email"),
                            rs.getString("shipping_address"),
                            rs.getString("payment_method"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    );
                }
            }
        }

        if (order == null) {
            JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Order not found"));
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM order_items WHERE order_id = ?")) {
            ps.setInt(1, order.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_title"),
                            rs.getString("product_image_url"),
                            rs.getInt("seller_id"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    ));
                }
                order.setItems(items);
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("order", order);
        JsonUtils.sendJsonResponse(exchange, 200, res);
    }

    private void handleUpdateStatus(HttpExchange exchange, int id) throws IOException, SQLException {
        Map<String, Object> body = JsonUtils.readRequestBody(exchange, Map.class);
        if (body == null || body.get("status") == null) {
            JsonUtils.sendJsonResponse(exchange, 400, Collections.singletonMap("error", "New status required"));
            return;
        }

        String newStatus = body.get("status").toString().toUpperCase();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Order status updated to " + newStatus);
                JsonUtils.sendJsonResponse(exchange, 200, res);
            } else {
                JsonUtils.sendJsonResponse(exchange, 404, Collections.singletonMap("error", "Order not found"));
            }
        }
    }
}
