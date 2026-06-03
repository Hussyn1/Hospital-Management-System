package com.hospital.models;

import java.time.LocalDate;

public class Medicine {
    private int id;
    private String name;
    private String genericName;
    private int stockQty;
    private int lowStockThreshold;
    private double price;
    private LocalDate expiryDate;

    public Medicine() {}

    public Medicine(int id, String name, String genericName, int stockQty, int lowStockThreshold, double price, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.stockQty = stockQty;
        this.lowStockThreshold = lowStockThreshold;
        this.price = price;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    /**
     * Checks if medicine is low in stock.
     */
    public boolean isLowStock() {
        return stockQty <= lowStockThreshold;
    }
}
