package com.example.hyynnstore.controller;

import android.content.Context;
import com.example.hyynnstore.database.DatabaseHelper;
import com.example.hyynnstore.model.NotificationItem;
import java.util.List;

public class NotificationController {
    private final DatabaseHelper db;
    public NotificationController(Context context) { db = new DatabaseHelper(context); }
    public List<NotificationItem> list() { return db.getNotifications(); }
    public long add(String title, String content, String type) { return db.addNotification(title, content, type); }
    public int unreadAdmin() { return db.unreadAdminNotifications(); }
    public void markAdminRead() { db.markAdminNotificationsRead(); }
    public boolean delete(int id) { return db.deleteNotification(id); }
}
