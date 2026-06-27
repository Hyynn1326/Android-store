package com.example.hyynnstore.controller;

import android.content.Context;
import com.example.hyynnstore.database.DatabaseHelper;

public class ProductReportController {
    private final DatabaseHelper db;
    public ProductReportController(Context context) { db = new DatabaseHelper(context); }
    public long add(int userId, int productId, String reason) { return db.addProductReport(userId, productId, reason); }
}
