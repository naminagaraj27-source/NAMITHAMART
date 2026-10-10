package com.namithamart;

import com.namithamart.controllers.*;
import com.namithamart.utils.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        } else if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("==================================================================");
        System.out.println("              NAMITHAMART FULL-STACK PLATFORM                    ");
        System.out.println("          Multi-Seller Online Shopping Architecture               ");
        System.out.println("==================================================================");

        // 1. Initialize SQLite Database
        Database.initDatabase();

        // 2. Start HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // API Endpoints
            server.createContext("/api/auth", new AuthController());
            server.createContext("/api/products", new ProductController());
            server.createContext("/api/cart", new CartController());
            server.createContext("/api/orders", new OrderController());
            server.createContext("/api/reviews", new ReviewController());
            server.createContext("/api/seller", new SellerController());
            server.createContext("/api/admin", new AdminController());
            server.createContext("/api/chat", new ChatController());

            // Static Frontend Files
            File publicDir = new File("public");
            if (!publicDir.exists()) {
                publicDir.mkdirs();
            }
            server.createContext("/", new StaticFileHandler("public"));

            // Executor with multiple worker threads
            server.setExecutor(Executors.newFixedThreadPool(16));
            server.start();

            System.out.println("[Server] NAMITHAMART Server successfully started!");
            System.out.println("[Server] Open your browser at: http://localhost:" + port);
            System.out.println("[Server] Active Theme: Midnight Orchid (#312A44) & Dusky Lilac (#88708E)");
            System.out.println("==================================================================");

        } catch (IOException e) {
            System.err.println("[Server] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
