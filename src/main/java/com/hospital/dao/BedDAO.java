package com.hospital.dao;

import com.hospital.enums.BedStatus;
import com.hospital.models.Bed;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {

    public boolean save(Bed b) throws SQLException {
        String query = "INSERT INTO beds (ward_id, bed_number, status) VALUES (?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, b.getWardId());
            pstmt.setString(2, b.getBedNumber());
            pstmt.setString(3, b.getStatus().name());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                b.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean updateStatus(int id, BedStatus status) throws SQLException {
        String query = "UPDATE beds SET status = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatusByNumber(int wardId, String bedNumber, BedStatus status) throws SQLException {
        String query = "UPDATE beds SET status = ? WHERE ward_id = ? AND bed_number = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, wardId);
            pstmt.setString(3, bedNumber);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Bed getById(int id) throws SQLException {
        String query = "SELECT b.*, w.name AS ward_name, w.type AS ward_type FROM beds b " +
                       "JOIN wards w ON b.ward_id = w.id WHERE b.id = ?;";
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

    public List<Bed> getByWardId(int wardId) throws SQLException {
        List<Bed> list = new ArrayList<>();
        String query = "SELECT b.*, w.name AS ward_name, w.type AS ward_type FROM beds b " +
                       "JOIN wards w ON b.ward_id = w.id WHERE b.ward_id = ? ORDER BY b.bed_number ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, wardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Bed> getAll() throws SQLException {
        List<Bed> list = new ArrayList<>();
        String query = "SELECT b.*, w.name AS ward_name, w.type AS ward_type FROM beds b " +
                       "JOIN wards w ON b.ward_id = w.id ORDER BY w.name ASC, b.bed_number ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int getAvailableBedsCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM beds WHERE status = 'AVAILABLE';";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOccupiedBedsCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM beds WHERE status = 'OCCUPIED';";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Bed mapRow(ResultSet rs) throws SQLException {
        Bed b = new Bed();
        b.setId(rs.getInt("id"));
        b.setWardId(rs.getInt("ward_id"));
        b.setBedNumber(rs.getString("bed_number"));
        b.setStatus(BedStatus.valueOf(rs.getString("status")));
        
        // Joined details
        b.setWardName(rs.getString("ward_name"));
        b.setWardType(rs.getString("ward_type"));
        return b;
    }
}
