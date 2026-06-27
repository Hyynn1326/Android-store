package com.example.hyynnstore.controller;

import android.content.Context;
import com.example.hyynnstore.database.DatabaseHelper;
import com.example.hyynnstore.model.*;
import java.util.List;

public class ReviewController {
    private final DatabaseHelper db;
    public ReviewController(Context c){ db = new DatabaseHelper(c); }
    public long add(int u,int p,int r,String cmt){ return db.addReview(u,p,r,cmt); }
    public void img(long id,String uri){ db.addReviewImage(id,uri); }
    public List<Review> list(int p){ return db.getReviews(p); }
    public List<Review> all(){ return db.getAllReviews(); }
    public List<ReviewImage> images(int r){ return db.getReviewImages(r); }
    public boolean del(int id){ return db.deleteReview(id); }
    public float avg(int p){ return db.avgRating(p); }
    public boolean reply(int id, String reply){ return db.replyReview(id, reply); }
    public boolean done(int id){ return db.markReviewAdminDone(id); }
    public boolean isDone(int id){ return db.isReviewAdminDone(id); }
}
