package com.example.dell.yoursapp.Model;


public class Order {
    private String ProductId;
    private String ProductName;
    private String Quantity;
    private String Price;
    private String Discount;

    public Order() {
    }

    public Order(String productId, String productName, String quanlity, String price, String discount) {
        ProductId = productId;
        ProductName = productName;
        Quantity = quanlity;
        Price = price;
        Discount = discount;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getQuanlity() {
        return Quantity;
    }

    public void setQuanlity(String quanlity) {
        Quantity = quanlity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }
}
