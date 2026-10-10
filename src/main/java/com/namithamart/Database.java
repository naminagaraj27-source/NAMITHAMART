package com.namithamart;

import java.sql.*;
import java.io.File;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:namithamart.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable WAL mode for better concurrency
            stmt.execute("PRAGMA journal_mode=WAL;");

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'buyer', " +
                    "store_name TEXT DEFAULT '', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // Products table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "category TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "discount_price REAL DEFAULT 0, " +
                    "stock INTEGER NOT NULL DEFAULT 10, " +
                    "image_url TEXT, " +
                    "seller_id INTEGER NOT NULL, " +
                    "seller_name TEXT, " +
                    "rating REAL DEFAULT 4.5, " +
                    "review_count INTEGER DEFAULT 0, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(seller_id) REFERENCES users(id)" +
                    ");");

            // Cart table
            stmt.execute("CREATE TABLE IF NOT EXISTS cart (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id), " +
                    "FOREIGN KEY(product_id) REFERENCES products(id)" +
                    ");");

            // Orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_number TEXT UNIQUE NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "customer_name TEXT NOT NULL, " +
                    "customer_email TEXT NOT NULL, " +
                    "shipping_address TEXT NOT NULL, " +
                    "payment_method TEXT NOT NULL, " +
                    "total_amount REAL NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'PLACED', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");");

            // Order items table
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "product_title TEXT NOT NULL, " +
                    "product_image_url TEXT, " +
                    "seller_id INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(id), " +
                    "FOREIGN KEY(product_id) REFERENCES products(id)" +
                    ");");

            // Reviews table
            stmt.execute("CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "user_name TEXT NOT NULL, " +
                    "rating INTEGER NOT NULL, " +
                    "comment TEXT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(product_id) REFERENCES products(id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");");

            seedInitialData(conn);
            System.out.println("[Database] NAMITHAMART SQLite database initialized and seeded successfully.");
        } catch (SQLException e) {
            System.err.println("[Database] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Check if users exist
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        }

        // Seed users
        String insertUser = "INSERT INTO users (name, email, password, role, store_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            // Admin
            ps.setString(1, "NAMITHAMART Admin");
            ps.setString(2, "admin@namithamart.com");
            ps.setString(3, "admin123");
            ps.setString(4, "admin");
            ps.setString(5, "NAMITHAMART Platform");
            ps.executeUpdate();

            // Seller 1
            ps.setString(1, "Namitha Tech Seller");
            ps.setString(2, "seller@namithamart.com");
            ps.setString(3, "seller123");
            ps.setString(4, "seller");
            ps.setString(5, "Namitha Electronics Hub");
            ps.executeUpdate();

            // Seller 2
            ps.setString(1, "Iris Artisan Studio");
            ps.setString(2, "artisan@namithamart.com");
            ps.setString(3, "seller123");
            ps.setString(4, "seller");
            ps.setString(5, "Lilac Living & Crafts");
            ps.executeUpdate();

            // Buyer
            ps.setString(1, "Namitha N.");
            ps.setString(2, "buyer@namithamart.com");
            ps.setString(3, "buyer123");
            ps.setString(4, "buyer");
            ps.setString(5, "");
            ps.executeUpdate();
        }

        // Seed products
        String insertProduct = "INSERT INTO products (title, description, category, price, discount_price, stock, image_url, seller_id, seller_name, rating, review_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertProduct)) {
            // Product 1
            ps.setString(1, "Noise Cancelling Wireless Headphones");
            ps.setString(2, "Premium over-ear wireless headphones with active noise cancellation, 40-hour battery life, and crystal-clear acoustic fidelity.");
            ps.setString(3, "Electronics");
            ps.setDouble(4, 3499.00);
            ps.setDouble(5, 2799.00);
            ps.setInt(6, 45);
            ps.setString(7, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 2);
            ps.setString(9, "Namitha Electronics Hub");
            ps.setDouble(10, 4.8);
            ps.setInt(11, 24);
            ps.executeUpdate();

            // Product 2
            ps.setString(1, "Ultra-Slim 4K Smart Monitor 27-inch");
            ps.setString(2, "Vibrant IPS 4K HDR display with USB-C 65W charging, 99% sRGB color gamut, and ultra-narrow orchid bezel design.");
            ps.setString(3, "Electronics");
            ps.setDouble(4, 18999.00);
            ps.setDouble(5, 16499.00);
            ps.setInt(6, 18);
            ps.setString(7, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 2);
            ps.setString(9, "Namitha Electronics Hub");
            ps.setDouble(10, 4.9);
            ps.setInt(11, 38);
            ps.executeUpdate();

            // Product 3
            ps.setString(1, "Lilac Blossom Embroidered Kurti");
            ps.setString(2, "Handcrafted pure cotton tunic adorned with delicate floral thread work in dusky lilac tones. Breathable and comfortable for all occasions.");
            ps.setString(3, "Fashion");
            ps.setDouble(4, 1799.00);
            ps.setDouble(5, 1299.00);
            ps.setInt(6, 60);
            ps.setString(7, "https://images.unsplash.com/photo-1618932260643-eee4a2f652a6?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.7);
            ps.setInt(11, 19);
            ps.executeUpdate();

            // Product 4
            ps.setString(1, "Midnight Orchid Formal Blazer");
            ps.setString(2, "Tailored premium blazer in deep midnight orchid shade with satin lapels, structured fit, and wrinkle-resistant fabric.");
            ps.setString(3, "Fashion");
            ps.setDouble(4, 4999.00);
            ps.setDouble(5, 3899.00);
            ps.setInt(6, 25);
            ps.setString(7, "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.6);
            ps.setInt(11, 14);
            ps.executeUpdate();

            // Product 5
            ps.setString(1, "Aromatherapy Ceramic Mist Diffuser");
            ps.setString(2, "Ultrasonic cold mist diffuser crafted in glazed ceramic. 7 ambient ambient light modes, whisper-quiet operation, and timer functions.");
            ps.setString(3, "Home & Living");
            ps.setDouble(4, 2199.00);
            ps.setDouble(5, 1699.00);
            ps.setInt(6, 35);
            ps.setString(7, "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.9);
            ps.setInt(11, 31);
            ps.executeUpdate();

            // Product 6
            ps.setString(1, "Minimalist Iris Bedside Table Lamp");
            ps.setString(2, "Warm ambient lighting featuring touch dimming, brushed brass base, and textured lilac linen shade. Perfect for contemporary interiors.");
            ps.setString(3, "Home & Living");
            ps.setDouble(4, 1899.00);
            ps.setDouble(5, 1499.00);
            ps.setInt(6, 40);
            ps.setString(7, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.8);
            ps.setInt(11, 22);
            ps.executeUpdate();

            // Product 7
            ps.setString(1, "Orchid Botanical Hydrating Serum 50ml");
            ps.setString(2, "Infused with organic orchid extract, hyaluronic acid, and vitamin B5 for intense cellular hydration and glowing radiance.");
            ps.setString(3, "Beauty & Wellness");
            ps.setDouble(4, 1299.00);
            ps.setDouble(5, 999.00);
            ps.setInt(6, 80);
            ps.setString(7, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.9);
            ps.setInt(11, 45);
            ps.executeUpdate();

            // Product 8
            ps.setString(1, "Mechanical RGB Gaming Keyboard (Tactile)");
            ps.setString(2, "Custom purple tactile switches, PBT double-shot keycaps, aluminum frame, and customizable per-key RGB backlighting.");
            ps.setString(3, "Electronics");
            ps.setDouble(4, 4599.00);
            ps.setDouble(5, 3699.00);
            ps.setInt(6, 30);
            ps.setString(7, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 2);
            ps.setString(9, "Namitha Electronics Hub");
            ps.setDouble(10, 4.7);
            ps.setInt(11, 18);
            ps.executeUpdate();

            // Product 9
            ps.setString(1, "Handmade Lilac Glazed Ceramic Mug Set");
            ps.setString(2, "Set of 2 handcrafted ceramic mugs with comfortable handles and dusky lilac matte glaze. Microwave and dishwasher safe.");
            ps.setString(3, "Home & Living");
            ps.setDouble(4, 999.00);
            ps.setDouble(5, 799.00);
            ps.setInt(6, 50);
            ps.setString(7, "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.8);
            ps.setInt(11, 27);
            ps.executeUpdate();

            // Product 10
            ps.setString(1, "Hardcover Executive Daily Planner & Journal");
            ps.setString(2, "Gold-embossed midnight orchid vegan leather cover with 120gsm bleed-proof ivory paper, ribbon bookmarks, and inner pocket.");
            ps.setString(3, "Books & Stationery");
            ps.setDouble(4, 899.00);
            ps.setDouble(5, 649.00);
            ps.setInt(6, 75);
            ps.setString(7, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.9);
            ps.setInt(11, 36);
            ps.executeUpdate();

            // Product 11
            ps.setString(1, "Smart Fitness Tracker Band 7 Pro");
            ps.setString(2, "Always-on AMOLED touch display, continuous SpO2 & heart rate monitor, 120 workout modes, and 14-day battery endurance.");
            ps.setString(3, "Electronics");
            ps.setDouble(4, 2999.00);
            ps.setDouble(5, 2399.00);
            ps.setInt(6, 40);
            ps.setString(7, "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 2);
            ps.setString(9, "Namitha Electronics Hub");
            ps.setDouble(10, 4.6);
            ps.setInt(11, 29);
            ps.executeUpdate();

            // Product 12
            ps.setString(1, "Iris Botanical Herbal Clay Face Mask 100g");
            ps.setString(2, "Purifying kaolin and bentonite clay infused with iris root and chamomile to unclog pores and brighten dull complexion.");
            ps.setString(3, "Beauty & Wellness");
            ps.setDouble(4, 849.00);
            ps.setDouble(5, 699.00);
            ps.setInt(6, 65);
            ps.setString(7, "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80");
            ps.setInt(8, 3);
            ps.setString(9, "Lilac Living & Crafts");
            ps.setDouble(10, 4.8);
            ps.setInt(11, 21);
            ps.executeUpdate();
        }

        // Seed sample reviews
        String insertReview = "INSERT INTO reviews (product_id, user_id, user_name, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertReview)) {
            ps.setInt(1, 1);
            ps.setInt(2, 4);
            ps.setString(3, "Namitha N.");
            ps.setInt(4, 5);
            ps.setString(5, "Exceptional sound quality and the midnight orchid finish looks so elegant! Best purchase on NAMITHAMART.");
            ps.executeUpdate();

            ps.setInt(1, 3);
            ps.setInt(2, 4);
            ps.setString(3, "Namitha N.");
            ps.setInt(4, 5);
            ps.setString(5, "The embroidery is top notch, fabric is pure cotton and soft. Prompt delivery from Lilac Living & Crafts.");
            ps.executeUpdate();

            ps.setInt(1, 5);
            ps.setInt(2, 4);
            ps.setString(3, "Rohan Verma");
            ps.setInt(4, 4);
            ps.setString(5, "Fills the entire bedroom with relaxing aroma and the ambient light modes are very soothing.");
            ps.executeUpdate();
        }

        // Seed sample order
        String insertOrder = "INSERT INTO orders (order_number, user_id, customer_name, customer_email, shipping_address, payment_method, total_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "NM-ORD-88201");
            ps.setInt(2, 4);
            ps.setString(3, "Namitha N.");
            ps.setString(4, "buyer@namithamart.com");
            ps.setString(5, "42 Orchids Avenue, Indiranagar, Bengaluru 560038");
            ps.setString(6, "UPI / Online Banking");
            ps.setDouble(7, 4098.00);
            ps.setString(8, "DELIVERED");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    String insertItem = "INSERT INTO order_items (order_id, product_id, product_title, product_image_url, seller_id, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement itemPs = conn.prepareStatement(insertItem)) {
                        itemPs.setInt(1, orderId);
                        itemPs.setInt(2, 1);
                        itemPs.setString(3, "Noise Cancelling Wireless Headphones");
                        itemPs.setString(4, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80");
                        itemPs.setInt(5, 2);
                        itemPs.setDouble(6, 2799.00);
                        itemPs.setInt(7, 1);
                        itemPs.executeUpdate();

                        itemPs.setInt(1, orderId);
                        itemPs.setInt(2, 3);
                        itemPs.setString(3, "Lilac Blossom Embroidered Kurti");
                        itemPs.setString(4, "https://images.unsplash.com/photo-1618932260643-eee4a2f652a6?w=600&auto=format&fit=crop&q=80");
                        itemPs.setInt(5, 3);
                        itemPs.setDouble(6, 1299.00);
                        itemPs.setInt(7, 1);
                        itemPs.executeUpdate();
                    }
                }
            }
        }
    }
}
