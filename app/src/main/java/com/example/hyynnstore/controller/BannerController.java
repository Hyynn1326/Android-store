package com.example.hyynnstore.controller;

import android.content.Context;

import com.example.hyynnstore.database.DatabaseHelper;
import com.example.hyynnstore.model.BannerItem;

import java.util.List;

public class BannerController {
    private final DatabaseHelper db;

    public BannerController(Context context) {
        db = new DatabaseHelper(context);
    }

    public List<BannerItem> all() { return db.getBanners(false); }
    public List<BannerItem> active() { return db.getBanners(true); }
    public long save(BannerItem banner) { return db.saveBanner(banner); }
    public boolean delete(int id) { return db.deleteBanner(id); }
}
