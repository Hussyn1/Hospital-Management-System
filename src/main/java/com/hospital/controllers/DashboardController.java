package com.hospital.controllers;

import com.hospital.enums.*;
import com.hospital.models.*;
import com.hospital.services.*;
import com.hospital.utils.AlertHelper;
import com.hospital.view.DashboardView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class DashboardController {

    // ── Fields (were @FXML, now plain) ────────────────────────────────────────
    private VBox      navigationBox;
    private Label     userNameLabel;
    private Label     userRoleLabel;
    private StackPane contentArea;

    // ── Stage + View ──────────────────────────────────────────────────────────
    private final Stage         stage;
    private final DashboardView view;

    // ── Services ──────────────────────────────────────────────────────────────
    private final AuthService        authService        = new AuthService();
    private final PatientService     patientService     = new PatientService();
    private final DoctorService      doctorService      = new DoctorService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final EMRService         emrService         = new EMRService();
    private final PharmacyService    pharmacyService    = new PharmacyService();
    private final LabService         labService         = new LabService();
    private final WardService        wardService        = new WardService();
    private final EmergencyService   emergencyService   = new EmergencyService();
    private final InventoryService   inventoryService   = new InventoryService();
    private final HRService          hrService          = new HRService();
    private final ReportsService     reportsService     = new ReportsService();
    private final BillingService     billingService     = new BillingService();

    private User   currentUser;
    private String activeTab = "overview";

    // ── Constructor ───────────────────────────────────────────────────────────
    public DashboardController(Stage stage) {
        this.stage = stage;
        this.view  = new DashboardView();

        this.navigationBox = view.getNavigationBox();
        this.userNameLabel = view.getUserNameLabel();
        this.userRoleLabel = view.getUserRoleLabel();
        this.contentArea   = view.getContentArea();

        view.getLogoutButton().setOnAction(e -> handleLogout());

        initialize();
    }

    // ── Build Scene ───────────────────────────────────────────────────────────
    public Scene buildScene() {
        Scene scene = new Scene(view.getRoot(), 1200, 800);
        scene.getStylesheets().add(
            getClass().getResource("/css/theme.css").toExternalForm());
        return scene;
    }

    // ── Initialize ────────────────────────────────────────────────────────────
    public void initialize() {
        currentUser = AuthService.getCurrentUser();
        if (currentUser == null) {
            currentUser = new User(1, "admin", "", Role.ADMIN,
                "System Administrator", "555-0100", "admin@hospital.com",
                LocalDateTime.now());
        }
        userNameLabel.setText(currentUser.getFullName());
        userRoleLabel.setText(currentUser.getRole().name() + " PORTAL");
        setupNavigation();
        navigateTo("overview");
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    private void handleLogout() {
        if (AlertHelper.showConfirmation("Sign Out",
                "Are you sure you want to sign out?",
                "Any unsaved active operations will be lost.")) {
            AuthService.logout();
            LoginController loginCtrl = new LoginController(stage);
            stage.setScene(loginCtrl.buildScene());
            stage.setTitle("Hospital Management System — Login");
            stage.centerOnScreen();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    private void setupNavigation() {
        navigationBox.getChildren().clear();
        Role role = currentUser.getRole();

        addNavButton("Overview / Home", "overview");

        if (role == Role.ADMIN || role == Role.RECEPTIONIST || role == Role.NURSE)
            addNavButton("Patients Management", "patients");
        if (role == Role.ADMIN)
            addNavButton("Doctors Directory", "doctors");
        if (role == Role.ADMIN || role == Role.RECEPTIONIST || role == Role.DOCTOR)
            addNavButton("Appointments Calendar", "appointments");
        if (role == Role.ADMIN || role == Role.DOCTOR)
            addNavButton("Clinical Records (EMR)", "emr");
        if (role == Role.ADMIN || role == Role.PHARMACIST || role == Role.DOCTOR)
            addNavButton("Pharmacy & Prescriptions", "pharmacy");
        if (role == Role.ADMIN || role == Role.LAB_TECH || role == Role.DOCTOR)
            addNavButton("Lab & Diagnostics", "lab");
        if (role == Role.ADMIN || role == Role.NURSE || role == Role.DOCTOR)
            addNavButton("Wards & Bed Grid", "wards");
        if (role == Role.ADMIN || role == Role.NURSE || role == Role.RECEPTIONIST)
            addNavButton("ER & Triage Queue", "emergency");
        if (role == Role.ADMIN || role == Role.RECEPTIONIST)
            addNavButton("Billing & Invoices", "billing");
        if (role == Role.ADMIN) {
            addNavButton("Staff Shifts & HR", "hr");
            addNavButton("Supplies & Inventory", "inventory");
            addNavButton("Reports & Analytics", "reports");
        }
    }

    private void addNavButton(String text, String tabId) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setOnAction(e -> navigateTo(tabId));
        navigationBox.getChildren().add(btn);
    }

    private void updateActiveButton(String tabId) {
        Role role = currentUser.getRole();
        List<String> tabs = new ArrayList<>();
        tabs.add("overview");
        if (role == Role.ADMIN || role == Role.RECEPTIONIST || role == Role.NURSE) tabs.add("patients");
        if (role == Role.ADMIN) tabs.add("doctors");
        if (role == Role.ADMIN || role == Role.RECEPTIONIST || role == Role.DOCTOR) tabs.add("appointments");
        if (role == Role.ADMIN || role == Role.DOCTOR) tabs.add("emr");
        if (role == Role.ADMIN || role == Role.PHARMACIST || role == Role.DOCTOR) tabs.add("pharmacy");
        if (role == Role.ADMIN || role == Role.LAB_TECH || role == Role.DOCTOR) tabs.add("lab");
        if (role == Role.ADMIN || role == Role.NURSE || role == Role.DOCTOR) tabs.add("wards");
        if (role == Role.ADMIN || role == Role.NURSE || role == Role.RECEPTIONIST) tabs.add("emergency");
        if (role == Role.ADMIN || role == Role.RECEPTIONIST) tabs.add("billing");
        if (role == Role.ADMIN) { tabs.add("hr"); tabs.add("inventory"); tabs.add("reports"); }

        int btnIndex = tabs.indexOf(tabId);
        for (int i = 0; i < navigationBox.getChildren().size(); i++) {
            if (navigationBox.getChildren().get(i) instanceof Button b) {
                b.getStyleClass().removeAll("sidebar-btn-active");
                if (i == btnIndex) b.getStyleClass().add("sidebar-btn-active");
            }
        }
    }

    private void navigateTo(String tabId) {
        activeTab = tabId;
        updateActiveButton(tabId);
        contentArea.getChildren().clear();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0;");

        VBox contentLayout = new VBox(20);
        contentLayout.setPadding(new Insets(20));
        contentLayout.setStyle("-fx-background-color: transparent;");

        switch (tabId) {
            case "overview"     -> buildOverview(contentLayout);
            case "patients"     -> buildPatientsView(contentLayout);
            case "doctors"      -> buildDoctorsView(contentLayout);
            case "appointments" -> buildAppointmentsView(contentLayout);
            case "emr"          -> buildEMRView(contentLayout);
            case "pharmacy"     -> buildPharmacyView(contentLayout);
            case "lab"          -> buildLabView(contentLayout);
            case "wards"        -> buildWardsView(contentLayout);
            case "emergency"    -> buildEmergencyView(contentLayout);
            case "billing"      -> buildBillingView(contentLayout);
            case "hr"           -> buildHRView(contentLayout);
            case "inventory"    -> buildInventoryView(contentLayout);
            case "reports"      -> buildReportsView(contentLayout);
        }

        scrollPane.setContent(contentLayout);
        contentArea.getChildren().add(scrollPane);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DIALOG HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPercentWidth(38);
        labelCol.setHalignment(javafx.geometry.HPos.RIGHT);
        labelCol.setHgrow(Priority.NEVER);
        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setPercentWidth(62);
        inputCol.setHgrow(Priority.ALWAYS);
        inputCol.setFillWidth(true);
        grid.getColumnConstraints().addAll(labelCol, inputCol);
        return grid;
    }

    private void addRow(GridPane grid, String labelText, javafx.scene.Node control, int row) {
        Label lbl = new Label(labelText);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-size: 13; -fx-text-fill: -hms-text-primary;");
        if (control instanceof Control ctrl) ctrl.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setFillWidth(control, true);
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 1: OVERVIEW
    // ══════════════════════════════════════════════════════════════════════════

    private void buildOverview(VBox container) {
        Label title = new Label("Dashboard Overview");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Real-time clinical metrics & health ecosystem status indicators.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        try {
            Map<String, Object> stats = reportsService.getDashboardStats();

            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(20);
            statsGrid.setVgap(20);
            statsGrid.setMaxWidth(Double.MAX_VALUE);
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            statsGrid.getColumnConstraints().addAll(col, col, col, col);

            statsGrid.add(createStatCard("TOTAL PATIENTS",        String.valueOf(stats.get("totalPatients")),    "👥",  "-hms-accent"),   0, 0);
            statsGrid.add(createStatCard("ACTIVE DOCTORS",        String.valueOf(stats.get("totalDoctors")),     "👨‍⚕️", "-hms-info"),     1, 0);
            statsGrid.add(createStatCard("TODAY'S APPOINTMENTS",  String.valueOf(stats.get("todayAppointments")),"📅",  "-hms-warning"),  2, 0);
            statsGrid.add(createStatCard("BED OCCUPANCY",
                stats.get("occupiedBeds") + " / " +
                ((int) stats.get("occupiedBeds") + (int) stats.get("availableBeds")),
                "🏥", "-hms-success"), 3, 0);

            statsGrid.add(createStatCard("ER WAITING LIST",       String.valueOf(stats.get("emergencyQueueSize")),   "🚨", "-hms-danger"),      0, 1);
            statsGrid.add(createStatCard("PENDING LABS",          String.valueOf(stats.get("pendingLabRequests")),   "🧪", "-hms-accent-soft"), 1, 1);
            statsGrid.add(createStatCard("PENDING PRESCRIPTIONS", String.valueOf(stats.get("pendingPrescriptions")), "💊", "-hms-info"),        2, 1);
            statsGrid.add(createStatCard("LOW STOCK PRODUCTS",
                String.valueOf((int) stats.get("lowStockMedicines") + (int) stats.get("lowStockInventory")),
                "📦", "-hms-danger"), 3, 1);

            container.getChildren().add(statsGrid);

            HBox bottomLayout = new HBox(20);
            HBox.setHgrow(bottomLayout, Priority.ALWAYS);

            // Left: Active ER Queue
            VBox erCard = new VBox(15);
            erCard.getStyleClass().add("card");
            HBox.setHgrow(erCard, Priority.ALWAYS);
            erCard.setMinWidth(400);
            Label erHeader = new Label("Active Emergency Priority Waiting List");
            erHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -hms-accent;");
            TableView<EmergencyPatient> erTable = new TableView<>();
            erTable.setPrefHeight(250);
            TableColumn<EmergencyPatient, String> patIdCol = new TableColumn<>("Patient ID");
            patIdCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
            TableColumn<EmergencyPatient, String> triageCol = new TableColumn<>("Triage Level");
            triageCol.setCellValueFactory(new PropertyValueFactory<>("triageLevel"));
            TableColumn<EmergencyPatient, String> dateCol = new TableColumn<>("Registered At");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("registeredAt"));
            TableColumn<EmergencyPatient, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            erTable.getColumns().addAll(patIdCol, triageCol, dateCol, statusCol);
            erTable.setItems(FXCollections.observableArrayList(emergencyService.getActiveWaitingQueue()));
            erCard.getChildren().addAll(erHeader, erTable);

            // Right: Quick shortcuts
            VBox actionCard = new VBox(15);
            actionCard.getStyleClass().add("card");
            actionCard.setMinWidth(300);
            Label actionHeader = new Label("System Direct Shortcuts");
            actionHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -hms-info;");
            Button btn1 = new Button("Register New Patient");     btn1.getStyleClass().add("btn-primary");   btn1.setMaxWidth(Double.MAX_VALUE); btn1.setOnAction(e -> navigateTo("patients"));
            Button btn2 = new Button("Book Appointment Slot");    btn2.getStyleClass().add("btn-secondary"); btn2.setMaxWidth(Double.MAX_VALUE); btn2.setOnAction(e -> navigateTo("appointments"));
            Button btn3 = new Button("View Bed Grid");            btn3.getStyleClass().add("btn-secondary"); btn3.setMaxWidth(Double.MAX_VALUE); btn3.setOnAction(e -> navigateTo("wards"));
            Button btn4 = new Button("Emergency Walk-in Triage"); btn4.getStyleClass().add("btn-danger");    btn4.setMaxWidth(Double.MAX_VALUE); btn4.setOnAction(e -> navigateTo("emergency"));
            actionCard.getChildren().addAll(actionHeader, btn1, btn2, btn3, btn4);

            bottomLayout.getChildren().addAll(erCard, actionCard);
            container.getChildren().add(bottomLayout);

        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Failed to fetch dashboard metrics.", e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createStatCard(String labelStr, String valueStr, String iconStr, String colorVar) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(iconStr);
        icon.setStyle("-fx-font-size: 24; -fx-text-fill: " + colorVar + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label(valueStr);
        val.getStyleClass().add("stat-value");
        top.getChildren().addAll(icon, spacer, val);
        Label label = new Label(labelStr);
        label.getStyleClass().add("stat-label");
        card.getChildren().addAll(top, label);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 2: PATIENTS
    // ══════════════════════════════════════════════════════════════════════════

    private void buildPatientsView(VBox container) {
        Label title = new Label("Patients Management Registry");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Register new patients, store demographics, view clinical statuses, and manage soft deletion.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search patients by name or ID...");
        Button registerBtn = new Button("+ Register New Patient");
        registerBtn.getStyleClass().add("btn-primary");
        toolbar.getChildren().addAll(searchField, registerBtn);
        container.getChildren().add(toolbar);

        TableView<Patient> patientTable = new TableView<>();
        patientTable.setPrefHeight(450);
        TableColumn<Patient, String> idCol      = new TableColumn<>("Patient ID");    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Patient, String> nameCol    = new TableColumn<>("Full Name");     nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Patient, String> dobCol     = new TableColumn<>("Date of Birth"); dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        TableColumn<Patient, String> bloodCol   = new TableColumn<>("Blood Group");   bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        TableColumn<Patient, String> contactCol = new TableColumn<>("Contact");       contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        TableColumn<Patient, String> statusCol  = new TableColumn<>("Status");        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        patientTable.getColumns().addAll(idCol, nameCol, dobCol, bloodCol, contactCol, statusCol);
        container.getChildren().add(patientTable);

        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        Button editBtn   = new Button("Edit Profile"); editBtn.getStyleClass().add("btn-secondary");
        Button deleteBtn = new Button("Delete Patient");      deleteBtn.getStyleClass().add("btn-danger");
        controls.getChildren().addAll(editBtn, deleteBtn);
        container.getChildren().add(controls);

        try {
            patientTable.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
            searchField.textProperty().addListener((obs, ov, nv) -> {
                try { patientTable.setItems(FXCollections.observableArrayList(patientService.searchPatients(nv)));
                } catch (SQLException e) { e.printStackTrace(); }
            });
        } catch (SQLException e) { e.printStackTrace(); }

        registerBtn.setOnAction(e -> showRegisterPatientDialog(() -> {
            try { patientTable.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
            } catch (SQLException ex) { ex.printStackTrace(); }
        }));

        editBtn.setOnAction(e -> {
            Patient selected = patientTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertHelper.showWarning("No Selection", "Please select a patient", "Select a patient row to edit.");
                return;
            }
            showEditPatientDialog(selected, () -> {
                try { patientTable.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
                } catch (SQLException ex) { ex.printStackTrace(); }
            });
        });

        deleteBtn.setOnAction(e -> {
            Patient selected = patientTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertHelper.showWarning("No Selection", "Please select a patient", "Select a patient row to soft-delete.");
                return;
            }
            if (AlertHelper.showConfirmation("Soft Delete",
                    "Are you sure you want to soft-delete patient " + selected.getName() + "?",
                    "Their billing and EMR records will be retained, but they will be removed from standard active search listings.")) {
                try {
                    patientService.softDeletePatient(selected.getId());
                    patientTable.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
                    AlertHelper.showInfo("Deleted Successfully", "Patient Soft Deleted", "The patient is marked as deleted.");
                } catch (SQLException ex) {
                    AlertHelper.showError("Error", "Soft delete failed", ex.getMessage());
                }
            }
        });
    }

    private void showRegisterPatientDialog(Runnable refreshCallback) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) contentArea.getChildren().remove(overlay); });

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(480);
        card.setOnMouseClicked(javafx.event.Event::consume);

        Label header = new Label("Register Demographic Patient Profile");
        header.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        TextField nameIn      = new TextField();
        DatePicker dobIn      = new DatePicker();
        ComboBox<String> bloodIn = new ComboBox<>(
            FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        TextField contactIn   = new TextField();
        TextField emergencyIn = new TextField();
        TextArea historyIn    = new TextArea(); historyIn.setPrefRowCount(3); historyIn.setWrapText(true);

        addRow(grid, "Patient Full Name:",  nameIn,      0);
        addRow(grid, "Date of Birth:",      dobIn,       1);
        addRow(grid, "Blood Group:",        bloodIn,     2);
        addRow(grid, "Phone Contact:",      contactIn,   3);
        addRow(grid, "Emergency Contact:",  emergencyIn, 4);
        addRow(grid, "Medical History:",    historyIn,   5);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: -hms-danger; -fx-font-size: 12;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        HBox btnBox = new HBox(15); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel"); cancel.getStyleClass().add("btn-secondary");
        Button save   = new Button("Register Patient"); save.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, grid, errorLbl, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        save.setOnAction(ex -> {
            errorLbl.setVisible(false);
            try {
                Patient p = new Patient();
                p.setName(nameIn.getText()); p.setDob(dobIn.getValue());
                p.setBloodGroup(bloodIn.getValue()); p.setContact(contactIn.getText());
                p.setEmergencyContact(emergencyIn.getText()); p.setMedicalHistory(historyIn.getText());
                String newId = patientService.registerPatient(p);
                AlertHelper.showInfo("Patient Registered", "Registration Successful!", "Patient unique ID is: " + newId);
                contentArea.getChildren().remove(overlay);
                refreshCallback.run();
            } catch (Exception err) { errorLbl.setText(err.getMessage()); errorLbl.setVisible(true); }
        });
    }

    private void showEditPatientDialog(Patient p, Runnable refreshCallback) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) contentArea.getChildren().remove(overlay); });

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(480);
        card.setOnMouseClicked(javafx.event.Event::consume);

        Label header = new Label("Edit Demographic Patient Profile: " + p.getId());
        header.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        TextField nameIn      = new TextField(p.getName());
        DatePicker dobIn      = new DatePicker(p.getDob());
        ComboBox<String> bloodIn = new ComboBox<>(
            FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        bloodIn.setValue(p.getBloodGroup());
        TextField contactIn   = new TextField(p.getContact());
        TextField emergencyIn = new TextField(p.getEmergencyContact());
        TextArea historyIn    = new TextArea(p.getMedicalHistory()); historyIn.setPrefRowCount(3);

        addRow(grid, "Patient Full Name:",  nameIn,      0);
        addRow(grid, "Date of Birth:",      dobIn,       1);
        addRow(grid, "Blood Group:",        bloodIn,     2);
        addRow(grid, "Phone Contact:",      contactIn,   3);
        addRow(grid, "Emergency Contact:",  emergencyIn, 4);
        addRow(grid, "Medical History:",    historyIn,   5);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: -hms-danger; -fx-font-size: 12;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        HBox btnBox = new HBox(15); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel"); cancel.getStyleClass().add("btn-secondary");
        Button save   = new Button("Save Changes"); save.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, grid, errorLbl, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        save.setOnAction(ex -> {
            errorLbl.setVisible(false);
            try {
                p.setName(nameIn.getText()); p.setDob(dobIn.getValue());
                p.setBloodGroup(bloodIn.getValue()); p.setContact(contactIn.getText());
                p.setEmergencyContact(emergencyIn.getText()); p.setMedicalHistory(historyIn.getText());
                patientService.updatePatient(p);
                AlertHelper.showInfo("Changes Saved", "Success!", "Patient demographic profile has been updated.");
                contentArea.getChildren().remove(overlay);
                refreshCallback.run();
            } catch (Exception err) { errorLbl.setText(err.getMessage()); errorLbl.setVisible(true); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 3: DOCTORS
    // ══════════════════════════════════════════════════════════════════════════

    private void buildDoctorsView(VBox container) {
        Label title = new Label("Doctors Directory & Department Schedules");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Add and view doctor profiles, consulting schedules, departments, and consultation fees.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search doctors by specialization or name...");
        Button addDoctorBtn = new Button("+ Add Doctor Profile");
        addDoctorBtn.getStyleClass().add("btn-primary");
        toolbar.getChildren().addAll(searchField, addDoctorBtn);
        container.getChildren().add(toolbar);

        TableView<Doctor> docTable = new TableView<>();
        docTable.setPrefHeight(450);
        TableColumn<Doctor, Integer> idCol   = new TableColumn<>("ID");             idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Doctor, String> nameCol  = new TableColumn<>("Doctor Name");    nameCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        TableColumn<Doctor, String> specCol  = new TableColumn<>("Specialization"); specCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        TableColumn<Doctor, String> deptCol  = new TableColumn<>("Department");     deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        TableColumn<Doctor, Double> feeCol   = new TableColumn<>("Fee ($)");        feeCol.setCellValueFactory(new PropertyValueFactory<>("consultationFee"));
        TableColumn<Doctor, String> schedCol = new TableColumn<>("Schedules");      schedCol.setCellValueFactory(new PropertyValueFactory<>("availabilitySchedule"));
        docTable.getColumns().addAll(idCol, nameCol, specCol, deptCol, feeCol, schedCol);
        container.getChildren().add(docTable);

        try {
            docTable.setItems(FXCollections.observableArrayList(doctorService.getAllDoctors()));
            searchField.textProperty().addListener((o, ov, nv) -> {
                try { docTable.setItems(FXCollections.observableArrayList(doctorService.searchDoctors(nv)));
                } catch (SQLException ex) { ex.printStackTrace(); }
            });
        } catch (SQLException ex) { ex.printStackTrace(); }

        addDoctorBtn.setOnAction(e -> showAddDoctorDialog(() -> {
            try { docTable.setItems(FXCollections.observableArrayList(doctorService.getAllDoctors()));
            } catch (SQLException ex) { ex.printStackTrace(); }
        }));
    }

    private void showAddDoctorDialog(Runnable refreshCallback) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) contentArea.getChildren().remove(overlay); });

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(520);
        card.setOnMouseClicked(javafx.event.Event::consume);

        Label header = new Label("Register Doctor & User Account");
        header.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        TextField     nameIn     = new TextField();
        TextField     usernameIn = new TextField();
        PasswordField passwordIn = new PasswordField();
        TextField     emailIn    = new TextField();
        TextField     contactIn  = new TextField();
        TextField     specIn     = new TextField();
        TextField     licenseIn  = new TextField();
        TextField     feeIn      = new TextField();
        TextField     deptIn     = new TextField();
        TextArea      schedIn    = new TextArea(
            "{\"Monday\":\"09:00-17:00\",\"Tuesday\":\"09:00-17:00\",\"Wednesday\":\"09:00-17:00\",\"Thursday\":\"09:00-17:00\",\"Friday\":\"09:00-17:00\"}");
        schedIn.setPrefRowCount(3); schedIn.setWrapText(true);

        addRow(grid, "Doctor Full Name:",     nameIn,     0);
        addRow(grid, "Portal Username:",      usernameIn, 1);
        addRow(grid, "Portal Password:",      passwordIn, 2);
        addRow(grid, "Email Contact:",        emailIn,    3);
        addRow(grid, "Phone Contact:",        contactIn,  4);
        addRow(grid, "Specialization:",       specIn,     5);
        addRow(grid, "License Number:",       licenseIn,  6);
        addRow(grid, "Consultation Fee ($):", feeIn,      7);
        addRow(grid, "Department:",           deptIn,     8);
        addRow(grid, "Schedule JSON:",        schedIn,    9);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: -hms-danger; -fx-font-size: 12;");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0;");
        scrollPane.setPrefViewportHeight(360);

        HBox btnBox = new HBox(15); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel"); cancel.getStyleClass().add("btn-secondary");
        Button save   = new Button("Save Doctor Profile"); save.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, scrollPane, errorLbl, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        save.setOnAction(ex -> {
            errorLbl.setVisible(false);
            try {
                String sched = schedIn.getText().trim();
                if (!sched.startsWith("{") || !sched.endsWith("}"))
                    throw new IllegalArgumentException("Schedule must be valid JSON, e.g. {\"Monday\":\"09:00-17:00\"}");
                Doctor d = new Doctor();
                d.setDoctorName(nameIn.getText()); d.setContact(contactIn.getText());
                d.setSpecialization(specIn.getText()); d.setLicenseNumber(licenseIn.getText());
                d.setConsultationFee(Double.parseDouble(feeIn.getText()));
                d.setDepartment(deptIn.getText()); d.setAvailabilitySchedule(sched);
                boolean success = doctorService.registerDoctor(
                    d, passwordIn.getText(), usernameIn.getText(), emailIn.getText());
                if (success) {
                    AlertHelper.showInfo("Doctor Added", "Registration Successful",
                        "The doctor has been added and a portal account created.");
                    contentArea.getChildren().remove(overlay);
                    refreshCallback.run();
                } else {
                    errorLbl.setText("Registration failed. Username or email may already exist.");
                    errorLbl.setVisible(true);
                }
            } catch (NumberFormatException nfe) {
                errorLbl.setText("Consultation fee must be a valid number.");
                errorLbl.setVisible(true);
            } catch (Exception err) { errorLbl.setText(err.getMessage()); errorLbl.setVisible(true); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 4: APPOINTMENTS
    // ══════════════════════════════════════════════════════════════════════════

    private void buildAppointmentsView(VBox container) {
        Label title = new Label("Appointments Scheduling Calendar");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Book appointments with instant doctor collision checks, slot availability, and lifecycle status changes.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox mainLayout = new HBox(20);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        // Left: Booking Form
        VBox formCard = new VBox(15);
        formCard.getStyleClass().add("card");
        formCard.setMinWidth(380);
        Label formHeader = new Label("Book A Consultation Slot");
        formHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        GridPane grid = new GridPane(); grid.setHgap(15); grid.setVgap(12);
        ComboBox<Patient> patCombo = new ComboBox<>(); patCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<Doctor> docCombo  = new ComboBox<>(); docCombo.setMaxWidth(Double.MAX_VALUE);
        DatePicker datePicker = new DatePicker(LocalDate.now()); datePicker.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> timeCombo = new ComboBox<>(); timeCombo.setMaxWidth(Double.MAX_VALUE);

        List<String> hoursList = new ArrayList<>();
        for (int h = 9; h <= 17; h++) {
            hoursList.add(String.format("%02d:00", h));
            hoursList.add(String.format("%02d:30", h));
        }
        timeCombo.setItems(FXCollections.observableArrayList(hoursList));
        TextArea notesIn = new TextArea(); notesIn.setPrefRowCount(3);

        grid.add(new Label("Patient Name:"), 0, 0);      grid.add(patCombo,   1, 0);
        grid.add(new Label("Doctor Specialty:"), 0, 1);  grid.add(docCombo,   1, 1);
        grid.add(new Label("Date:"), 0, 2);              grid.add(datePicker, 1, 2);
        grid.add(new Label("Time Slot:"), 0, 3);         grid.add(timeCombo,  1, 3);
        grid.add(new Label("Consultation Notes:"), 0, 4); grid.add(notesIn,  1, 4);

        Button bookBtn = new Button("Save Appointment");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        formCard.getChildren().addAll(formHeader, grid, bookBtn);

        // Right: Appointments Table
        VBox tableCard = new VBox(15);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        Label tableHeader = new Label("Scheduled Consultations");
        tableHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -hms-info;");

        TableView<Appointment> apptTable = new TableView<>();
        apptTable.setPrefHeight(400);
        TableColumn<Appointment, Integer> apptIdCol   = new TableColumn<>("ID");        apptIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Appointment, String> patNameCol   = new TableColumn<>("Patient");    patNameCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        TableColumn<Appointment, String> docNameCol   = new TableColumn<>("Doctor");     docNameCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        TableColumn<Appointment, String> apptDateCol  = new TableColumn<>("Date");       apptDateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        TableColumn<Appointment, String> apptTimeCol  = new TableColumn<>("Time");       apptTimeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        TableColumn<Appointment, String> apptStatusCol = new TableColumn<>("Status");    apptStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        apptTable.getColumns().addAll(apptIdCol, patNameCol, docNameCol, apptDateCol, apptTimeCol, apptStatusCol);

        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_LEFT);
        Button confirm  = new Button("Confirm");        confirm.getStyleClass().add("btn-success");
        Button cancelA  = new Button("Cancel");         cancelA.getStyleClass().add("btn-danger");
        Button complete = new Button("Mark Completed"); complete.getStyleClass().add("btn-secondary");
        btnBox.getChildren().addAll(confirm, cancelA, complete);

        tableCard.getChildren().addAll(tableHeader, apptTable, btnBox);
        mainLayout.getChildren().addAll(formCard, tableCard);
        container.getChildren().add(mainLayout);

        // Populate
        try {
            patCombo.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
            docCombo.setItems(FXCollections.observableArrayList(doctorService.getAllDoctors()));
            apptTable.setItems(FXCollections.observableArrayList(appointmentService.getAllAppointments()));

            patCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
                }
            });
            patCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
                }
            });
            docCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Doctor item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getDoctorName() + " (" + item.getSpecialization() + ")");
                }
            });
            docCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Doctor item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getDoctorName() + " (" + item.getSpecialization() + ")");
                }
            });
        } catch (SQLException ex) { ex.printStackTrace(); }

        bookBtn.setOnAction(e -> {
            try {
                Patient selPat = patCombo.getValue();
                Doctor selDoc  = docCombo.getValue();
                LocalDate date = datePicker.getValue();
                String slot    = timeCombo.getValue();
                if (selPat == null || selDoc == null || date == null || slot == null)
                    throw new IllegalArgumentException("All consultation booking parameters are required.");
                Appointment appt = new Appointment();
                appt.setPatientId(selPat.getId()); appt.setDoctorId(selDoc.getId());
                appt.setAppointmentDate(date); appt.setAppointmentTime(LocalTime.parse(slot));
                appt.setNotes(notesIn.getText());
                appointmentService.bookAppointment(appt);
                apptTable.setItems(FXCollections.observableArrayList(appointmentService.getAllAppointments()));
                AlertHelper.showInfo("Appointment Booked", "Consultation Slot Secured",
                    "The appointment is successfully booked in PENDING state.");
                notesIn.clear();
            } catch (Exception ex) {
                AlertHelper.showError("Booking Overlap Conflict", "Consultation booking rejected.", ex.getMessage());
            }
        });

        confirm.setOnAction(e -> {
            Appointment sel = apptTable.getSelectionModel().getSelectedItem();
            if (sel != null) try {
                appointmentService.confirmAppointment(sel.getId());
                apptTable.setItems(FXCollections.observableArrayList(appointmentService.getAllAppointments()));
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        cancelA.setOnAction(e -> {
            Appointment sel = apptTable.getSelectionModel().getSelectedItem();
            if (sel != null) try {
                appointmentService.cancelAppointment(sel.getId());
                apptTable.setItems(FXCollections.observableArrayList(appointmentService.getAllAppointments()));
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        complete.setOnAction(e -> {
            Appointment sel = apptTable.getSelectionModel().getSelectedItem();
            if (sel != null) try {
                appointmentService.completeAppointment(sel.getId());
                apptTable.setItems(FXCollections.observableArrayList(appointmentService.getAllAppointments()));
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 5: EMR
    // ══════════════════════════════════════════════════════════════════════════

    private void buildEMRView(VBox container) {
        Label title = new Label("Electronic Medical Records (EMR) Suite");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Add consult entries, diagnosis plans, prescribe medications, and track patient clinical history chronologically.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox layout = new HBox(20);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Left: Patient List
        VBox patListCard = new VBox(10);
        patListCard.getStyleClass().add("card");
        patListCard.setMinWidth(300);
        TextField searchPat = new TextField();
        searchPat.getStyleClass().add("search-field");
        searchPat.setPromptText("Filter patient EMR profile...");
        ListView<Patient> patListView = new ListView<>();
        patListView.setPrefHeight(500);
        patListCard.getChildren().addAll(new Label("Select Patient EMR"), searchPat, patListView);

        // Right: EMR Detail Area
        VBox emrMainArea = new VBox(15);
        HBox.setHgrow(emrMainArea, Priority.ALWAYS);

        VBox timelineCard = new VBox(10);
        timelineCard.getStyleClass().add("card");
        timelineCard.setPrefHeight(280);
        Label timelineHeader = new Label("Chronological EMR Timeline Details");
        timelineHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-accent;");
        TextArea timelineText = new TextArea("Select a patient from the left side panel to view clinical medical history.");
        timelineText.setEditable(false); timelineText.setWrapText(true);
        VBox.setVgrow(timelineText, Priority.ALWAYS);
        timelineCard.getChildren().addAll(timelineHeader, timelineText);

        VBox consultCard = new VBox(10);
        consultCard.getStyleClass().add("card");
        Label consultHeader = new Label("Write New Consult Entry & Prescribe / Lab Orders");
        consultHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-info;");

        GridPane consultGrid = new GridPane(); consultGrid.setHgap(15); consultGrid.setVgap(10);
        ComboBox<Appointment> apptCombo = new ComboBox<>(); apptCombo.setMaxWidth(Double.MAX_VALUE);
        TextField diagIn = new TextField(); diagIn.setPromptText("E.g. Acute Bronchitis, Essential Hypertension");
        TextArea planIn  = new TextArea();  planIn.setPrefRowCount(2);
        planIn.setPromptText("E.g. Bed rest 3 days, drink lots of warm fluids, follow-up in 1 week.");

        consultGrid.add(new Label("Select Active Appointment:"), 0, 0); consultGrid.add(apptCombo, 1, 0);
        consultGrid.add(new Label("Clinical Diagnosis:"), 0, 1);        consultGrid.add(diagIn,    1, 1);
        consultGrid.add(new Label("Treatment Action Plan:"), 0, 2);     consultGrid.add(planIn,    1, 2);

        HBox consultBtnBox = new HBox(15); consultBtnBox.setAlignment(Pos.CENTER_RIGHT);
        Button addPresBtn    = new Button("+ Add Rx Prescription"); addPresBtn.getStyleClass().add("btn-secondary");
        Button addLabBtn     = new Button("+ Order Lab Test");      addLabBtn.getStyleClass().add("btn-secondary");
        Button saveConsultBtn = new Button("Finalize Clinical Record"); saveConsultBtn.getStyleClass().add("btn-primary");
        consultBtnBox.getChildren().addAll(addLabBtn, addPresBtn, saveConsultBtn);

        consultCard.getChildren().addAll(consultHeader, consultGrid, consultBtnBox);
        emrMainArea.getChildren().addAll(timelineCard, consultCard);
        layout.getChildren().addAll(patListCard, emrMainArea);
        container.getChildren().add(layout);

        List<PrescriptionItem> activeRxItems = new ArrayList<>();
        List<String> activeLabTests = new ArrayList<>();

        // Load patients
        try {
            ObservableList<Patient> pats = FXCollections.observableArrayList(patientService.getAllPatients());
            patListView.setItems(pats);
            patListView.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
                }
            });
            searchPat.textProperty().addListener((o, ov, nv) -> {
                try { patListView.setItems(FXCollections.observableArrayList(patientService.searchPatients(nv)));
                } catch (SQLException ex) { ex.printStackTrace(); }
            });
        } catch (SQLException ex) { ex.printStackTrace(); }

        // Selection listener
        patListView.getSelectionModel().selectedItemProperty().addListener((o, oldPat, newPat) -> {
            if (newPat == null) return;
            activeRxItems.clear(); activeLabTests.clear(); diagIn.clear(); planIn.clear();
            try {
                List<MedicalRecord> history = emrService.getPatientHistory(newPat.getId());
                if (history.isEmpty()) {
                    timelineText.setText("No clinical history records found for patient: " + newPat.getName());
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (MedicalRecord mr : history) {
                        sb.append("=========================================\n");
                        sb.append(String.format("VISIT DATE: %s  |  RECORD ID: #MR-%d\n", mr.getVisitDate(), mr.getId()));
                        sb.append(String.format("CONSULTING DOCTOR: ID #%d\n", mr.getDoctorId()));
                        sb.append(String.format("DIAGNOSIS: %s\n", mr.getDiagnosis()));
                        sb.append(String.format("TREATMENT PLAN: %s\n\n", mr.getTreatmentPlan()));

                        List<LabRequest> labs = labService.getPatientLabHistory(newPat.getId());
                        boolean hasLabs = false;
                        for (LabRequest lr : labs) {
                            if (lr.getRecordId() != null && lr.getRecordId() == mr.getId()) {
                                if (!hasLabs) { sb.append("  ORDERED LABORATORY TEST RESULTS:\n"); hasLabs = true; }
                                sb.append(String.format("    - [%s] %s: %s (Status: %s)\n",
                                    lr.isFlagAbnormal() ? "ABNORMAL" : "NORMAL",
                                    lr.getTestName(), lr.getResultText(), lr.getStatus()));
                            }
                        }

                        List<Prescription> presList = pharmacyService.getAllPrescriptions();
                        boolean hasRx = false;
                        for (Prescription pr : presList) {
                            if (pr.getRecordId() == mr.getId()) {
                                if (!hasRx) { sb.append("\n  DISPENSED PHARMACY PRESCRIPTIONS:\n"); hasRx = true; }
                                for (PrescriptionItem pri : pr.getItems()) {
                                    sb.append(String.format("    - Medicine ID %d | Qty: %d | Dosage: %s | Freq: %s\n",
                                        pri.getMedicineId(), pri.getQuantity(), pri.getDosage(), pri.getFrequency()));
                                }
                            }
                        }
                        sb.append("\n");
                    }
                    timelineText.setText(sb.toString());
                }

                List<Appointment> allAppts = appointmentService.getPatientAppointments(newPat.getId());
                List<Appointment> pendingOrConfirmed = new ArrayList<>();
                for (Appointment ap : allAppts) {
                    if (ap.getStatus() == AppointmentStatus.CONFIRMED || ap.getStatus() == AppointmentStatus.PENDING)
                        pendingOrConfirmed.add(ap);
                }
                apptCombo.setItems(FXCollections.observableArrayList(pendingOrConfirmed));
                apptCombo.setCellFactory(lv -> new ListCell<>() {
                    @Override protected void updateItem(Appointment item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : "Appt ID #" + item.getId() + " - Date: " + item.getAppointmentDate() + " @ " + item.getAppointmentTime());
                    }
                });
                apptCombo.setButtonCell(new ListCell<>() {
                    @Override protected void updateItem(Appointment item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : "Appt ID #" + item.getId() + " - Date: " + item.getAppointmentDate() + " @ " + item.getAppointmentTime());
                    }
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        addPresBtn.setOnAction(e -> {
            if (patListView.getSelectionModel().getSelectedItem() == null) {
                AlertHelper.showWarning("Warning", "Select Patient First",
                    "Please select a patient from the left side panel before compiling prescription items.");
                return;
            }
            showAddPrescriptionDialog(activeRxItems);
        });

        addLabBtn.setOnAction(e -> {
            if (patListView.getSelectionModel().getSelectedItem() == null) {
                AlertHelper.showWarning("Warning", "Select Patient First",
                    "Please select a patient from the left side panel before ordering diagnostic tests.");
                return;
            }
            showAddLabRequestDialog(activeLabTests);
        });

        saveConsultBtn.setOnAction(e -> {
            Patient selPat   = patListView.getSelectionModel().getSelectedItem();
            Appointment selAppt = apptCombo.getValue();
            if (selPat == null)  { AlertHelper.showWarning("Warning", "Select Patient First", "Select patient from the left panel."); return; }
            if (selAppt == null) { AlertHelper.showWarning("Warning", "Select Active Appointment", "Please select an active appointment slot."); return; }
            if (diagIn.getText().trim().isEmpty()) { AlertHelper.showWarning("Warning", "Diagnosis Required", "Please enter diagnostic evaluation findings."); return; }
            try {
                MedicalRecord mr = new MedicalRecord();
                mr.setAppointmentId(selAppt.getId()); mr.setPatientId(selPat.getId());
                mr.setDoctorId(selAppt.getDoctorId()); mr.setDiagnosis(diagIn.getText()); mr.setTreatmentPlan(planIn.getText());
                emrService.addMedicalRecord(mr);

                List<MedicalRecord> allMr = emrService.getPatientHistory(selPat.getId());
                MedicalRecord newestMr = allMr.get(0);

                if (!activeRxItems.isEmpty()) {
                    Prescription prescription = new Prescription();
                    prescription.setRecordId(newestMr.getId()); prescription.setDoctorId(selAppt.getDoctorId());
                    prescription.setPatientId(selPat.getId()); prescription.setItems(activeRxItems);
                    pharmacyService.createPrescription(prescription);
                }

                if (!activeLabTests.isEmpty()) {
                    for (String test : activeLabTests) {
                        LabRequest lr = new LabRequest();
                        lr.setRecordId(newestMr.getId()); lr.setPatientId(selPat.getId()); lr.setTestName(test);
                        labService.requestLabTest(lr);
                    }
                }

                appointmentService.completeAppointment(selAppt.getId());
                billingService.generateBill(selPat.getId(), selAppt.getId(), null);

                AlertHelper.showInfo("Consult Closed", "EMR Record Saved Successfully",
                    "Consult entry successfully registered. Associated billing invoice has been automatically compiled.");
                patListView.getSelectionModel().select(selPat);
            } catch (Exception ex) {
                AlertHelper.showError("Save Failure", "Failed to finalize consultation entry.", ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void showAddPrescriptionDialog(List<PrescriptionItem> rxItems) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(400);

        Label header = new Label("Add Medicine Prescription Item");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        ComboBox<Medicine> medCombo = new ComboBox<>();
        TextField dosageIn = new TextField(); dosageIn.setPromptText("E.g. 500mg, 1 tablet");
        TextField freqIn   = new TextField(); freqIn.setPromptText("E.g. Twice daily, twice a day");
        TextField qtyIn    = new TextField(); qtyIn.setPromptText("E.g. 15, 30");

        addRow(grid, "Select Medicine:",      medCombo, 0);
        addRow(grid, "Dosage:",              dosageIn, 1);
        addRow(grid, "Frequency:",           freqIn,   2);
        addRow(grid, "Total Dispense Qty:",  qtyIn,    3);

        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Close"); cancel.getStyleClass().add("btn-secondary");
        Button add    = new Button("Add Item"); add.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, add);

        card.getChildren().addAll(header, grid, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        try {
            medCombo.setItems(FXCollections.observableArrayList(pharmacyService.getAllMedicines()));
            medCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Medicine item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " ($" + item.getPrice() + " | Stock: " + item.getStockQty() + ")");
                }
            });
            medCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Medicine item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        add.setOnAction(ex -> {
            try {
                Medicine med = medCombo.getValue();
                if (med == null) throw new IllegalArgumentException("Select a medicine product.");
                int q = Integer.parseInt(qtyIn.getText());
                if (q <= 0) throw new IllegalArgumentException("Quantity must be positive.");
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineId(med.getId()); item.setDosage(dosageIn.getText());
                item.setFrequency(freqIn.getText()); item.setQuantity(q); item.setPrice(med.getPrice());
                rxItems.add(item);
                AlertHelper.showInfo("Added", "Item added to active prescription list",
                    med.getName() + " - " + dosageIn.getText());
                contentArea.getChildren().remove(overlay);
            } catch (Exception err) {
                AlertHelper.showError("Input Failure", "Failed to compile prescription item.", err.getMessage());
            }
        });
    }

    private void showAddLabRequestDialog(List<String> labTests) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(380);

        Label header = new Label("Add Laboratory Test Request");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        ComboBox<String> testCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Complete Blood Count (CBC)", "Lipid Panel (Cholesterol)", "Basic Metabolic Panel (BMP)",
            "Thyroid Panel (TSH)", "Urinalysis", "X-Ray Chest", "MRI Brain Scan", "Ultrasound Abdomen"));
        testCombo.setEditable(true);

        addRow(grid, "Select / Enter Test Name:", testCombo, 0);

        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Close"); cancel.getStyleClass().add("btn-secondary");
        Button add    = new Button("Order Test"); add.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, add);

        card.getChildren().addAll(header, grid, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        add.setOnAction(ex -> {
            String test = testCombo.getValue();
            if (test == null || test.trim().isEmpty()) {
                AlertHelper.showWarning("Warning", "Input Required", "Please specify a diagnostic procedure test name.");
                return;
            }
            labTests.add(test);
            AlertHelper.showInfo("Added", "Lab Test Added to order list", test);
            contentArea.getChildren().remove(overlay);
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 6: PHARMACY
    // ══════════════════════════════════════════════════════════════════════════

    private void buildPharmacyView(VBox container) {
        Label title = new Label("Pharmacy Operations & Medical Dispensary");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Manage medicines catalog, track stock quantity metrics, and process patient prescription orders.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        TabPane tabPane = new TabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Prescription Queue
        Tab qTab = new Tab("Pending Prescriptions"); qTab.setClosable(false);
        VBox qLayout = new VBox(15); qLayout.setPadding(new Insets(15));

        TableView<Prescription> rxTable = new TableView<>(); rxTable.setPrefHeight(350);
        TableColumn<Prescription, Integer> rxId     = new TableColumn<>("Rx ID");     rxId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Prescription, String>  rxPat    = new TableColumn<>("Patient ID"); rxPat.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        TableColumn<Prescription, Integer> rxDoc    = new TableColumn<>("Doctor ID");  rxDoc.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        TableColumn<Prescription, String>  rxStatus = new TableColumn<>("Status");     rxStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        rxTable.getColumns().addAll(rxId, rxPat, rxDoc, rxStatus);

        TextArea rxDetailsText = new TextArea("Select a pending prescription order to review medicine checklist.");
        rxDetailsText.setEditable(false); rxDetailsText.setPrefRowCount(4);
        Button dispenseBtn = new Button("Dispense Medications");
        dispenseBtn.getStyleClass().add("btn-primary"); dispenseBtn.setMaxWidth(Double.MAX_VALUE);
        qLayout.getChildren().addAll(rxTable, rxDetailsText, dispenseBtn);
        qTab.setContent(qLayout);

        // Tab 2: Medicines Catalog
        Tab mTab = new Tab("Medicines Stock Directory"); mTab.setClosable(false);
        VBox mLayout = new VBox(15); mLayout.setPadding(new Insets(15));
        HBox mBar = new HBox(15);
        TextField searchMed = new TextField(); searchMed.getStyleClass().add("search-field"); searchMed.setPromptText("Filter medicine catalog...");
        Button addMedBtn = new Button("+ Add Medicine Product"); addMedBtn.getStyleClass().add("btn-primary");
        mBar.getChildren().addAll(searchMed, addMedBtn);

        TableView<Medicine> medTable = new TableView<>(); medTable.setPrefHeight(350);
        TableColumn<Medicine, Integer> mId    = new TableColumn<>("ID");           mId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Medicine, String>  mName  = new TableColumn<>("Product Name"); mName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Medicine, String>  mGen   = new TableColumn<>("Generic Name"); mGen.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        TableColumn<Medicine, Integer> mStock = new TableColumn<>("Qty In Stock"); mStock.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
        TableColumn<Medicine, Double>  mPrice = new TableColumn<>("Price ($)");    mPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<Medicine, String>  mExp   = new TableColumn<>("Expiry Date");  mExp.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        medTable.getColumns().addAll(mId, mName, mGen, mStock, mPrice, mExp);

        Button editMedBtn = new Button("Edit Stock / Price"); editMedBtn.getStyleClass().add("btn-secondary");
        mLayout.getChildren().addAll(mBar, medTable, editMedBtn);
        mTab.setContent(mLayout);

        tabPane.getTabs().addAll(qTab, mTab);
        container.getChildren().add(tabPane);

        try {
            rxTable.setItems(FXCollections.observableArrayList(pharmacyService.getPendingPrescriptions()));
            medTable.setItems(FXCollections.observableArrayList(pharmacyService.getAllMedicines()));

            rxTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
                if (nv != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("PRESCRIPTION ID: #Rx-%d | CLINICAL CASE: #MR-%d\n", nv.getId(), nv.getRecordId()));
                    sb.append("MEDICINE PRODUCTS TO DISPENSE:\n");
                    for (PrescriptionItem item : nv.getItems()) {
                        sb.append(String.format("  - ID #%d | Dosage: %s | Frequency: %s | Quantity Required: %d\n",
                            item.getMedicineId(), item.getDosage(), item.getFrequency(), item.getQuantity()));
                    }
                    rxDetailsText.setText(sb.toString());
                }
            });

            searchMed.textProperty().addListener((o, ov, nv) -> {
                try { medTable.setItems(FXCollections.observableArrayList(pharmacyService.searchMedicines(nv)));
                } catch (SQLException ex) { ex.printStackTrace(); }
            });
        } catch (SQLException ex) { ex.printStackTrace(); }

        dispenseBtn.setOnAction(e -> {
            Prescription sel = rxTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "Select Order", "Select a pending prescription order to fulfill."); return; }
            try {
                pharmacyService.dispensePrescription(sel.getId());
                rxTable.setItems(FXCollections.observableArrayList(pharmacyService.getPendingPrescriptions()));
                medTable.setItems(FXCollections.observableArrayList(pharmacyService.getAllMedicines()));
                rxDetailsText.setText("Prescription dispensed successfully. Pharmacy stock inventory levels updated.");
                AlertHelper.showInfo("Dispensed", "Order Filled Successfully", "Patient prescription was successfully marked filled.");
            } catch (Exception ex) {
                AlertHelper.showError("Fulfillment Failure", "Dispense transaction rejected.", ex.getMessage());
            }
        });

        addMedBtn.setOnAction(e -> showAddMedicineDialog(() -> {
            try { medTable.setItems(FXCollections.observableArrayList(pharmacyService.getAllMedicines()));
            } catch (SQLException ex) { ex.printStackTrace(); }
        }));

        editMedBtn.setOnAction(e -> {
            Medicine sel = medTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "No Selection", "Please select a medicine product."); return; }
            showEditMedicineDialog(sel, () -> {
                try { medTable.setItems(FXCollections.observableArrayList(pharmacyService.getAllMedicines()));
                } catch (SQLException ex) { ex.printStackTrace(); }
            });
        });
    }

    private void showAddMedicineDialog(Runnable callback) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(400);

        Label header = new Label("Add New Pharmaceutical Product");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        TextField nameIn      = new TextField();
        TextField genIn       = new TextField();
        TextField qtyIn       = new TextField();
        TextField thresholdIn = new TextField("10");
        TextField priceIn     = new TextField();
        DatePicker expIn      = new DatePicker();

        addRow(grid, "Medicine Name:",    nameIn,      0);
        addRow(grid, "Generic Name:",     genIn,       1);
        addRow(grid, "Stock Quantity:",   qtyIn,       2);
        addRow(grid, "Low Stock Alert:",  thresholdIn, 3);
        addRow(grid, "Unit Price ($):",   priceIn,     4);
        addRow(grid, "Expiry Date:",      expIn,       5);

        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Close"); cancel.getStyleClass().add("btn-secondary");
        Button add    = new Button("Save Product"); add.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, add);

        card.getChildren().addAll(header, grid, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        add.setOnAction(ex -> {
            try {
                Medicine m = new Medicine();
                m.setName(nameIn.getText()); m.setGenericName(genIn.getText());
                m.setStockQty(Integer.parseInt(qtyIn.getText()));
                m.setLowStockThreshold(Integer.parseInt(thresholdIn.getText()));
                m.setPrice(Double.parseDouble(priceIn.getText())); m.setExpiryDate(expIn.getValue());
                pharmacyService.addMedicine(m);
                AlertHelper.showInfo("Added", "Product registered", m.getName() + " added successfully.");
                contentArea.getChildren().remove(overlay);
                callback.run();
            } catch (Exception err) {
                AlertHelper.showError("Validation Failure", "Could not save medicine profile.", err.getMessage());
            }
        });
    }

    private void showEditMedicineDialog(Medicine m, Runnable callback) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(400);

        Label header = new Label("Modify Pharmaceutical Stock Profile");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        TextField nameIn  = new TextField(m.getName()); nameIn.setEditable(false);
        TextField qtyIn   = new TextField(String.valueOf(m.getStockQty()));
        TextField priceIn = new TextField(String.valueOf(m.getPrice()));

        addRow(grid, "Medicine Product Name:", nameIn,  0);
        addRow(grid, "Update Quantity:",       qtyIn,   1);
        addRow(grid, "Update Price ($):",      priceIn, 2);

        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Close"); cancel.getStyleClass().add("btn-secondary");
        Button save   = new Button("Save Modifications"); save.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, grid, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        save.setOnAction(ex -> {
            try {
                m.setStockQty(Integer.parseInt(qtyIn.getText()));
                m.setPrice(Double.parseDouble(priceIn.getText()));
                pharmacyService.updateMedicine(m);
                AlertHelper.showInfo("Saved", "Product updated", "Modifications saved.");
                contentArea.getChildren().remove(overlay);
                callback.run();
            } catch (Exception err) {
                AlertHelper.showError("Validation Failure", "Could not save changes.", err.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 7: LAB
    // ══════════════════════════════════════════════════════════════════════════

    private void buildLabView(VBox container) {
        Label title = new Label("Laboratory & Diagnostic Test Services");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Review clinical ordered tests, input findings, flag abnormalities, and complete procedures.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox layout = new HBox(20);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Left: Queue
        VBox queueCard = new VBox(12);
        queueCard.getStyleClass().add("card");
        HBox.setHgrow(queueCard, Priority.ALWAYS);
        Label queueHeader = new Label("Laboratory Requests Orders Queue");
        queueHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        TableView<LabRequest> labTable = new TableView<>(); labTable.setPrefHeight(450);
        TableColumn<LabRequest, Integer> lId     = new TableColumn<>("ID");           lId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<LabRequest, String>  lPat    = new TableColumn<>("Patient ID");   lPat.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        TableColumn<LabRequest, String>  lName   = new TableColumn<>("Test Ordered"); lName.setCellValueFactory(new PropertyValueFactory<>("testName"));
        TableColumn<LabRequest, String>  lDate   = new TableColumn<>("Ordered Date"); lDate.setCellValueFactory(new PropertyValueFactory<>("requestedDate"));
        TableColumn<LabRequest, String>  lStatus = new TableColumn<>("Status");       lStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        labTable.getColumns().addAll(lId, lPat, lName, lDate, lStatus);
        queueCard.getChildren().addAll(queueHeader, labTable);

        // Right: Input Form
        VBox formCard = new VBox(15);
        formCard.getStyleClass().add("card");
        formCard.setMinWidth(380);
        Label formHeader = new Label("Input Lab Findings & Upload Reports");
        formHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-info;");

        GridPane grid = buildGrid();
        TextArea resultsIn = new TextArea(); resultsIn.setPrefRowCount(4); resultsIn.setPromptText("Input descriptive biological findings...");
        CheckBox abnormalCheck = new CheckBox("Flag Abnormal Results"); abnormalCheck.getStyleClass().add("check-box");
        TextField fileIn = new TextField(); fileIn.setPromptText("Local findings report path link...");

        addRow(grid, "Test Findings Report:", resultsIn,     0);
        addRow(grid, "Clinical Flags:",       abnormalCheck, 1);
        addRow(grid, "Report Attach File:",   fileIn,        2);

        Button submitBtn = new Button("Submit Diagnostic Results");
        submitBtn.getStyleClass().add("btn-primary"); submitBtn.setMaxWidth(Double.MAX_VALUE);
        formCard.getChildren().addAll(formHeader, grid, submitBtn);

        layout.getChildren().addAll(queueCard, formCard);
        container.getChildren().add(layout);

        try { labTable.setItems(FXCollections.observableArrayList(labService.getPendingRequests()));
        } catch (SQLException ex) { ex.printStackTrace(); }

        labTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) { resultsIn.clear(); abnormalCheck.setSelected(false); fileIn.clear(); }
        });

        submitBtn.setOnAction(e -> {
            LabRequest sel = labTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "Select Lab Request", "Select a lab request row from the queue to process."); return; }
            try {
                labService.recordResults(sel.getId(), resultsIn.getText(), abnormalCheck.isSelected(), fileIn.getText());
                labTable.setItems(FXCollections.observableArrayList(labService.getPendingRequests()));
                resultsIn.clear(); abnormalCheck.setSelected(false); fileIn.clear();
                AlertHelper.showInfo("Success", "Diagnostic Results Logged", "Lab test marked completed. Billing ledger notified.");
            } catch (Exception ex) {
                AlertHelper.showError("Error", "Diagnostic save failed", ex.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 8: WARDS
    // ══════════════════════════════════════════════════════════════════════════

    private void buildWardsView(VBox container) {
        Label title = new Label("Interactive Ward Bed Status Grid");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Admit patients to specialized wards, toggle room occupancies, and manage beds schedules interactively.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox selectorRow = new HBox(15); selectorRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Ward> wardCombo = new ComboBox<>(); wardCombo.setMaxWidth(300);
        Label availableLabel = new Label("Green: AVAILABLE  |  Blue: OCCUPIED  |  Orange: MAINTENANCE");
        availableLabel.getStyleClass().add("label-muted");
        selectorRow.getChildren().addAll(new Label("Select Clinical Ward:"), wardCombo, availableLabel);
        container.getChildren().add(selectorRow);

        FlowPane bedGrid = new FlowPane();
        bedGrid.setHgap(20); bedGrid.setVgap(20); bedGrid.setPadding(new Insets(20));
        bedGrid.getStyleClass().add("card"); bedGrid.setMinWidth(600);
        container.getChildren().add(bedGrid);

        try {
            List<Ward> wardsList = wardService.getAllWards();
            wardCombo.setItems(FXCollections.observableArrayList(wardsList));
            wardCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Ward item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + item.getType() + ")");
                }
            });
            wardCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Ward item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
            if (!wardsList.isEmpty()) wardCombo.setValue(wardsList.get(0));
        } catch (SQLException ex) { ex.printStackTrace(); }

        Runnable refreshBeds = () -> {
            bedGrid.getChildren().clear();
            Ward sel = wardCombo.getValue();
            if (sel == null) return;
            try {
                List<Bed> beds = wardService.getBedsInWard(sel.getId());
                for (Bed b : beds) {
                    Button bedBtn = new Button(b.getBedNumber());
                    bedBtn.setPrefSize(100, 100);
                    bedBtn.getStyleClass().add("btn-secondary");
                    bedBtn.setCursor(javafx.scene.Cursor.HAND);
                    if (b.getStatus() == BedStatus.AVAILABLE) {
                        bedBtn.setStyle("-fx-background-color: rgba(72,187,120,0.15); -fx-text-fill: #48bb78; -fx-border-color: #48bb78; -fx-font-weight: bold; -fx-font-size: 14;");
                    } else if (b.getStatus() == BedStatus.OCCUPIED) {
                        bedBtn.setStyle("-fx-background-color: rgba(66,153,225,0.15); -fx-text-fill: #4299e1; -fx-border-color: #4299e1; -fx-font-weight: bold; -fx-font-size: 14;");
                    } else {
                        bedBtn.setStyle("-fx-background-color: rgba(237,137,54,0.15); -fx-text-fill: #ed8936; -fx-border-color: #ed8936; -fx-font-weight: bold; -fx-font-size: 14;");
                    }
                    bedBtn.setOnAction(e -> {
                        if (b.getStatus() == BedStatus.AVAILABLE) showAdmitPatientDialog(b, r -> r.run());
                        else if (b.getStatus() == BedStatus.OCCUPIED) showDischargePatientDialog(b, r -> r.run());
                        else AlertHelper.showWarning("Maintenance Alert", "Bed Under Service", "This bed is undergoing maintenance service operations.");
                    });
                    bedGrid.getChildren().add(bedBtn);
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };

        wardCombo.valueProperty().addListener((o, ov, nv) -> refreshBeds.run());
        refreshBeds.run();
    }

    private void showAdmitPatientDialog(Bed b, java.util.function.Consumer<Runnable> onComplete) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox card = new VBox(15);
        card.getStyleClass().add("dialog-card");
        card.setMaxWidth(400);

        Label header = new Label("Admit Patient to Bed " + b.getBedNumber());
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        ComboBox<Patient> patCombo = new ComboBox<>();
        TextField rateIn = new TextField("150.0");

        addRow(grid, "Select Patient:",       patCombo, 0);
        addRow(grid, "Daily Ward Rate ($):",  rateIn,   1);

        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Close"); cancel.getStyleClass().add("btn-secondary");
        Button admit  = new Button("Admit Patient"); admit.getStyleClass().add("btn-primary");
        btnBox.getChildren().addAll(cancel, admit);

        card.getChildren().addAll(header, grid, btnBox);
        overlay.getChildren().add(card);
        contentArea.getChildren().add(overlay);

        try {
            patCombo.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
            patCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
                }
            });
            patCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
        } catch (SQLException ex) { ex.printStackTrace(); }

        cancel.setOnAction(ex -> contentArea.getChildren().remove(overlay));
        admit.setOnAction(ex -> {
            try {
                Patient pat = patCombo.getValue();
                if (pat == null) throw new IllegalArgumentException("Please select a patient.");
                double rate = Double.parseDouble(rateIn.getText());
                wardService.admitPatient(pat.getId(), b.getId(), rate);
                AlertHelper.showInfo("Admitted", "Patient Admitted Successfully",
                    "Patient " + pat.getName() + " is now registered in bed " + b.getBedNumber());
                contentArea.getChildren().remove(overlay);
                navigateTo("wards");
            } catch (Exception err) {
                AlertHelper.showError("Admission Failure", "Failed to complete bed admission.", err.getMessage());
            }
        });
    }

    private void showDischargePatientDialog(Bed b, java.util.function.Consumer<Runnable> onComplete) {
        try {
            List<Admission> active = wardService.getAllActiveAdmissions();
            Admission currentAdm = null;
            System.out.println("Clicked bed: " + b.getBedNumber() + " ID=" + b.getId());

            for (Admission adm : active) {
                if (adm.getBedId() == b.getId()) { 
                    currentAdm = adm; 
                    break; 
                }
            }
            
            // ORPHANED BED PROTECTION SYSTEM
            if (currentAdm == null) {
                boolean resetTarget = AlertHelper.showConfirmation(
                    "Data Sync Discrepancy", 
                    "Orphaned Occupancy Detected", 
                    "Bed " + b.getBedNumber() + " is marked OCCUPIED, but no active check-in logs exist.\n\n" +
                    "Would you like to force-reset this bed's state back to AVAILABLE?"
                );
                
                if (resetTarget) {
                    try (java.sql.Connection conn = com.hospital.dao.DBConnection.getConnection();
                         java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE beds SET status = 'AVAILABLE' WHERE id = ?;")) {
                        ps.setInt(1, b.getId());
                        ps.executeUpdate();
                        
                        AlertHelper.showInfo("System Repaired", "Bed Status Reset", 
                            "Bed " + b.getBedNumber() + " is now available for fresh admissions.");
                        navigateTo("wards");
                    } catch (Exception dbErr) {
                        AlertHelper.showError("Repair Failure", "Could not clear bed state lock.", dbErr.getMessage());
                    }
                }
                return;
            }
            
            final Admission finalAdm = currentAdm;
            if (AlertHelper.showConfirmation("Discharge Patient",
                    "Proceed with Ward Discharge for Patient: " + finalAdm.getPatientId() + "?",
                    "This will mark bed as available, close check-in date/time, and auto-compile the overall ward invoice bill.")) {
                wardService.dischargePatient(finalAdm.getPatientId());
                billingService.generateBill(finalAdm.getPatientId(), null, finalAdm.getId());
                AlertHelper.showInfo("Discharged", "Patient Discharged",
                    "Bed successfully cleared. Ward stay invoice compiled.");
                navigateTo("wards");
            }
        } catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
    }
    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 9: EMERGENCY
    // ══════════════════════════════════════════════════════════════════════════

    private void buildEmergencyView(VBox container) {
        Label title = new Label("Emergency Room & Prioritized Triage Waiting List");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Prioritize emergency cases based on triage level, automatically ordering queue positions.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox layout = new HBox(20);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Left: Register Form
        VBox formCard = new VBox(15);
        formCard.getStyleClass().add("card");
        formCard.setMinWidth(380);
        Label formHeader = new Label("Register ER Case Triage");
        formHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        ComboBox<Patient>     patCombo    = new ComboBox<>(); patCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<TriageLevel> triageCombo = new ComboBox<>(FXCollections.observableArrayList(TriageLevel.values()));
        triageCombo.setMaxWidth(Double.MAX_VALUE);

        addRow(grid, "Select Patient:", patCombo,    0);
        addRow(grid, "Triage Level:",   triageCombo, 1);

        Button registerBtn = new Button("Register and Seat");
        registerBtn.getStyleClass().add("btn-danger"); registerBtn.setMaxWidth(Double.MAX_VALUE);
        formCard.getChildren().addAll(formHeader, grid, registerBtn);

        // Right: Queue
        VBox queueCard = new VBox(12);
        queueCard.getStyleClass().add("card");
        HBox.setHgrow(queueCard, Priority.ALWAYS);
        Label queueHeader = new Label("Sorted Triage Waiting List");
        queueHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-danger;");

        TableView<EmergencyPatient> erTable = new TableView<>(); erTable.setPrefHeight(400);
        TableColumn<EmergencyPatient, Integer> qId       = new TableColumn<>("ID");             qId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<EmergencyPatient, String>  qPat      = new TableColumn<>("Patient Name");   qPat.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        TableColumn<EmergencyPatient, String>  qTriage   = new TableColumn<>("Triage Level");   qTriage.setCellValueFactory(new PropertyValueFactory<>("triageLevel"));
        TableColumn<EmergencyPatient, Integer> qPriority = new TableColumn<>("Priority Code");  qPriority.setCellValueFactory(new PropertyValueFactory<>("queuePriority"));
        TableColumn<EmergencyPatient, Integer> qBed      = new TableColumn<>("Assigned Bed ID"); qBed.setCellValueFactory(new PropertyValueFactory<>("bedId"));
        TableColumn<EmergencyPatient, String>  qStatus   = new TableColumn<>("Status");         qStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        erTable.getColumns().addAll(qId, qPat, qTriage, qPriority, qBed, qStatus);

        HBox btnBox = new HBox(12);
        Button resolveBtn = new Button("Discharge / Resolve"); resolveBtn.getStyleClass().add("btn-secondary");
        btnBox.getChildren().add(resolveBtn);
        queueCard.getChildren().addAll(queueHeader, erTable, btnBox);

        layout.getChildren().addAll(formCard, queueCard);
        container.getChildren().add(layout);

        try {
            patCombo.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
            patCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
                }
            });
            patCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
            erTable.setItems(FXCollections.observableArrayList(emergencyService.getActiveWaitingQueue()));
        } catch (SQLException ex) { ex.printStackTrace(); }

        registerBtn.setOnAction(e -> {
            try {
                Patient p      = patCombo.getValue();
                TriageLevel t  = triageCombo.getValue();
                if (p == null || t == null) throw new IllegalArgumentException("Demographic Patient and triage priority is required.");
                EmergencyPatient ep = new EmergencyPatient();
                ep.setPatientId(p.getId()); ep.setTriageLevel(t);
                emergencyService.registerEmergency(ep);
                erTable.setItems(FXCollections.observableArrayList(emergencyService.getActiveWaitingQueue()));
                AlertHelper.showInfo("Emergency Registered", "Triage Queue Updated",
                    "Patient is registered and sorted in queue. Instant Bed Assignment engine assigned an open bed if available.");
            } catch (Exception ex) {
                AlertHelper.showError("Registration Error", "Emergency Triage registration failed.", ex.getMessage());
            }
        });

        resolveBtn.setOnAction(e -> {
            EmergencyPatient sel = erTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "Select Patient", "Select patient from queue list."); return; }
            try {
                emergencyService.resolveEmergency(sel.getId(), "DISCHARGED");
                erTable.setItems(FXCollections.observableArrayList(emergencyService.getActiveWaitingQueue()));
                AlertHelper.showInfo("Resolved", "Case Closed", "Emergency case is successfully resolved and released.");
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 10: BILLING
    // ══════════════════════════════════════════════════════════════════════════

    private void buildBillingView(VBox container) {
        Label title = new Label("Billing & Financial Ledger");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Review auto-generated consult invoices, accept payments, and review revenue status catalogs.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);
 
        HBox layout = new HBox(20);
        VBox.setVgrow(layout, Priority.ALWAYS);
 
        // Left: Invoice Table
        VBox ledgerCard = new VBox(12);
        ledgerCard.getStyleClass().add("card");
        HBox.setHgrow(ledgerCard, Priority.ALWAYS);
        Label ledgerHeader = new Label("Patients Financial Ledger Statements");
        ledgerHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-accent;");
 
        TableView<Bill> billTable = new TableView<>(); billTable.setPrefHeight(450);
        TableColumn<Bill, Integer> bId     = new TableColumn<>("Invoice ID");        bId.setCellValueFactory(new PropertyValueFactory<>("id"));
        // FIX: show patient name (from JOIN) instead of raw ID
        TableColumn<Bill, String>  bName   = new TableColumn<>("Patient Name");      bName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        TableColumn<Bill, String>  bPat    = new TableColumn<>("Patient ID");        bPat.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        TableColumn<Bill, Double>  bTotal  = new TableColumn<>("Total Invoice ($)"); bTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        TableColumn<Bill, Double>  bPaid   = new TableColumn<>("Paid Total ($)");    bPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        TableColumn<Bill, Double>  bDue    = new TableColumn<>("Outstanding ($)");
        bDue.setCellValueFactory(cd -> {
            double due = cd.getValue().getTotalAmount() - cd.getValue().getPaidAmount();
            return new javafx.beans.property.SimpleObjectProperty<>(Math.max(0.0, due));
        });
     // CHANGE THIS LINE: Make sure it says com.hospital.enums.PaymentStatus, NOT String
        TableColumn<Bill, com.hospital.enums.PaymentStatus> bStatus = new TableColumn<>("Status");
        bStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Then your pasted cell factory will match perfectly:
        bStatus.setCellFactory(col -> new TableCell<Bill, com.hospital.enums.PaymentStatus>() {
            @Override protected void updateItem(com.hospital.enums.PaymentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                
                String label = item.name();
                setText(label);
                switch (label) {
                    case "PAID"    -> setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold;");
                    case "PARTIAL" -> setStyle("-fx-text-fill: #ed8936; -fx-font-weight: bold;");
                    default        -> setStyle("-fx-text-fill: #fc8181; -fx-font-weight: bold;");
                }
            }
        });
        billTable.getColumns().addAll(bId, bName, bPat, bTotal, bPaid, bDue, bStatus);
        ledgerCard.getChildren().addAll(ledgerHeader, billTable);
 
        // Right: Payment Panel
        VBox payCard = new VBox(15);
        payCard.getStyleClass().add("card");
        payCard.setMinWidth(380);
        Label payHeader = new Label("Process Ledger Payment Receipt");
        payHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-info;");
 
        TextArea invoiceText = new TextArea("Select an invoice statement from the ledger sheet.");
        invoiceText.setEditable(false); invoiceText.setPrefRowCount(9);
 
        // FIX: label shows outstanding amount so receptionist knows the exact max
        Label outstandingLbl = new Label();
        outstandingLbl.setStyle("-fx-text-fill: -hms-warning; -fx-font-size: 12;");
 
        GridPane grid = buildGrid();
        TextField payIn = new TextField(); payIn.setPromptText("Enter payment amount...");
        ComboBox<String> methodCombo = new ComboBox<>(FXCollections.observableArrayList("CASH", "CARD", "INSURANCE", "UPI"));
        methodCombo.setValue("CASH");
 
        addRow(grid, "Accept Payment ($):", payIn,       0);
        addRow(grid, "Payment Method:",     methodCombo, 1);
 
        Button payBtn   = new Button("Finalize Payment");      payBtn.getStyleClass().add("btn-primary");   payBtn.setMaxWidth(Double.MAX_VALUE);
        Button printBtn = new Button("Print Invoiced Receipt"); printBtn.getStyleClass().add("btn-secondary"); printBtn.setMaxWidth(Double.MAX_VALUE);
        payCard.getChildren().addAll(payHeader, invoiceText, outstandingLbl, grid, payBtn, printBtn);
 
        layout.getChildren().addAll(ledgerCard, payCard);
        container.getChildren().add(layout);
 
        try { billTable.setItems(FXCollections.observableArrayList(billingService.getAllBills()));
        } catch (SQLException ex) { ex.printStackTrace(); }
 
        billTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv == null) { outstandingLbl.setText(""); return; }
 
            double outstanding = Math.max(0.0, nv.getTotalAmount() - nv.getPaidAmount());
 
            // FIX: show patient name and outstanding hint
            outstandingLbl.setText(String.format("Outstanding balance: $%.2f", outstanding));
 
            // Disable pay button and dim it if invoice is already fully paid
            boolean alreadyPaid = nv.getPaymentStatus() == com.hospital.enums.PaymentStatus.PAID;
            payBtn.setDisable(alreadyPaid);
            payBtn.setOpacity(alreadyPaid ? 0.45 : 1.0);
            if (alreadyPaid) {
                outstandingLbl.setText("This invoice is fully paid. No payment required.");
                outstandingLbl.setStyle("-fx-text-fill: #48bb78; -fx-font-size: 12;");
            } else {
                outstandingLbl.setStyle("-fx-text-fill: -hms-warning; -fx-font-size: 12;");
            }
 
            StringBuilder sb = new StringBuilder();
            sb.append("=========================================\n");
            sb.append("         CITY MEDICAL HOSPITAL          \n");
            sb.append("         FINANCIAL INVOICE STATEMENT     \n");
            sb.append("=========================================\n");
            sb.append(String.format("INVOICE ID:      #INV-%d\n",   nv.getId()));
            // FIX: use patientName from JOIN, fall back to ID if somehow null
            String displayName = (nv.getPatientName() != null && !nv.getPatientName().isBlank())
                                 ? nv.getPatientName() : nv.getPatientId();
            sb.append(String.format("PATIENT NAME:    %s\n",        displayName));
            sb.append(String.format("PATIENT ID:      %s\n",        nv.getPatientId()));
            sb.append(String.format("DATE COMPILED:   %s\n",
                nv.getBillingDate() != null
                ? nv.getBillingDate().toString().replace("T", "  ") : "N/A"));
            sb.append("-----------------------------------------\n");
            if (nv.getAppointmentId() != null) sb.append("DOCTOR CONSULTING FEES:    Included\n");
            if (nv.getAdmissionId()   != null) sb.append("WARD ROOM DAILY RATE STAY: Included\n");
            sb.append("-----------------------------------------\n");
            sb.append(String.format("TOTAL GROSS AMOUNT:      $%8.2f\n", nv.getTotalAmount()));
            sb.append(String.format("TOTAL PAID:              $%8.2f\n", nv.getPaidAmount()));
            sb.append(String.format("OUTSTANDING BALANCE:     $%8.2f\n", outstanding));
            sb.append("-----------------------------------------\n");
            sb.append(String.format("PAYMENT STATUS:  %s\n",         nv.getPaymentStatus().name()));
            sb.append(String.format("PAYMENT METHOD:  %s\n",
                nv.getPaymentMethod() != null ? nv.getPaymentMethod() : "N/A"));
            sb.append("=========================================\n");
            invoiceText.setText(sb.toString());
        });
 
        payBtn.setOnAction(e -> {
            Bill sel = billTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                AlertHelper.showWarning("Warning", "Select Invoice", "Select an invoice to process payment.");
                return;
            }
            // FIX: UI-level guard — should never reach here for PAID bills (button is disabled),
            //      but defensive check remains in case of edge cases.
            if (sel.getPaymentStatus() == com.hospital.enums.PaymentStatus.PAID) {
                AlertHelper.showWarning("Already Paid",
                    "Invoice #" + sel.getId() + " is fully settled.",
                    "This invoice has already been paid in full. No further payment is accepted.");
                return;
            }
            try {
                String amtText = payIn.getText().trim();
                if (amtText.isEmpty()) throw new IllegalArgumentException("Please enter a payment amount.");
                double amt = Double.parseDouble(amtText);
                if (amt <= 0) throw new IllegalArgumentException("Payment amount must be greater than zero.");
 
                billingService.processPayment(sel.getId(), amt, methodCombo.getValue());
 
                // Reload table and re-select the same bill to refresh the invoice panel
                billTable.setItems(FXCollections.observableArrayList(billingService.getAllBills()));
                // Re-select by matching id since the list reference changed
                for (Bill b : billTable.getItems()) {
                    if (b.getId() == sel.getId()) { billTable.getSelectionModel().select(b); break; }
                }
                payIn.clear();
                AlertHelper.showInfo("Payment Logged", "Receipt Processed",
                    String.format("$%.2f recorded against Invoice #%d.", amt, sel.getId()));
            } catch (Exception ex) {
                AlertHelper.showError("Payment Rejected", "Transaction failed.", ex.getMessage());
            }
        });
 
        printBtn.setOnAction(e -> {
            Bill sel = billTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "Select Invoice", "Select an invoice to print."); return; }
            File file = new File("receipt_INV_" + sel.getId() + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(invoiceText.getText());
                AlertHelper.showInfo("Print Job Sent", "Receipt Saved Successfully",
                    "Printed file saved to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                AlertHelper.showError("Print Error", "Failed to print receipt file.", ex.getMessage());
            }
        });
    }


    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 11: HR
    // ══════════════════════════════════════════════════════════════════════════

    private void buildHRView(VBox container) {
        Label title = new Label("Staff Shifts Roster & Scheduler");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Schedule staff shift rosters, assign roles, and log operational safety schedules.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox layout = new HBox(20);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Left: Form
        VBox formCard = new VBox(15);
        formCard.getStyleClass().add("card");
        formCard.setMinWidth(380);
        Label formHeader = new Label("Schedule Shift Roster");
        formHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        GridPane grid = buildGrid();
        TextField staffIdIn = new TextField(); staffIdIn.setPromptText("Enter staff User ID...");
        DatePicker shiftDate = new DatePicker(LocalDate.now());
        ComboBox<String> startCombo = new ComboBox<>(FXCollections.observableArrayList("08:00", "16:00", "20:00"));
        ComboBox<String> endCombo   = new ComboBox<>(FXCollections.observableArrayList("16:00", "00:00", "08:00"));
        ComboBox<String> roleCombo  = new ComboBox<>(FXCollections.observableArrayList("DOCTOR", "NURSE", "RECEPTIONIST", "PHARMACIST", "LAB_TECH"));

        addRow(grid, "Staff User ID:",       staffIdIn, 0);
        addRow(grid, "Shift Date:",          shiftDate, 1);
        addRow(grid, "Start Time:",          startCombo, 2);
        addRow(grid, "End Time:",            endCombo,   3);
        addRow(grid, "Shift Role Assigned:", roleCombo,  4);

        Button saveBtn = new Button("Publish Shift Roster");
        saveBtn.getStyleClass().add("btn-primary"); saveBtn.setMaxWidth(Double.MAX_VALUE);
        formCard.getChildren().addAll(formHeader, grid, saveBtn);

        // Right: Roster Table
        VBox rosterCard = new VBox(12);
        rosterCard.getStyleClass().add("card");
        HBox.setHgrow(rosterCard, Priority.ALWAYS);
        Label rosterHeader = new Label("Active Staff Shifts Directory");
        rosterHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-info;");

        TableView<StaffShift> shiftTable = new TableView<>(); shiftTable.setPrefHeight(400);
        TableColumn<StaffShift, Integer> sId    = new TableColumn<>("Shift ID");     sId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<StaffShift, Integer> sStaff = new TableColumn<>("Staff User ID"); sStaff.setCellValueFactory(new PropertyValueFactory<>("userId"));
        TableColumn<StaffShift, String>  sStart = new TableColumn<>("Start");        sStart.setCellValueFactory(new PropertyValueFactory<>("shiftStart"));
        TableColumn<StaffShift, String>  sEnd   = new TableColumn<>("End");          sEnd.setCellValueFactory(new PropertyValueFactory<>("shiftEnd"));
        TableColumn<StaffShift, String>  sRole  = new TableColumn<>("Role");         sRole.setCellValueFactory(new PropertyValueFactory<>("roleAssigned"));
        shiftTable.getColumns().addAll(sId, sStaff, sStart, sEnd, sRole);

        HBox btnBox = new HBox(12);
        Button deleteBtn = new Button("Remove Shift"); deleteBtn.getStyleClass().add("btn-danger");
        btnBox.getChildren().add(deleteBtn);
        rosterCard.getChildren().addAll(rosterHeader, shiftTable, btnBox);

        layout.getChildren().addAll(formCard, rosterCard);
        container.getChildren().add(layout);

        try { shiftTable.setItems(FXCollections.observableArrayList(hrService.getAllShifts()));
        } catch (SQLException ex) { ex.printStackTrace(); }

        saveBtn.setOnAction(e -> {
            try {
                int u           = Integer.parseInt(staffIdIn.getText());
                LocalDate d     = shiftDate.getValue();
                String st       = startCombo.getValue();
                String et       = endCombo.getValue();
                String r        = roleCombo.getValue();
                if (d == null || st == null || et == null || r == null)
                    throw new IllegalArgumentException("Compile shift roster fields fully.");
                LocalDateTime start = LocalDateTime.of(d, LocalTime.parse(st));
                LocalDateTime end   = LocalDateTime.of(d, LocalTime.parse(et));
                if (end.isBefore(start) || end.isEqual(start)) end = end.plusDays(1);
                hrService.assignShift(u, start, end, r);
                shiftTable.setItems(FXCollections.observableArrayList(hrService.getAllShifts()));
                AlertHelper.showInfo("Rostered", "Shift Published", "Roster successfully scheduled and logged.");
            } catch (Exception ex) {
                AlertHelper.showError("Scheduling Rejection", "Roster conflict or invalid staff key ID.", ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            StaffShift sel = shiftTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "Select Shift", "Select a shift row to remove."); return; }
            try {
                hrService.removeShift(sel.getId());
                shiftTable.setItems(FXCollections.observableArrayList(hrService.getAllShifts()));
                AlertHelper.showInfo("Removed", "Shift Cancelled", "Roster shift removed successfully.");
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 12: INVENTORY
    // ══════════════════════════════════════════════════════════════════════════

    private void buildInventoryView(VBox container) {
        Label title = new Label("Supplies & Inventory Control Panel");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Track consumables, gloves, syringes, and critical medical equipments restocking orders.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox layout = new HBox(20);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Left: Table
        VBox listCard = new VBox(12);
        listCard.getStyleClass().add("card");
        HBox.setHgrow(listCard, Priority.ALWAYS);
        Label listHeader = new Label("Clinical Supplies Stock Sheets");
        listHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-accent;");

        TableView<InventoryItem> invTable = new TableView<>(); invTable.setPrefHeight(450);
        TableColumn<InventoryItem, Integer> iId    = new TableColumn<>("ID");             iId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<InventoryItem, String>  iName  = new TableColumn<>("Supply Item Name"); iName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<InventoryItem, String>  iType  = new TableColumn<>("Type");            iType.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        TableColumn<InventoryItem, Integer> iStock = new TableColumn<>("Stock Qty");       iStock.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
        TableColumn<InventoryItem, String>  iUnit  = new TableColumn<>("Unit");            iUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        invTable.getColumns().addAll(iId, iName, iType, iStock, iUnit);
        listCard.getChildren().addAll(listHeader, invTable);

        // Right: Restock Panel
        VBox restockCard = new VBox(15);
        restockCard.getStyleClass().add("card");
        restockCard.setMinWidth(380);
        Label restockHeader = new Label("Process Restock Purchase Order");
        restockHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: -hms-info;");

        GridPane grid = buildGrid();
        TextField qtyIn      = new TextField(); qtyIn.setPromptText("Restock quantity...");
        TextField costIn     = new TextField(); costIn.setPromptText("Cost of order ($)...");
        TextField supplierIn = new TextField(); supplierIn.setPromptText("Supplier corporate name...");

        addRow(grid, "Order Restock Qty:",        qtyIn,      0);
        addRow(grid, "Total Invoice Cost ($):",   costIn,     1);
        addRow(grid, "Log Supplier Corporate:",   supplierIn, 2);

        Button restockBtn = new Button("Submit Purchase Order");
        restockBtn.getStyleClass().add("btn-primary"); restockBtn.setMaxWidth(Double.MAX_VALUE);
        restockCard.getChildren().addAll(restockHeader, grid, restockBtn);

        layout.getChildren().addAll(listCard, restockCard);
        container.getChildren().add(layout);

        try { invTable.setItems(FXCollections.observableArrayList(inventoryService.getAllItems()));
        } catch (SQLException ex) { ex.printStackTrace(); }

        restockBtn.setOnAction(e -> {
            InventoryItem sel = invTable.getSelectionModel().getSelectedItem();
            if (sel == null) { AlertHelper.showWarning("Warning", "Select Product", "Select a supply product row from the stock list sheets."); return; }
            try {
                int q    = Integer.parseInt(qtyIn.getText());
                double c = Double.parseDouble(costIn.getText());
                String s = supplierIn.getText();
                inventoryService.createPurchaseOrder(sel.getId(), q, c, s);
                invTable.setItems(FXCollections.observableArrayList(inventoryService.getAllItems()));
                qtyIn.clear(); costIn.clear(); supplierIn.clear();
                AlertHelper.showInfo("Processed", "Restock Order Secured",
                    "Inventory quantities successfully updated. Restock purchase orders logged.");
            } catch (Exception ex) {
                AlertHelper.showError("Rejection", "Failed to compile purchase order restock.", ex.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODULE 13: REPORTS
    // ══════════════════════════════════════════════════════════════════════════

    private void buildReportsView(VBox container) {
        Label title = new Label("Reports & Analytics Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(
            "Real-time revenue channels audits, check-ins analytics, and one-click data sheets export.");
        subtitle.getStyleClass().add("page-subtitle");
        container.getChildren().addAll(title, subtitle);

        HBox toolbar = new HBox(15); toolbar.setAlignment(Pos.CENTER_LEFT);
        Button exportBtn = new Button("📥 Export Financial Ledger (CSV)");
        exportBtn.getStyleClass().add("btn-primary");
        toolbar.getChildren().add(exportBtn);
        container.getChildren().add(toolbar);

        HBox chartsRow = new HBox(20);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis   = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Revenue Channels Audits ($)");
        barChart.setPrefHeight(450);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Beds Occupancy Distribution");
        pieChart.setPrefHeight(450);
        HBox.setHgrow(pieChart, Priority.ALWAYS);

        chartsRow.getChildren().addAll(barChart, pieChart);
        container.getChildren().add(chartsRow);

        try {
            Map<String, Double> rev = reportsService.getRevenueByPaymentMethod();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue channels");
            for (Map.Entry<String, Double> entry : rev.entrySet())
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            barChart.getData().add(series);

            Map<String, int[]> occ = reportsService.getBedOccupancyByWard();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, int[]> entry : occ.entrySet()) {
                int occupied = entry.getValue()[1];
                pieData.add(new PieChart.Data(entry.getKey() + " (" + occupied + ")", occupied));
            }
            pieChart.setData(pieData);
        } catch (SQLException ex) { ex.printStackTrace(); }

        exportBtn.setOnAction(e -> {
            File file = new File("hms_financial_ledger_report.csv");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Invoice ID,Patient ID,Total Amount ($),Paid Amount ($),Payment Status,Payment Method,Billing Date\n");
                List<Bill> all = billingService.getAllBills();
                for (Bill b : all) {
                    writer.write(String.format("%d,%s,%.2f,%.2f,%s,%s,%s\n",
                        b.getId(), b.getPatientId(), b.getTotalAmount(), b.getPaidAmount(),
                        b.getPaymentStatus().name(), b.getPaymentMethod(), b.getBillingDate().toString()));
                }
                AlertHelper.showInfo("Export Complete", "Data Ledger Downloaded",
                    "CSV sheet successfully saved: " + file.getAbsolutePath());
            } catch (Exception ex) {
                AlertHelper.showError("Export Failure", "Failed to write CSV ledger file.", ex.getMessage());
            }
        });
    }
}