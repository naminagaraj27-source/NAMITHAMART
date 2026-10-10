# NAMITHAMART — Multi-Seller Online Shopping Platform

Welcome to **NAMITHAMART**, a complete full-stack multi-seller e-commerce web platform engineered with a robust backend architecture, embedded SQLite database, responsive frontend, and an integrated AI shopping assistant.

---

## 🎨 Professional Purple / Lilac Design System

NAMITHAMART features a tailored, high-contrast, elegant color palette applied consistently across all buyer, seller, and administrator interfaces:

| Color Name | Hex Code | Purpose |
| :--- | :--- | :--- |
| **Midnight Orchid** | `#312A44` | Primary brand color, Navigation bar, Headings, Primary CTA buttons |
| **Dusky Lilac** | `#88708E` | Secondary brand color, Hover states, Sub-navigation, Tag borders |
| **Iris Mist** | `#BAB0C8` | Light accents, Form input borders, Muted labels |
| **Plum Blossom** | `#D7C5D2` | Product cards background, Category highlight containers |
| **Silver Wisteria** | `#DAD4DF` | Page background, High readability and softness |
| **Input Background**| `#F5F2F7` | Form inputs, Search boxes, Table striping |
| **White** | `#FFFFFF` | Card backgrounds, Contrast areas |
| **Dark Text** | `#312A44` | High legibility body text |
| **Secondary Text** | `#88708E` | Subheadings, timestamps, seller credentials |

---

## 🚀 Key Features

### 1. Multi-Role Authentication & Access Control
- **Buyer Account**: Browse catalog, search, filter, manage cart, place orders, write reviews, and track order status.
- **Seller Account**: Dedicated storefront, upload new products with stock and images, update inventory, and manage incoming orders.
- **Admin Account**: High-level platform governance, view platform-wide revenue (GMV), manage/ban users, and moderate catalog listings.

### 2. Multi-Seller Catalog & Product Discovery
- Instant keyword search across all products and merchant catalogs.
- Category filtering: **Electronics**, **Fashion**, **Home & Living**, **Beauty & Wellness**, **Books & Stationery**.
- Dynamic sorting by: Price (Low to High, High to Low), Customer Rating, and Newest.
- Detailed product pages with ratings breakdown, seller badges, stock indicator, and customer reviews.

### 3. Shopping Cart & Checkout
- Add to cart, adjust quantities (+/-), and real-time total calculations.
- Comprehensive checkout form capturing full shipping address, PIN code, and contact information.
- Flexible payment methods: **Cash on Delivery (COD)**, **UPI / Net Banking**, and **Credit/Debit Card**.
- Automatic stock deduction upon successful order completion.

### 4. Real-Time Order Tracking & Management
- Multi-step status pipeline: `PLACED` &rarr; `PROCESSING` &rarr; `SHIPPED` &rarr; `DELIVERED` &rarr; `CANCELLED`.
- Buyers track their packages with color-coded status badges in their dashboard.
- Sellers update order progress directly from their merchant portal.

### 5. NAMITHAMART AI Shopping Assistant
- Floating 24/7 AI chatbot on every page.
- Friendly greeting: *"Hi! I'm NAMITHAMART AI Assistant. How can I help you?"*
- Interactive quick chips for Order Tracking, Seller Onboarding, Return Policies, Payment Methods, and Customer Support.

---

## 🛠️ Technology Stack

NAMITHAMART provides dual-stack backend flexibility:
1. **Java 8+ / JDK 17 Runtime**:
   - Zero-external-dependency embedded `com.sun.net.httpserver.HttpServer`.
   - Thread pool concurrency via `Executors.newFixedThreadPool`.
   - SQLite Database (`sqlite-jdbc` & WAL mode).
   - JSON serialization & parsing (`Google Gson`).
2. **C++20 Drogon Framework (Alternate Stack)**:
   - High-performance asynchronous non-blocking C++20 Drogon HTTP server.
   - Configured via `cpp_backend/CMakeLists.txt` and `config.json`.
3. **Frontend**:
   - Semantic HTML5, Vanilla JavaScript (ES6+ REST API client).
   - Responsive CSS3 variables without bloated external CSS frameworks.

---

## 📂 Project Directory Structure

```text
NAMITHAMART/
├── bin/                             # Compiled Java class files
├── lib/                             # Standalone dependencies
│   ├── ecj.jar                      # Standalone Eclipse Compiler for Java
│   ├── gson.jar                     # Google Gson JSON library
│   └── sqlite-jdbc.jar              # SQLite JDBC Database Driver
├── src/main/java/com/namithamart/   # Java Backend Source
│   ├── Main.java                    # HTTP Server entry point (port 8080)
│   ├── Database.java                # SQLite schema and seed logic
│   ├── models/                      # User, Product, Order, OrderItem, Review, CartItem
│   ├── controllers/                 # REST API endpoints (Auth, Products, Cart, Orders, Reviews, Seller, Admin, Chat)
│   └── utils/                       # JsonUtils, StaticFileHandler
├── public/                          # Frontend Static Assets
│   ├── index.html                   # Homepage
│   ├── products.html                # Catalog & Filter
│   ├── product-detail.html          # Product View & Reviews
│   ├── cart.html                    # Shopping Cart
│   ├── checkout.html                # Checkout
│   ├── login.html                   # Sign In (with one-click demo logins)
│   ├── register.html                # Register (Buyer & Seller)
│   ├── buyer-dashboard.html         # Buyer Orders & Profile
│   ├── seller-dashboard.html        # Merchant Inventory & Orders
│   ├── admin-dashboard.html         # Platform Administration
│   ├── css/
│   │   ├── style.css                # Purple/Lilac Master Theme
│   │   ├── dashboard.css            # Dashboard styling
│   │   └── chatbot.css              # AI Assistant styling
│   └── js/
│       ├── api.js                   # REST API client
│       ├── auth.js                  # Authentication & session state
│       ├── app.js                   # Global helpers & cart count
│       └── chatbot.js               # AI Chatbot logic
├── cpp_backend/                     # C++20 Drogon Backend files
│   ├── CMakeLists.txt
│   ├── config.json
│   ├── main.cpp
│   └── controllers/
├── build.bat                        # One-click Windows build script
├── run.bat                          # One-click Windows launch script
├── namithamart.db                   # SQLite database (auto-seeded)
└── README.md                        # Documentation
```

---

## 🔑 Default Demo Accounts

The database comes pre-seeded with sample accounts for instant testing:

| Role | Email | Password | Store / Display Name |
| :--- | :--- | :--- | :--- |
| **Buyer** | `buyer@namithamart.com` | `buyer123` | Namitha N. |
| **Seller** | `seller@namithamart.com` | `seller123` | Namitha Electronics Hub |
| **Seller** | `artisan@namithamart.com` | `seller123` | Lilac Living & Crafts |
| **Admin** | `admin@namithamart.com` | `admin123` | NAMITHAMART Central Admin |

*Tip: On the [Sign In page](http://localhost:8080/login.html), click the "Buyer", "Seller", or "Admin" pill to auto-fill these credentials.*

---

## 🚀 How to Run the Application

### Option A: One-Click Windows Launch
1. Double-click **`run.bat`** (or open command prompt and run `run.bat`).
2. Open your web browser at: **`http://localhost:8080`**.

### Option B: Command Line (Windows PowerShell / CMD)
```cmd
:: 1. Compile Java code:
build.bat

:: 2. Run the server:
run.bat
```
Or directly:
```cmd
java -cp "bin;lib\gson.jar;lib\sqlite-jdbc.jar" com.namithamart.Main 8080
```

---

## 🐙 How to Push to GitHub

### Method 1: Automated Script (No Git installation needed!)
1. Double-click **`push_to_github.bat`** (or run `powershell -File push_to_github.ps1`).
2. Paste your [GitHub Personal Access Token](https://github.com/settings/tokens/new) (classic with `repo` scope).
3. The script will automatically create the `NAMITHAMART` repository on your GitHub account and push all code and assets!

### Method 2: Standard Git Commands (If Git or GitHub Desktop is installed)
```cmd
git init
git add .
git commit -m "NAMITHAMART Full-Stack Platform with Purple/Lilac Theme"
git branch -M main
git remote add origin https://github.com/<your-username>/NAMITHAMART.git
git push -u origin main
```

---

## ☁️ How to Host on Render (Free & 100% Cloud-Hosted)

The project includes an optimized [`Dockerfile`](file:///c:/Users/acer/Downloads/NAMITHAMART/Dockerfile) and [`render.yaml`](file:///c:/Users/acer/Downloads/NAMITHAMART/render.yaml) preconfigured for Render.

### Step-by-Step Render Deployment:
1. Sign up / Log in to [Render](https://dashboard.render.com).
2. Click **New +** in the top navigation and select **Web Service**.
3. Choose **Build and deploy from a Git repository** and connect your **`NAMITHAMART`** GitHub repository.
4. Render will automatically detect the **Dockerfile**:
   - **Name**: `namithamart`
   - **Environment**: `Docker`
   - **Region**: Nearest to you (e.g. `Oregon` or `Frankfurt` or `Singapore`)
   - **Branch**: `main`
   - **Plan**: `Free`
5. Under **Advanced Settings**, ensure:
   - **Environment Variable**: `PORT` = `10000` (pre-configured)
6. Click **Deploy Web Service**!
7. Render will build the container and provide your live URL (e.g. `https://namithamart.onrender.com`).

---

## 🔒 Verification & Compliance
- **Zero Old Branding**: 100% free of legacy names. All logos, headings, footers, page titles, and code references use **NAMITHAMART**.
- **Accurate Colors**: Every page strictly respects the Midnight Orchid (`#312A44`) and Dusky Lilac (`#88708E`) theme.
- **REST API Driven**: Clean separation between backend services and UI layer.
- **Cloud-Ready**: Native Docker support ready for Render, Railway, Fly.io, or AWS.

