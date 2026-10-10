package com.namithamart.models;

public class CartItem {
    private int id;
    private int userId;
    private int productId;
    private String productTitle;
    private double price;
    private String imageUrl;
    private int sellerId;
    private String sellerName;
    private int quantity;
    private int stock;

    public CartItem() {}

    public CartItem(int id, int userId, int productId, String productTitle, double price, 
                    String imageUrl, int sellerId, String sellerName, int quantity, int stock) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productTitle = productTitle;
        this.price = price;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.quantity = quantity;
        this.stock = stock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
