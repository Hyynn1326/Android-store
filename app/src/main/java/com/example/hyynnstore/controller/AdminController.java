package com.example.hyynnstore.controller;

import android.content.Context;
import com.example.hyynnstore.database.DatabaseHelper;
import com.example.hyynnstore.model.User;
import java.util.List;

public class AdminController {
    private final DatabaseHelper db;
    public AdminController(Context c){ db = new DatabaseHelper(c); }
    public int count(String t){ return db.count(t); }
    public double revenue(){ return db.revenue(); }
    public int pendingOrders(){ return db.pendingOrdersCount(); }
    public int pendingReviews(){ return db.pendingReviewsCount(); }
    public int pendingReports(){ return db.pendingProductReportsCount(); }
    public List<User> users(){ return db.getAllUsers(); }
    public boolean toggle(int id,String status){ return db.toggleUserStatus(id,status); }
    public boolean lock(int id, String reason){ return db.lockUserWithReason(id, reason); }
    public boolean unlock(int id){ return db.unlockUser(id); }
}
