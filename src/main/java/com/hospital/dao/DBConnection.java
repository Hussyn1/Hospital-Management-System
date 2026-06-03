package com.hospital.dao;

import com.hospital.utils.HashHelper;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "hms.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    static {
        // Ensure data directory exists
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Initialize the schema and populate mock data
        initializeDatabase();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // 1. Create Schema Tables
                createTables(stmt);
                conn.commit();

                // 2. Check if Database is empty and insert mock data
                if (isDatabaseEmpty(conn)) {
                    insertMockData(conn);
                    conn.commit();
                    System.out.println("Hospital Management System Database initialized and populated with mock data successfully!");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Transaction failed, database changes rolled back.");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect or initialize the SQLite database.");
            e.printStackTrace();
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        String[] ddlQueries = {
            // 1. Users Table
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT UNIQUE NOT NULL," +
            "  password_hash TEXT NOT NULL," +
            "  role TEXT CHECK(role IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PHARMACIST', 'LAB_TECH')) NOT NULL," +
            "  full_name TEXT NOT NULL," +
            "  contact TEXT NOT NULL," +
            "  email TEXT UNIQUE NOT NULL," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ");",

            // 2. Patients Table
            "CREATE TABLE IF NOT EXISTS patients (" +
            "  id TEXT PRIMARY KEY," +
            "  name TEXT NOT NULL," +
            "  dob DATE NOT NULL," +
            "  blood_group TEXT CHECK(blood_group IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')) NOT NULL," +
            "  contact TEXT NOT NULL," +
            "  emergency_contact TEXT NOT NULL," +
            "  medical_history TEXT," +
            "  status TEXT CHECK(status IN ('ACTIVE', 'ADMITTED', 'DISCHARGED')) DEFAULT 'ACTIVE'," +
            "  is_deleted INTEGER DEFAULT 0," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ");",

            // 3. Doctors Table
            "CREATE TABLE IF NOT EXISTS doctors (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id INTEGER UNIQUE NOT NULL," +
            "  specialization TEXT NOT NULL," +
            "  license_number TEXT UNIQUE NOT NULL," +
            "  consultation_fee REAL NOT NULL," +
            "  availability_schedule TEXT NOT NULL," + // JSON-encoded string
            "  department TEXT NOT NULL," +
            "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ");",

            // 4. Appointments Table
            "CREATE TABLE IF NOT EXISTS appointments (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  patient_id TEXT NOT NULL," +
            "  doctor_id INTEGER NOT NULL," +
            "  appointment_date DATE NOT NULL," +
            "  appointment_time TIME NOT NULL," +
            "  status TEXT CHECK(status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')) DEFAULT 'PENDING'," +
            "  notes TEXT," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)," +
            "  FOREIGN KEY (doctor_id) REFERENCES doctors(id)" +
            ");",

            // 5. Medical Records Table
            "CREATE TABLE IF NOT EXISTS medical_records (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  appointment_id INTEGER UNIQUE," +
            "  patient_id TEXT NOT NULL," +
            "  doctor_id INTEGER NOT NULL," +
            "  diagnosis TEXT NOT NULL," +
            "  visit_date DATE NOT NULL," +
            "  treatment_plan TEXT," +
            "  FOREIGN KEY (appointment_id) REFERENCES appointments(id)," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)," +
            "  FOREIGN KEY (doctor_id) REFERENCES doctors(id)" +
            ");",

            // 6. Medicines Table
            "CREATE TABLE IF NOT EXISTS medicines (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT UNIQUE NOT NULL," +
            "  generic_name TEXT NOT NULL," +
            "  stock_qty INTEGER NOT NULL," +
            "  low_stock_threshold INTEGER DEFAULT 10," +
            "  price REAL NOT NULL," +
            "  expiry_date DATE NOT NULL" +
            ");",

            // 7. Prescriptions Table
            "CREATE TABLE IF NOT EXISTS prescriptions (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  record_id INTEGER NOT NULL," +
            "  doctor_id INTEGER NOT NULL," +
            "  patient_id TEXT NOT NULL," +
            "  status TEXT CHECK(status IN ('PENDING', 'FILLED')) DEFAULT 'PENDING'," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  FOREIGN KEY (record_id) REFERENCES medical_records(id)," +
            "  FOREIGN KEY (doctor_id) REFERENCES doctors(id)," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)" +
            ");",

            // 8. Prescription Items Table
            "CREATE TABLE IF NOT EXISTS prescription_items (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  prescription_id INTEGER NOT NULL," +
            "  medicine_id INTEGER NOT NULL," +
            "  dosage TEXT NOT NULL," +
            "  frequency TEXT NOT NULL," +
            "  quantity INTEGER NOT NULL," +
            "  FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE," +
            "  FOREIGN KEY (medicine_id) REFERENCES medicines(id)" +
            ");",

            // 9. Lab Requests Table
            "CREATE TABLE IF NOT EXISTS lab_requests (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  record_id INTEGER," +
            "  patient_id TEXT NOT NULL," +
            "  test_name TEXT NOT NULL," +
            "  requested_date DATE NOT NULL," +
            "  result_text TEXT," +
            "  flag_abnormal INTEGER DEFAULT 0," +
            "  status TEXT CHECK(status IN ('PENDING', 'COMPLETED')) DEFAULT 'PENDING'," +
            "  report_file_path TEXT," +
            "  FOREIGN KEY (record_id) REFERENCES medical_records(id)," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)" +
            ");",

            // 10. Wards Table
            "CREATE TABLE IF NOT EXISTS wards (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT NOT NULL," +
            "  type TEXT CHECK(type IN ('GENERAL', 'ICU', 'PEDIATRIC', 'SURGICAL', 'MATERNITY')) NOT NULL" +
            ");",

            // 11. Beds Table
            "CREATE TABLE IF NOT EXISTS beds (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  ward_id INTEGER NOT NULL," +
            "  bed_number TEXT NOT NULL," +
            "  status TEXT CHECK(status IN ('AVAILABLE', 'OCCUPIED', 'UNDER_MAINTENANCE')) DEFAULT 'AVAILABLE'," +
            "  UNIQUE(ward_id, bed_number)," +
            "  FOREIGN KEY (ward_id) REFERENCES wards(id) ON DELETE CASCADE" +
            ");",

            // 12. Admissions Table
            "CREATE TABLE IF NOT EXISTS admissions (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  patient_id TEXT NOT NULL," +
            "  bed_id INTEGER NOT NULL," +
            "  admission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  discharge_date TIMESTAMP," +
            "  daily_rate REAL NOT NULL DEFAULT 150.0," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)," +
            "  FOREIGN KEY (bed_id) REFERENCES beds(id)" +
            ");",

            // 13. Billing Table
            "CREATE TABLE IF NOT EXISTS billing (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  patient_id TEXT NOT NULL," +
            "  appointment_id INTEGER," +
            "  admission_id INTEGER," +
            "  total_amount REAL NOT NULL DEFAULT 0.0," +
            "  paid_amount REAL NOT NULL DEFAULT 0.0," +
            "  payment_status TEXT CHECK(payment_status IN ('UNPAID', 'PARTIAL', 'PAID')) DEFAULT 'UNPAID'," +
            "  billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  payment_method TEXT CHECK(payment_method IN ('CASH', 'CARD', 'INSURANCE', 'UPI'))," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)," +
            "  FOREIGN KEY (appointment_id) REFERENCES appointments(id)," +
            "  FOREIGN KEY (admission_id) REFERENCES admissions(id)" +
            ");",

            // 14. Emergency Queue Table
            "CREATE TABLE IF NOT EXISTS emergency_queue (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  patient_id TEXT NOT NULL," +
            "  triage_level TEXT CHECK(triage_level IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')) NOT NULL," +
            "  queue_priority INTEGER NOT NULL," +
            "  bed_id INTEGER," +
            "  registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  status TEXT CHECK(status IN ('WAITING', 'ADMITTED', 'DISCHARGED')) DEFAULT 'WAITING'," +
            "  FOREIGN KEY (patient_id) REFERENCES patients(id)," +
            "  FOREIGN KEY (bed_id) REFERENCES beds(id)" +
            ");",

            // 15. Inventory Table
            "CREATE TABLE IF NOT EXISTS inventory (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  item_name TEXT NOT NULL," +
            "  item_type TEXT CHECK(item_type IN ('EQUIPMENT', 'CONSUMABLE')) NOT NULL," +
            "  stock_qty INTEGER NOT NULL," +
            "  unit TEXT NOT NULL," +
            "  low_stock_threshold INTEGER DEFAULT 5," +
            "  last_restocked DATE," +
            "  expiry_date DATE" +
            ");",

            // 16. Purchase Orders Table
            "CREATE TABLE IF NOT EXISTS purchase_orders (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  item_id INTEGER NOT NULL," +
            "  quantity INTEGER NOT NULL," +
            "  order_date DATE NOT NULL," +
            "  cost REAL NOT NULL," +
            "  supplier TEXT NOT NULL," +
            "  FOREIGN KEY (item_id) REFERENCES inventory(id)" +
            ");",

            // 17. Staff Shifts Table
            "CREATE TABLE IF NOT EXISTS staff_shifts (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id INTEGER NOT NULL," +
            "  shift_start TIMESTAMP NOT NULL," +
            "  shift_end TIMESTAMP NOT NULL," +
            "  role_assigned TEXT NOT NULL," +
            "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ");",

            // 18. Notifications Table
            "CREATE TABLE IF NOT EXISTS notifications (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id INTEGER NOT NULL," +
            "  message TEXT NOT NULL," +
            "  is_read INTEGER DEFAULT 0," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ");"
        };

        for (String query : ddlQueries) {
            stmt.execute(query);
        }
    }

    private static boolean isDatabaseEmpty(Connection conn) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    public static int getLastInsertId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve last insert rowid.");
    }

    private static void insertMockData(Connection conn) throws SQLException {
        // 1. Insert Base Staff (Users)
        String userInsert = "INSERT INTO users (username, password_hash, role, full_name, contact, email) VALUES (?, ?, ?, ?, ?, ?);";
        String adminPass = HashHelper.hash("admin123");
        String doctorPass = HashHelper.hash("doctor123");
        String nursePass = HashHelper.hash("nurse123");
        String recepPass = HashHelper.hash("receptionist123");
        String pharmPass = HashHelper.hash("pharmacist123");
        String labPass = HashHelper.hash("lab123");

        int adminId, drSmithId, drJonesId, drMillerId, nurseId, recepId, pharmId, labTechId;

        try (PreparedStatement pstmt = conn.prepareStatement(userInsert)) {
            // Admin
            pstmt.setString(1, "admin");
            pstmt.setString(2, adminPass);
            pstmt.setString(3, "ADMIN");
            pstmt.setString(4, "System Administrator");
            pstmt.setString(5, "555-0100");
            pstmt.setString(6, "admin@hospital.com");
            pstmt.executeUpdate();
            adminId = getLastInsertId(conn);

            // Dr. Smith
            pstmt.setString(1, "dr_smith");
            pstmt.setString(2, doctorPass);
            pstmt.setString(3, "DOCTOR");
            pstmt.setString(4, "Dr. Sarah Smith");
            pstmt.setString(5, "555-0111");
            pstmt.setString(6, "sarah.smith@hospital.com");
            pstmt.executeUpdate();
            drSmithId = getLastInsertId(conn);

            // Dr. Jones
            pstmt.setString(1, "dr_jones");
            pstmt.setString(2, doctorPass);
            pstmt.setString(3, "DOCTOR");
            pstmt.setString(4, "Dr. Robert Jones");
            pstmt.setString(5, "555-0112");
            pstmt.setString(6, "robert.jones@hospital.com");
            pstmt.executeUpdate();
            drJonesId = getLastInsertId(conn);

            // Dr. Miller
            pstmt.setString(1, "dr_miller");
            pstmt.setString(2, doctorPass);
            pstmt.setString(3, "DOCTOR");
            pstmt.setString(4, "Dr. Emily Miller");
            pstmt.setString(5, "555-0113");
            pstmt.setString(6, "emily.miller@hospital.com");
            pstmt.executeUpdate();
            drMillerId = getLastInsertId(conn);

            // Nurse Kelly
            pstmt.setString(1, "nurse_kelly");
            pstmt.setString(2, nursePass);
            pstmt.setString(3, "NURSE");
            pstmt.setString(4, "Nurse Kelly Brown");
            pstmt.setString(5, "555-0120");
            pstmt.setString(6, "kelly.brown@hospital.com");
            pstmt.executeUpdate();
            nurseId = getLastInsertId(conn);

            // Receptionist Amy
            pstmt.setString(1, "recep_amy");
            pstmt.setString(2, recepPass);
            pstmt.setString(3, "RECEPTIONIST");
            pstmt.setString(4, "Amy Green");
            pstmt.setString(5, "555-0130");
            pstmt.setString(6, "amy.green@hospital.com");
            pstmt.executeUpdate();
            recepId = getLastInsertId(conn);

            // Pharmacist Phil
            pstmt.setString(1, "pharm_phil");
            pstmt.setString(2, pharmPass);
            pstmt.setString(3, "PHARMACIST");
            pstmt.setString(4, "Phil Cooper");
            pstmt.setString(5, "555-0140");
            pstmt.setString(6, "phil.cooper@hospital.com");
            pstmt.executeUpdate();
            pharmId = getLastInsertId(conn);

            // Lab Tech Lucy
            pstmt.setString(1, "lab_lucy");
            pstmt.setString(2, labPass);
            pstmt.setString(3, "LAB_TECH");
            pstmt.setString(4, "Lucy Vance");
            pstmt.setString(5, "555-0150");
            pstmt.setString(6, "lucy.vance@hospital.com");
            pstmt.executeUpdate();
            labTechId = getLastInsertId(conn);
        }

        // 2. Insert Doctor Details
        String docInsert = "INSERT INTO doctors (user_id, specialization, license_number, consultation_fee, availability_schedule, department) VALUES (?, ?, ?, ?, ?, ?);";
        int drSmithDocId, drJonesDocId, drMillerDocId;
        try (PreparedStatement pstmt = conn.prepareStatement(docInsert)) {
            // Dr. Smith - Cardiology
            pstmt.setInt(1, drSmithId);
            pstmt.setString(2, "Cardiology");
            pstmt.setString(3, "LIC-CAR-90812");
            pstmt.setDouble(4, 150.0);
            pstmt.setString(5, "{\"Monday\":\"09:00-17:00\",\"Wednesday\":\"09:00-17:00\",\"Friday\":\"09:00-13:00\"}");
            pstmt.setString(6, "Cardiology");
            pstmt.executeUpdate();
            drSmithDocId = getLastInsertId(conn);

            // Dr. Jones - Pediatrics
            pstmt.setInt(1, drJonesId);
            pstmt.setString(2, "Pediatrics");
            pstmt.setString(3, "LIC-PED-54210");
            pstmt.setDouble(4, 100.0);
            pstmt.setString(5, "{\"Tuesday\":\"08:00-16:00\",\"Thursday\":\"08:00-16:00\"}");
            pstmt.setString(6, "Pediatrics");
            pstmt.executeUpdate();
            drJonesDocId = getLastInsertId(conn);

            // Dr. Miller - General Surgery
            pstmt.setInt(1, drMillerId);
            pstmt.setString(2, "Surgery");
            pstmt.setString(3, "LIC-SUR-33299");
            pstmt.setDouble(4, 200.0);
            pstmt.setString(5, "{\"Monday\":\"10:00-18:00\",\"Thursday\":\"10:00-18:00\"}");
            pstmt.setString(6, "General Surgery");
            pstmt.executeUpdate();
            drMillerDocId = getLastInsertId(conn);
        }

        // 3. Insert Patients
        String patInsert = "INSERT INTO patients (id, name, dob, blood_group, contact, emergency_contact, medical_history, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(patInsert)) {
            // Patient 1
            pstmt.setString(1, "PT-10001");
            pstmt.setString(2, "John Doe");
            pstmt.setString(3, "1985-05-15");
            pstmt.setString(4, "O+");
            pstmt.setString(5, "555-0199");
            pstmt.setString(6, "Mary Doe (555-0198)");
            pstmt.setString(7, "Chronic high blood pressure. No known drug allergies.");
            pstmt.setString(8, "ACTIVE");
            pstmt.executeUpdate();

            // Patient 2
            pstmt.setString(1, "PT-10002");
            pstmt.setString(2, "Jane Smith");
            pstmt.setString(3, "1992-08-22");
            pstmt.setString(4, "A-");
            pstmt.setString(5, "555-0144");
            pstmt.setString(6, "Richard Smith (555-0143)");
            pstmt.setString(7, "Asthma history. Penicillin allergic.");
            pstmt.setString(8, "ADMITTED");
            pstmt.executeUpdate();

            // Patient 3
            pstmt.setString(1, "PT-10003");
            pstmt.setString(2, "Joy Jones");
            pstmt.setString(3, "2021-12-01");
            pstmt.setString(4, "B+");
            pstmt.setString(5, "555-0188");
            pstmt.setString(6, "Robert Jones (555-0112)");
            pstmt.setString(7, "None. Regular childhood vaccination schedule.");
            pstmt.setString(8, "ACTIVE");
            pstmt.executeUpdate();

            // Patient 4
            pstmt.setString(1, "PT-10004");
            pstmt.setString(2, "Alice Johnson");
            pstmt.setString(3, "1968-11-30");
            pstmt.setString(4, "AB+");
            pstmt.setString(5, "555-0167");
            pstmt.setString(6, "Fred Johnson (555-0166)");
            pstmt.setString(7, "Type 2 Diabetes. Diabetic neuropathy.");
            pstmt.setString(8, "DISCHARGED");
            pstmt.executeUpdate();
        }

        // 4. Insert Medicines
        String medInsert = "INSERT INTO medicines (name, generic_name, stock_qty, low_stock_threshold, price, expiry_date) VALUES (?, ?, ?, ?, ?, ?);";
        int amoxId, paraId, ibupId, insuId;
        try (PreparedStatement pstmt = conn.prepareStatement(medInsert)) {
            // Amoxicillin
            pstmt.setString(1, "Amoxicillin 500mg");
            pstmt.setString(2, "Amoxicillin");
            pstmt.setInt(3, 120);
            pstmt.setInt(4, 20);
            pstmt.setDouble(5, 12.50);
            pstmt.setString(6, "2027-06-30");
            pstmt.executeUpdate();
            amoxId = getLastInsertId(conn);

            // Paracetamol (Low Stock)
            pstmt.setString(1, "Paracetamol 500mg");
            pstmt.setString(2, "Acetaminophen");
            pstmt.setInt(3, 15);
            pstmt.setInt(4, 30);
            pstmt.setDouble(5, 3.20);
            pstmt.setString(6, "2028-01-15");
            pstmt.executeUpdate();
            paraId = getLastInsertId(conn);

            // Ibuprofen
            pstmt.setString(1, "Ibuprofen 400mg");
            pstmt.setString(2, "Ibuprofen");
            pstmt.setInt(3, 80);
            pstmt.setInt(4, 15);
            pstmt.setDouble(5, 6.50);
            pstmt.setString(6, "2027-12-31");
            pstmt.executeUpdate();
            ibupId = getLastInsertId(conn);

            // Insulin (Low Stock)
            pstmt.setString(1, "Insulin Glargine 100IU");
            pstmt.setString(2, "Insulin");
            pstmt.setInt(3, 8);
            pstmt.setInt(4, 10);
            pstmt.setDouble(5, 45.00);
            pstmt.setString(6, "2026-11-20");
            pstmt.executeUpdate();
            insuId = getLastInsertId(conn);
        }

        // 5. Insert Wards
        String wardInsert = "INSERT INTO wards (name, type) VALUES (?, ?);";
        int wardGenId, wardIcuId, wardPedId, wardSurId, wardMatId;
        try (PreparedStatement pstmt = conn.prepareStatement(wardInsert)) {
            // General
            pstmt.setString(1, "General Ward A");
            pstmt.setString(2, "GENERAL");
            pstmt.executeUpdate();
            wardGenId = getLastInsertId(conn);

            // ICU
            pstmt.setString(1, "Intensive Care Unit");
            pstmt.setString(2, "ICU");
            pstmt.executeUpdate();
            wardIcuId = getLastInsertId(conn);

            // Pediatric
            pstmt.setString(1, "Pediatric Ward B");
            pstmt.setString(2, "PEDIATRIC");
            pstmt.executeUpdate();
            wardPedId = getLastInsertId(conn);

            // Surgical
            pstmt.setString(1, "Surgical Ward C");
            pstmt.setString(2, "SURGICAL");
            pstmt.executeUpdate();
            wardSurId = getLastInsertId(conn);

            // Maternity
            pstmt.setString(1, "Maternity Ward D");
            pstmt.setString(2, "MATERNITY");
            pstmt.executeUpdate();
            wardMatId = getLastInsertId(conn);
        }

        // 6. Insert Beds
        String bedInsert = "INSERT INTO beds (ward_id, bed_number, status) VALUES (?, ?, ?);";
        int b1Id, b2Id, b3Id, b4Id, b5Id;
        try (PreparedStatement pstmt = conn.prepareStatement(bedInsert)) {
            // General Beds
            pstmt.setInt(1, wardGenId);
            pstmt.setString(2, "G-101");
            pstmt.setString(3, "AVAILABLE");
            pstmt.executeUpdate();
            b1Id = getLastInsertId(conn);

            pstmt.setInt(1, wardGenId);
            pstmt.setString(2, "G-102");
            pstmt.setString(3, "AVAILABLE");
            pstmt.executeUpdate();

            pstmt.setInt(1, wardGenId);
            pstmt.setString(2, "G-103");
            pstmt.setString(3, "UNDER_MAINTENANCE");
            pstmt.executeUpdate();

            // ICU Beds
            pstmt.setInt(1, wardIcuId);
            pstmt.setString(2, "ICU-201");
            pstmt.setString(3, "OCCUPIED"); // Occupied by Jane Smith
            pstmt.executeUpdate();
            b2Id = getLastInsertId(conn);

            pstmt.setInt(1, wardIcuId);
            pstmt.setString(2, "ICU-202");
            pstmt.setString(3, "AVAILABLE");
            pstmt.executeUpdate();

            // Pediatric Beds
            pstmt.setInt(1, wardPedId);
            pstmt.setString(2, "PED-301");
            pstmt.setString(3, "AVAILABLE");
            pstmt.executeUpdate();

            // Surgical Beds
            pstmt.setInt(1, wardSurId);
            pstmt.setString(2, "SUR-401");
            pstmt.setString(3, "AVAILABLE");
            pstmt.executeUpdate();

            // Maternity Beds
            pstmt.setInt(1, wardMatId);
            pstmt.setString(2, "MAT-501");
            pstmt.setString(3, "AVAILABLE");
            pstmt.executeUpdate();
        }

        // 7. Admissions (Jane Smith in ICU)
        String admInsert = "INSERT INTO admissions (patient_id, bed_id, admission_date, daily_rate) VALUES (?, ?, ?, ?);";
        int janeAdmId;
        try (PreparedStatement pstmt = conn.prepareStatement(admInsert)) {
            pstmt.setString(1, "PT-10002");
            pstmt.setInt(2, b2Id); // ICU-201
            pstmt.setString(3, "2026-05-28 14:30:00");
            pstmt.setDouble(4, 300.0); // ICU special daily rate
            pstmt.executeUpdate();
            janeAdmId = getLastInsertId(conn);
        }

        // 8. Non-Medicine Inventory
        String invInsert = "INSERT INTO inventory (item_name, item_type, stock_qty, unit, low_stock_threshold, last_restocked, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?);";
        int gloveId, syringeId;
        try (PreparedStatement pstmt = conn.prepareStatement(invInsert)) {
            pstmt.setString(1, "Latex Gloves Medium");
            pstmt.setString(2, "CONSUMABLE");
            pstmt.setInt(3, 45);
            pstmt.setString(4, "Boxes");
            pstmt.setInt(5, 50); // Low stock!
            pstmt.setString(6, "2026-04-10");
            pstmt.setString(7, "2028-09-30");
            pstmt.executeUpdate();
            gloveId = getLastInsertId(conn);

            pstmt.setString(1, "Syringes 5ml");
            pstmt.setString(2, "CONSUMABLE");
            pstmt.setInt(3, 450);
            pstmt.setString(4, "Pieces");
            pstmt.setInt(5, 100);
            pstmt.setString(6, "2026-05-01");
            pstmt.setString(7, "2029-05-01");
            pstmt.executeUpdate();
            syringeId = getLastInsertId(conn);

            pstmt.setString(1, "Automated Defibrillator");
            pstmt.setString(2, "EQUIPMENT");
            pstmt.setInt(3, 3);
            pstmt.setString(4, "Units");
            pstmt.setInt(5, 1);
            pstmt.setString(6, "2025-10-15");
            pstmt.setNull(7, java.sql.Types.VARCHAR);
            pstmt.executeUpdate();

            pstmt.setString(1, "Vital Signs Monitor");
            pstmt.setString(2, "EQUIPMENT");
            pstmt.setInt(3, 12);
            pstmt.setString(4, "Units");
            pstmt.setInt(5, 2);
            pstmt.setString(6, "2025-12-01");
            pstmt.setNull(7, java.sql.Types.VARCHAR);
            pstmt.executeUpdate();
        }

        // 9. Staff Shifts Scheduling (For current week)
        String shiftInsert = "INSERT INTO staff_shifts (user_id, shift_start, shift_end, role_assigned) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(shiftInsert)) {
            // Nurse Kelly Shift
            pstmt.setInt(1, nurseId);
            pstmt.setString(2, "2026-05-31 08:00:00");
            pstmt.setString(3, "2026-05-31 16:00:00");
            pstmt.setString(4, "NURSE");
            pstmt.executeUpdate();

            // Dr. Smith Shift
            pstmt.setInt(1, drSmithId);
            pstmt.setString(2, "2026-05-31 09:00:00");
            pstmt.setString(3, "2026-05-31 17:00:00");
            pstmt.setString(4, "DOCTOR");
            pstmt.executeUpdate();

            // Receptionist Amy Shift
            pstmt.setInt(1, recepId);
            pstmt.setString(2, "2026-05-31 07:30:00");
            pstmt.setString(3, "2026-05-31 15:30:00");
            pstmt.setString(4, "RECEPTIONIST");
            pstmt.executeUpdate();
        }

        // 10. Sample Appointments
        String apptInsert = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, notes) VALUES (?, ?, ?, ?, ?, ?);";
        int johnApptId, joyApptId;
        try (PreparedStatement pstmt = conn.prepareStatement(apptInsert)) {
            // John Doe Completed Appointment
            pstmt.setString(1, "PT-10001");
            pstmt.setInt(2, drSmithDocId); // Dr. Smith Cardial
            pstmt.setString(3, "2026-05-30");
            pstmt.setString(4, "10:30:00");
            pstmt.setString(5, "COMPLETED");
            pstmt.setString(6, "Routine checkup for hypertension management.");
            pstmt.executeUpdate();
            johnApptId = getLastInsertId(conn);

            // Joy Jones Confirmed Appointment (Today)
            pstmt.setString(1, "PT-10003");
            pstmt.setInt(2, drJonesDocId); // Dr. Jones Pediatrician
            pstmt.setString(3, "2026-05-31");
            pstmt.setString(4, "14:00:00");
            pstmt.setString(5, "CONFIRMED");
            pstmt.setString(6, "Mild cough and low-grade fever for 2 days.");
            pstmt.executeUpdate();
            joyApptId = getLastInsertId(conn);

            // Alice Johnson Cancelled Appointment
            pstmt.setString(1, "PT-10004");
            pstmt.setInt(2, drMillerDocId); // Dr. Miller Surgeon
            pstmt.setString(3, "2026-05-29");
            pstmt.setString(4, "11:00:00");
            pstmt.setString(5, "CANCELLED");
            pstmt.setString(6, "Patient requested reschedule due to transportation issues.");
            pstmt.executeUpdate();
        }

        // 11. EMR & Records (For John Doe Completed Appointment)
        String emrInsert = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, visit_date, treatment_plan) VALUES (?, ?, ?, ?, ?, ?);";
        int johnEmrId;
        try (PreparedStatement pstmt = conn.prepareStatement(emrInsert)) {
            pstmt.setInt(1, johnApptId);
            pstmt.setString(2, "PT-10001");
            pstmt.setInt(3, drSmithDocId);
            pstmt.setString(4, "Essential Hypertension - stabilized.");
            pstmt.setString(5, "2026-05-30");
            pstmt.setString(6, "Continue Amoxicillin 500mg as preventative post-procedure. Keep daily blood pressure logs.");
            pstmt.executeUpdate();
            johnEmrId = getLastInsertId(conn);
        }

        // 12. Prescriptions (For John Doe)
        String presInsert = "INSERT INTO prescriptions (record_id, doctor_id, patient_id, status) VALUES (?, ?, ?, ?);";
        int johnPresId;
        try (PreparedStatement pstmt = conn.prepareStatement(presInsert)) {
            pstmt.setInt(1, johnEmrId);
            pstmt.setInt(2, drSmithDocId);
            pstmt.setString(3, "PT-10001");
            pstmt.setString(4, "FILLED");
            pstmt.executeUpdate();
            johnPresId = getLastInsertId(conn);
        }

        // Prescription Items
        String presItemInsert = "INSERT INTO prescription_items (prescription_id, medicine_id, dosage, frequency, quantity) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(presItemInsert)) {
            pstmt.setInt(1, johnPresId);
            pstmt.setInt(2, amoxId); // Amoxicillin
            pstmt.setString(3, "500mg");
            pstmt.setString(4, "Three times daily");
            pstmt.setInt(5, 21); // 7 days course
            pstmt.executeUpdate();
        }

        // 13. Lab Requests (For John Doe)
        String labInsert = "INSERT INTO lab_requests (record_id, patient_id, test_name, requested_date, result_text, flag_abnormal, status) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(labInsert)) {
            pstmt.setInt(1, johnEmrId);
            pstmt.setString(2, "PT-10001");
            pstmt.setString(3, "Lipid Panel (Cholesterol)");
            pstmt.setString(4, "2026-05-30");
            pstmt.setString(5, "Total Cholesterol: 210 mg/dL (Abnormal), HDL: 45 mg/dL, LDL: 135 mg/dL.");
            pstmt.setInt(6, 1); // Abnormal flag!
            pstmt.setString(7, "COMPLETED");
            pstmt.executeUpdate();
        }

        // 14. Billing Invoices (For John Doe Completed visit)
        String billInsert = "INSERT INTO billing (patient_id, appointment_id, total_amount, paid_amount, payment_status, payment_method) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(billInsert)) {
            // John Doe visit invoice
            pstmt.setString(1, "PT-10001");
            pstmt.setInt(2, johnApptId);
            // 150 (Dr. Smith fee) + 50 (Lab lipid test) + 12.5*2 (Amoxicillin) = ~225.0
            pstmt.setDouble(3, 225.0);
            pstmt.setDouble(4, 225.0);
            pstmt.setString(5, "PAID");
            pstmt.setString(6, "CARD");
            pstmt.executeUpdate();
        }

        // 15. Emergency Room Queue
        String erInsert = "INSERT INTO emergency_queue (patient_id, triage_level, queue_priority, bed_id, status) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(erInsert)) {
            // Patient 2 (Jane Smith) is already admitted to ICU, so she is registered as ADMITTED in queue
            pstmt.setString(1, "PT-10002");
            pstmt.setString(2, "CRITICAL");
            pstmt.setInt(3, 1);
            pstmt.setInt(4, b2Id); // ICU-201
            pstmt.setString(5, "ADMITTED");
            pstmt.executeUpdate();
        }

        // 16. Internal Notifications (Alerts for Low Stocks)
        String notifInsert = "INSERT INTO notifications (user_id, message) VALUES (?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(notifInsert)) {
            pstmt.setInt(1, adminId);
            pstmt.setString(2, "Inventory Alert: 'Paracetamol 500mg' is below the threshold! Current stock: 15.");
            pstmt.executeUpdate();

            pstmt.setInt(1, adminId);
            pstmt.setString(2, "Inventory Alert: 'Insulin Glargine 100IU' is below the threshold! Current stock: 8.");
            pstmt.executeUpdate();

            pstmt.setInt(1, adminId);
            pstmt.setString(2, "Inventory Alert: 'Latex Gloves Medium' is below the threshold! Current stock: 45.");
            pstmt.executeUpdate();
        }
    }
}
