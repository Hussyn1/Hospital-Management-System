package com.hospital.dao;

import com.hospital.enums.WardType;
import com.hospital.models.Ward;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WardDAO {

    public boolean save(Ward w) throws SQLException {
        String query = "INSERT INTO wards (name, type) VALUES (?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, w.getName());
            pstmt.setString(2, w.getType().name());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                w.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public Ward getById(int id) throws SQLException {
        String query = "SELECT * FROM wards WHERE id = ?;";
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

    public List<Ward> getAll() throws SQLException {
        List<Ward> list = new ArrayList<>();
        String query = "SELECT * FROM wards ORDER BY name ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Ward mapRow(ResultSet rs) throws SQLException {
        Ward w = new Ward();
        w.setId(rs.getInt("id"));
        w.setName(rs.getString("name"));
        w.setType(WardType.valueOf(rs.getString("type")));
        return w;
    }
}
