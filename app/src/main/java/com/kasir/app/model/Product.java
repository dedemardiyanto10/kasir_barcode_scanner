package com.kasir.app.model;

public class Product {
    private String id;
    private String name;
    private String price;
    private String barcode;

    public Product(String id, String name, String price, String barcode) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.barcode = barcode;
    }

    public Product() {}

    // Getter methods

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getBarcode() {
        return barcode;
    }

    // Setter methods

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
