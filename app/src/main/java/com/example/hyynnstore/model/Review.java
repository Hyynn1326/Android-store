package com.example.hyynnstore.model;

public class Review {
    public int id, userId, productId, rating;
    public String comment, createdAt, userName, productName, adminReply;

    public Review(int id, int userId, int productId, int rating, String comment, String createdAt, String userName) {
        this(id, userId, productId, rating, comment, createdAt, userName, "Product #" + productId, "");
    }

    public Review(int id, int userId, int productId, int rating, String comment, String createdAt, String userName, String productName, String adminReply) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userName = userName;
        this.productName = productName;
        this.adminReply = adminReply;
    }
}
