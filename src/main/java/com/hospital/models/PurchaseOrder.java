package com.hospital.models;

import java.time.LocalDate;

public class PurchaseOrder {
    private int id;
    private int itemId;
    private int quantity;
    private LocalDate orderDate;
    private double cost;
    private String supplier;

    // Helper fields
    private String itemName;

    public PurchaseOrder() {}

    public PurchaseOrder(int id, int itemId, int quantity, LocalDate orderDate, double cost, String supplier) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.cost = cost;
        this.supplier = supplier;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
