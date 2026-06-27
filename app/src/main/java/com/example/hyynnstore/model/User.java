package com.example.hyynnstore.model;

public class User {
    public int id;
    public String name, email, phone, password, avatar, address, role, status, lockReason;

    public User(int id, String name, String email, String phone, String password, String avatar, String address, String role, String status) {
        this(id, name, email, phone, password, avatar, address, role, status, "");
    }

    public User(int id, String name, String email, String phone, String password, String avatar, String address, String role, String status, String lockReason) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.avatar = avatar;
        this.address = address;
        this.role = role;
        this.status = status;
        this.lockReason = lockReason;
    }
}
