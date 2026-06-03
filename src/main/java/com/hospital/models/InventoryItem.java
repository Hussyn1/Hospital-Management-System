package com.hospital.models;

import java.time.LocalDate;

public class InventoryItem {
    private int id;
    private String itemName;
    private String itemType; // EQUIPMENT, CONSUMABLE
    private int stockQty;
    private String unit; // Boxes, Pieces, etc.
    private int lowStockThreshold;
    private LocalDate lastRestocked;
    private LocalDate expiryDate; // Nullable for equipment

    public InventoryItem() {}

    public InventoryItem(int id, String itemName, String itemType, int stockQty, String unit, int lowStockThreshold, LocalDate lastRestocked, LocalDate expiryDate) {
        this.id = id;
        this.itemName = itemName;
        this.itemType = itemType;
        this.stockQty = stockQty;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
        this.lastRestocked = lastRestocked;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public LocalDate getLastRestocked() { return lastRestocked; }
    public void setLastRestocked(LocalDate lastRestocked) { this.lastRestocked = lastRestocked; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isLowStock() {
        return stockQty <= lowStockThreshold;
    }
}
