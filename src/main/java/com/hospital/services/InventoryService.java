package com.hospital.services;

import com.hospital.dao.InventoryDAO;
import com.hospital.models.InventoryItem;
import com.hospital.models.PurchaseOrder;
import com.hospital.utils.Validator;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventoryService {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean addItem(InventoryItem item) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(item.getItemName())) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (!Validator.isNotEmpty(item.getItemType())) {
            throw new IllegalArgumentException("Item type (EQUIPMENT or CONSUMABLE) is required.");
        }
        if (item.getStockQty() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (!Validator.isNotEmpty(item.getUnit())) {
            throw new IllegalArgumentException("Measurement unit is required.");
        }

        return inventoryDAO.save(item);
    }

    public boolean updateItem(InventoryItem item) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(item.getItemName())) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (item.getStockQty() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return inventoryDAO.update(item);
    }

    public boolean adjustStock(int itemId, int adjustment) throws SQLException {
        return inventoryDAO.updateStock(itemId, adjustment);
    }

    public InventoryItem getItem(int id) throws SQLException {
        return inventoryDAO.getById(id);
    }

    public List<InventoryItem> getAllItems() throws SQLException {
        return inventoryDAO.getAll();
    }

    public List<InventoryItem> getLowStockAlerts() throws SQLException {
        return inventoryDAO.getLowStockItems();
    }

    /**
     * Creates a purchase order and automatically restocks the item.
     */
    public boolean createPurchaseOrder(int itemId, int qty, double cost, String supplier) throws SQLException, IllegalArgumentException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive.");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost cannot be negative.");
        }
        if (!Validator.isNotEmpty(supplier)) {
            throw new IllegalArgumentException("Supplier name is required.");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setItemId(itemId);
        po.setQuantity(qty);
        po.setOrderDate(LocalDate.now());
        po.setCost(cost);
        po.setSupplier(supplier);

        return inventoryDAO.savePurchaseOrder(po);
    }

    public List<PurchaseOrder> getAllPurchaseOrders() throws SQLException {
        return inventoryDAO.getAllPurchaseOrders();
    }
}
