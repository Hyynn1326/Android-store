package com.example.hyynnstore.model;

public class NotificationItem {
    public int id;
    public String title;
    public String content;
    public String type;
    public String createdAt;

    public NotificationItem(int id, String title, String content, String type, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
    }
}
