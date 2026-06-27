package com.example.hyynnstore.model;
public class OrderDetail { public int id,orderId,productId,quantity; public double price; public String productName; public OrderDetail(int id,int orderId,int productId,int quantity,double price,String productName){this.id=id;this.orderId=orderId;this.productId=productId;this.quantity=quantity;this.price=price;this.productName=productName;} }
