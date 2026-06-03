package com.hospital.dao;

import com.hospital.models.Medicine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    public boolean save(Medicine m) throws SQLException {
        String query = "INSERT INTO medicines (name, generic_name, stock_qty, low_stock_threshold, price, expiry_date) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getGenericName());
            pstmt.setInt(3, m.getStockQty());
            pstmt.setInt(4, m.getLowStockThreshold());
            pstmt.setDouble(5, m.getPrice());
            pstmt.setString(6, m.getExpiryDate().toString());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                m.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean update(Medicine m) throws SQLException {
        String query = "UPDATE medicines SET name = ?, generic_name = ?, stock_qty = ?, low_stock_threshold = ?, price = ?, expiry_date = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getGenericName());
            pstmt.setInt(3, m.getStockQty());
            pstmt.setInt(4, m.getLowStockThreshold());
            pstmt.setDouble(5, m.getPrice());
            pstmt.setString(6, m.getExpiryDate().toString());
            pstmt.setInt(7, m.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String query = "DELETE FROM medicines WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStock(int id, int adjustmentQty) throws SQLException {
        String query = "UPDATE medicines SET stock_qty = stock_qty + ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, adjustmentQty);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Medicine getById(int id) throws SQLException {
        String query = "SELECT * FROM medicines WHERE id = ?;";
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

    public List<Medicine> getAll() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String query = "SELECT * FROM medicines ORDER BY name ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Medicine> getLowStockMedicines() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String query = "SELECT * FROM medicines WHERE stock_qty <= low_stock_threshold ORDER BY name ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Medicine> search(String keyword) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String query = "SELECT * FROM medicines WHERE name LIKE ? OR generic_name LIKE ? ORDER BY name ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            String wildcard = "%" + keyword + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setGenericName(rs.getString("generic_name"));
        m.setStockQty(rs.getInt("stock_qty"));
        m.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        m.setPrice(rs.getDouble("price"));
        m.setExpiryDate(LocalDate.parse(rs.getString("expiry_date")));
        return m;
    }
}
