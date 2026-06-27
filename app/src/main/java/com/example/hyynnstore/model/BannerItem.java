package com.example.hyynnstore.model;

public class BannerItem {
    public int id;
    public String title;
    public String imageUri;
    public int active;

    public BannerItem(int id, String title, String imageUri, int active) {
        this.id = id;
        this.title = title;
        this.imageUri = imageUri;
        this.active = active;
    }
}
