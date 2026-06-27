package com.example.hyynnstore.controller;

import android.content.Context;

import com.example.hyynnstore.database.DatabaseHelper;
import com.example.hyynnstore.model.Category;
import com.example.hyynnstore.model.Product;

import java.util.List;

public class ProductController {
    private final DatabaseHelper db;

    public ProductController(Context c) {
        db = new DatabaseHelper(c);
    }

    public List<Product> list(String keyword, int categoryId) {
        return db.getProducts(keyword, categoryId);
    }

    public Product get(int id) {
        return db.getProduct(id);
    }

    public List<Category> categories() {
        return db.getCategories();
    }

    public long save(Product product) {
        return db.saveProduct(product);
    }

    public boolean delete(int id) {
        return db.deleteProduct(id);
    }

    public long addCategory(String name) {
        return db.addCategory(name);
    }

    public boolean updateCategory(int id, String name) {
        return db.updateCategory(id, name);
    }

    public boolean deleteCategory(int id) {
        return db.deleteCategory(id);
    }
}
