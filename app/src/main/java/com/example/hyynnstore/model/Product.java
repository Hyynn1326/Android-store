package com.example.hyynnstore.model;

public class Product {
    public int id, categoryId, stock, isHot, isNew;
    public String name, image, description, brand, spec;
    public double price;
    public double salePrice;

    public Product(int id, String name, double price, String image, String description, int categoryId, String brand, String spec, int stock, int isHot, int isNew) {
        this(id, name, price, 0, image, description, categoryId, brand, spec, stock, isHot, isNew);
    }

    public Product(int id, String name, double price, double salePrice, String image, String description, int categoryId, String brand, String spec, int stock, int isHot, int isNew) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.salePrice = salePrice;
        this.image = image;
        this.description = description;
        this.categoryId = categoryId;
        this.brand = brand;
        this.spec = spec;
        this.stock = stock;
        this.isHot = isHot;
        this.isNew = isNew;
    }

    public boolean hasSale() {
        return salePrice > 0 && salePrice < price;
    }

    public double finalPrice() {
        return hasSale() ? salePrice : price;
    }

    public int discountPercent() {
        if (!hasSale() || price <= 0) return 0;
        return (int) Math.round((price - salePrice) * 100.0 / price);
    }
}
