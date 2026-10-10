package com.namithamart.controllers;

import com.namithamart.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.*;

public class ChatController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            JsonUtils.sendOptionsResponse(exchange);
            return;
        }

        if (!"POST".equals(method)) {
            JsonUtils.sendJsonResponse(exchange, 405, Collections.singletonMap("error", "Method not allowed"));
            return;
        }

        try {
            Map<String, String> body = JsonUtils.readRequestBody(exchange, Map.class);
            String message = body != null ? body.get("message") : "";

            String reply = generateBotReply(message != null ? message.trim() : "");

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("reply", reply);
            res.put("timestamp", System.currentTimeMillis());

            JsonUtils.sendJsonResponse(exchange, 200, res);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtils.sendJsonResponse(exchange, 500, Collections.singletonMap("error", e.getMessage()));
        }
    }

    private String generateBotReply(String msg) {
        String lower = msg.toLowerCase();

        if (lower.isEmpty() || lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hi! I'm NAMITHAMART AI Assistant. How can I help you? I can assist you with orders, product recommendations, returns, and multi-seller store inquiries.";
        }

        if (lower.contains("order") || lower.contains("track") || lower.contains("status")) {
            return "You can track your orders by navigating to the Buyer Dashboard under 'My Orders'. Each order shows real-time tracking badges: PLACED, PROCESSING, SHIPPED, or DELIVERED.";
        }

        if (lower.contains("seller") || lower.contains("sell") || lower.contains("vendor") || lower.contains("register as seller")) {
            return "NAMITHAMART is a multi-seller platform! You can join as a seller by clicking 'Register' and selecting 'Seller Account'. Once registered, you get access to the Seller Dashboard to upload products and manage orders.";
        }

        if (lower.contains("return") || lower.contains("refund") || lower.contains("exchange")) {
            return "NAMITHAMART offers an easy 7-day return policy for eligible products. If your item is damaged or not as described, you can initiate a return from your Buyer Dashboard or contact our help desk.";
        }

        if (lower.contains("pay") || lower.contains("payment") || lower.contains("cod") || lower.contains("upi") || lower.contains("card")) {
            return "NAMITHAMART accepts Cash on Delivery (COD), UPI, Net Banking, and Credit/Debit Cards with 256-bit secure encryption.";
        }

        if (lower.contains("discount") || lower.contains("coupon") || lower.contains("offer")) {
            return "Great news! All products on NAMITHAMART currently feature special promotional pricing up to 25% off across Electronics, Fashion, and Home collections.";
        }

        if (lower.contains("contact") || lower.contains("support") || lower.contains("help") || lower.contains("phone") || lower.contains("email")) {
            return "You can reach the NAMITHAMART customer care team at support@namithamart.com or call our toll-free helpline at 1800-NAMITHA-MART.";
        }

        if (lower.contains("who are you") || lower.contains("name")) {
            return "I am the official NAMITHAMART AI Assistant, designed to help buyers and sellers navigate the NAMITHAMART multi-seller shopping ecosystem.";
        }

        return "Thank you for reaching out to NAMITHAMART! Feel free to ask about our curated products, order status, seller onboarding, or shipping details.";
    }
}
