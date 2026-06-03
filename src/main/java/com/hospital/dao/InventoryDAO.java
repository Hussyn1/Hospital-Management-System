package com.hospital.dao;

import com.hospital.models.InventoryItem;
import com.hospital.models.PurchaseOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public boolean save(InventoryItem item) throws SQLException {
        String query = "INSERT INTO inventory (item_name, item_type, stock_qty, unit, low_stock_threshold, last_restocked, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getItemType());
            pstmt.setInt(3, item.getStockQty());
            pstmt.setString(4, item.getUnit());
            pstmt.setInt(5, item.getLowStockThreshold());
            
            if (item.getLastRestocked() != null) {
                pstmt.setString(6, item.getLastRestocked().toString());
            } else {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }

            if (item.getExpiryDate() != null) {
                pstmt.setString(7, item.getExpiryDate().toString());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                item.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean update(InventoryItem item) throws SQLException {
        String query = "UPDATE inventory SET item_name = ?, item_type = ?, stock_qty = ?, unit = ?, low_stock_threshold = ?, last_restocked = ?, expiry_date = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getItemType());
            pstmt.setInt(3, item.getStockQty());
            pstmt.setString(4, item.getUnit());
            pstmt.setInt(5, item.getLowStockThreshold());
            
            if (item.getLastRestocked() != null) {
                pstmt.setString(6, item.getLastRestocked().toString());
            } else {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }

            if (item.getExpiryDate() != null) {
                pstmt.setString(7, item.getExpiryDate().toString());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }
            
            pstmt.setInt(8, item.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStock(int id, int qtyAdjustment) throws SQLException {
        String query = "UPDATE inventory SET stock_qty = stock_qty + ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, qtyAdjustment);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public InventoryItem getById(int id) throws SQLException {
        String query = "SELECT * FROM inventory WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<InventoryItem> getAll() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String query = "SELECT * FROM inventory ORDER BY item_name ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<InventoryItem> getLowStockItems() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String query = "SELECT * FROM inventory WHERE stock_qty <= low_stock_threshold ORDER BY item_name ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Transactional: saves purchase order, updates item's last_restocked date, and increments item's stock_qty!
     */
    public boolean savePurchaseOrder(PurchaseOrder po) throws SQLException {
        String poQuery = "INSERT INTO purchase_orders (item_id, quantity, order_date, cost, supplier) VALUES (?, ?, ?, ?, ?);";
        String itemUpdate = "UPDATE inventory SET stock_qty = stock_qty + ?, last_restocked = ? WHERE id = ?;";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            try (PreparedStatement poStmt = conn.prepareStatement(poQuery)) {
                poStmt.setInt(1, po.getItemId());
                poStmt.setInt(2, po.getQuantity());
                poStmt.setString(3, po.getOrderDate().toString());
                poStmt.setDouble(4, po.getCost());
                poStmt.setString(5, po.getSupplier());
                
                poStmt.executeUpdate();
                po.setId(DBConnection.getLastInsertId(conn));
            }

            try (PreparedStatement itemStmt = conn.prepareStatement(itemUpdate)) {
                itemStmt.setInt(1, po.getQuantity());
                itemStmt.setString(2, po.getOrderDate().toString());
                itemStmt.setInt(3, po.getItemId());
                itemStmt.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public List<PurchaseOrder> getAllPurchaseOrders() throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        String query = "SELECT po.*, i.item_name FROM purchase_orders po " +
                       "JOIN inventory i ON po.item_id = i.id ORDER BY po.order_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                PurchaseOrder po = new PurchaseOrder();
                po.setId(rs.getInt("id"));
                po.setItemId(rs.getInt("item_id"));
                po.setQuantity(rs.getInt("quantity"));
                po.setOrderDate(LocalDate.parse(rs.getString("order_date")));
                po.setCost(rs.getDouble("cost"));
                po.setSupplier(rs.getString("supplier"));
                
                // Helper
                po.setItemName(rs.getString("item_name"));
                list.add(po);
            }
        }
        return list;
    }

    private InventoryItem mapRow(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getInt("id"));
        item.setItemName(rs.getString("item_name"));
        item.setItemType(rs.getString("item_type"));
        item.setStockQty(rs.getInt("stock_qty"));
        item.setUnit(rs.getString("unit"));
        item.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        
        String restockStr = rs.getString("last_restocked");
        if (restockStr != null) {
            item.setLastRestocked(LocalDate.parse(restockStr));
        }
        
        String expiryStr = rs.getString("expiry_date");
        if (expiryStr != null) {
            item.setExpiryDate(LocalDate.parse(expiryStr));
        }
        return item;
    }
}
