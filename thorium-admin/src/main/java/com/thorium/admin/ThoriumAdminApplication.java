licencingsoftware/thorium-admin/src/main/java/com/thorium/admin/ThoriumAdminApplication.java
```java
package com.thorium.admin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.thorium.sdk.hardware.HardwareFingerprint;
import com.thorium.sdk.license.LicenseTokenEngine;
import com.thorium.sdk.license.LicenseKeyData;
import com.thorium.sdk.license.ValidationResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * ThoriumAdminApplication - JavaFX Desktop Application for license management.
 *
 * This admin app allows developers to:
 * - View dashboard with license statistics
 * - Create and manage products
 * - Generate license keys
 * - View and manage existing licenses
 * - Configure settings
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class ThoriumAdminApplication extends Application {

    // Application title and version
    private static final String APP_TITLE = "Thorium Admin";
    private static final String APP_VERSION = "1.0.0";

    // Main stage
    private Stage primaryStage;

    // Data lists
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<License> licenses = FXCollections.observableArrayList();

    // Sample data
    private int totalLicenses = 0;
    private int activeLicenses = 0;
    private int expiredLicenses = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(APP_TITLE + " v" + APP_VERSION);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        // Initialize sample data
        initializeSampleData();

        // Create main layout
        BorderPane mainLayout = createMainLayout();

        // Set scene
        Scene scene = new Scene(mainLayout, 1000, 700);
        scene.getStylesheets().add(createStylesheet());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeSampleData() {
        // Add sample products
        products.add(new Product("prod_001", "School Timetable Pro", "1.0.0", 25));
        products.add(new Product("prod_002", "Inventory Manager", "2.1.0", 50));
        products.add(new Product("prod_003", "Employee Tracker", "1.5.0", 10));

        // Add sample licenses
        licenses.add(new License("ABCD1234EFGH5678", "prod_001", " Machine1", "12/2026", true));
        licenses.add(new License("QRST5678UVWX9012", "prod_001", " Machine2", "06/2027", true));
        licenses.add(new License("IJKL3456MNOP7890", "prod_002", " Machine3", "03/2026", false));
        licenses.add(new License("PQRS9012YZAB3456", "prod_003", " Machine4", "11/2026", true));

        updateLicenseStats();
    }

    private void updateLicenseStats() {
        totalLicenses = licenses.size();
        activeLicenses = (int) licenses.stream().filter(License::isActive).count();
        expiredLicenses = totalLicenses - activeLicenses;
    }

    private BorderPane createMainLayout() {
        BorderPane layout = new BorderPane();

        // Create menu bar
        MenuBar menuBar = createMenuBar();
        layout.setTop(menuBar);

        // Create sidebar navigation
        VBox sidebar = createSidebar();
        layout.setLeft(sidebar);

        // Create main content area (Dashboard by default)
        TabPane contentArea = createContentArea();
        layout.setCenter(contentArea);

        return layout;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newProduct = new MenuItem("New Product");
        MenuItem exportLicenses = new MenuItem("Export Licenses");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(newProduct, exportLicenses, new SeparatorMenuItem(), exit);

        // Tools menu
        Menu toolsMenu = new Menu("Tools");
        MenuItem generateKey = new MenuItem("Generate License Key");
        MenuItem validateKey = new MenuItem("Validate License Key");
        MenuItem refreshData = new MenuItem("Refresh Data");
        toolsMenu.getItems().addAll(generateKey, validateKey, refreshData);

        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem documentation = new MenuItem("Documentation");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().addAll(documentation, about);

        menuBar.getMenus().addAll(fileMenu, toolsMenu, helpMenu);

        return menuBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(200);

        // App title
        Label appTitle = new Label("THORIUM");
        appTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

        Label appSubtitle = new Label("Admin Panel");
        appSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdc3c7;");

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #34495e;");

        // Navigation buttons
        Button dashboardBtn = createNavButton("Dashboard", "📊");
        Button productsBtn = createNavButton("Products", "📦");
        Button licensesBtn = createNavButton("Licenses", "🔑");
        Button generateBtn = createNavButton("Generate Key", "⚡");
        Button settingsBtn = createNavButton("Settings", "⚙️");

        // Default selected
        dashboardBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: #ecf0f1;");

        sidebar.getChildren().addAll(appTitle, appSubtitle, sep, dashboardBtn, productsBtn, licensesBtn, generateBtn, settingsBtn);

        return sidebar;
    }

    private Button createNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(170);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 10, 12, 10));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-cursor: hand;"));
        return btn;
    }

    private TabPane createContentArea() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.NONE);

        // Dashboard Tab
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setContent(createDashboardView());
        tabPane.getTabs().add(dashboardTab);

        // Products Tab
        Tab productsTab = new Tab("Products");
        productsTab.setContent(createProductsView());
        tabPane.getTabs().add(productsTab);

        // Licenses Tab
        Tab licensesTab = new Tab("Licenses");
        licensesTab.setContent(createLicensesView());
        tabPane.getTabs().add(licensesTab);

        // Generate Key Tab
        Tab generateTab = new Tab("Generate Key");
        generateTab.setContent(createGenerateKeyView());
        tabPane.getTabs().add(generateTab);

        // Settings Tab
        Tab settingsTab = new Tab("Settings");
        settingsTab.setContent(createSettingsView());
        tabPane.getTabs().add(settingsTab);

        return tabPane;
    }

    private VBox createDashboardView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        // Title
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Stats cards
        HBox statsRow = new HBox(20);

        // Total Licenses Card
        VBox totalCard = createStatCard("Total Licenses", String.valueOf(totalLicenses), "#3498db");
        // Active Licenses Card
        VBox activeCard = createStatCard("Active", String.valueOf(activeLicenses), "#27ae60");
        // Expired Licenses Card
        VBox expiredCard = createStatCard("Expired", String.valueOf(expiredLicenses), "#e74c3c");
        // Products Card
        VBox productsCard = createStatCard("Products", String.valueOf(products.size()), "#9b59b6");

        statsRow.getChildren().addAll(totalCard, activeCard, expiredCard, productsCard);

        // Recent activity
        Label activityLabel = new Label("Recent Activity");
        activityLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> activityList = new ListView<>();
        activityList.setPrefHeight(200);
        ObservableList<String> activities = FXCollections.observableArrayList(
            "License generated for Machine1 - 12/2026",
            "New product 'Inventory Manager' created",
            "License validated for Machine2 - SUCCESS",
            "Payment received - $49.99",
            "License expired for Machine3"
        );
        activityList.setItems(activities);

        // Quick actions
        Label quickActionsLabel = new Label("Quick Actions");
        quickActionsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox quickActions = new HBox(10);
        Button generateBtn = new Button("Generate New Key");
        Button exportBtn = new Button("Export Data");
        generateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20;");
        exportBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20;");
        quickActions.getChildren().addAll(generateBtn, exportBtn);

        layout.getChildren().addAll(title, statsRow, activityLabel, activityList, quickActionsLabel, quickActions);

        return layout;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #cccccc, 5, 0.5, 2, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLabel, valueLabel);

        return card;
    }

    private VBox createProductsView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        // Title
        Label title = new Label("Products");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Toolbar
        HBox toolbar = new HBox(10);
        Button addProduct = new Button("+ Add Product");
        Button editProduct = new Button("Edit");
        Button deleteProduct = new Button("Delete");
        addProduct.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 16;");
        editProduct.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 16;");
        deleteProduct.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16;");
        toolbar.getChildren().addAll(addProduct, editProduct, deleteProduct);

        // Products table
        TableView<Product> table = new TableView<>();
        table.setItems(products);

        TableColumn<Product, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> versionCol = new TableColumn<>("Version");
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

        TableColumn<Product, Integer> licensesCol = new TableColumn<>("Licenses");
        licensesCol.setCellValueFactory(new PropertyValueFactory<>("licenseCount"));

        table.getColumns().addAll(idCol, nameCol, versionCol, licensesCol);
        table.setPrefHeight(400);

        layout.getChildren().addAll(title, toolbar, table);

        return layout;
    }

    private VBox createLicensesView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        // Title
        Label title = new Label("Licenses");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Search bar
        HBox searchBar = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search licenses...");
        searchField.setPrefWidth(300);
        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");
        searchBar.getChildren().addAll(searchField, searchBtn, refreshBtn);

        // Licenses table
        TableView<License> table = new TableView<>();
        table.setItems(licenses);

        TableColumn<License, String> keyCol = new TableColumn<>("License Key");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<License, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<License, String> machineCol = new TableColumn<>("Machine");
        machineCol.setCellValueFactory(new PropertyValueFactory<>("machineId"));

        TableColumn<License, String> expiryCol = new TableColumn<>("Expires");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        TableColumn<License, Boolean> activeCol = new TableColumn<>("Status");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.getColumns().addAll(keyCol, productCol, machineCol, expiryCol, activeCol);
        table.setPrefHeight(400);

        // Action buttons
        HBox actions = new HBox(10);
        Button validateBtn = new Button("Validate");
        Button revokeBtn = new Button("Revoke");
        Button extendBtn = new Button("Extend");
        validateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 16;");
        revokeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16;");
        extendBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 8 16;");
        actions.getChildren().addAll(validateBtn, revokeBtn, extendBtn);

        layout.getChildren().addAll(title, searchBar, table, actions);

        return layout;
    }

    private VBox createGenerateKeyView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(30));

        // Title
        Label title = new Label("Generate License Key");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Form
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER_LEFT);

        // Product selection
        Label productLabel = new Label("Select Product:");
        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.setItems(products);
        productCombo.setPromptText("Select a product");
        productCombo.setPrefWidth(300);

        // HWID input
        Label hwidLabel = new Label("Hardware ID (HWID):");
        TextField hwidField = new TextField();
        hwidField.setPromptText("Enter HWID or leave blank for current machine");
        hwidField.setPrefWidth(300);

        // Generate button for HWID
        Button generateHWIDBtn = new Button("Get Current HWID");
        generateHWIDBtn.setOnAction(e -> {
            String hwid = HardwareFingerprint.generateHWID();
            hwidField.setText(hwid);
        });

        // Expiry date
        Label expiryLabel = new Label("Expiry Date:");
        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setValue(LocalDate.now().plusMonths(12));
        expiryPicker.setPrefWidth(300);

        // Feature mask
        Label featureLabel = new Label("Feature Tier:");
        ComboBox<String> featureCombo = new ComboBox<>(FXCollections.observableArrayList(
            "1 - Standard (Basic features)",
            "2 - Premium (All features)",
            "3 - Professional (Printing + Premium)",
            "4-9 - Custom tiers"
        ));
        featureCombo.setValue("3 - Professional (Printing + Premium)");
        featureCombo.setPrefWidth(300);

        // Add to form
        form.addRow(0, productLabel, productCombo);
        form.addRow(1, hwidLabel, hwidField, generateHWIDBtn);
        form.addRow(2, expiryLabel, expiryPicker);
        form.addRow(3, featureLabel, featureCombo);

        // Generate button
        Button generateBtn = new Button("Generate License Key");
        generateBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15 30;");
        generateBtn.setPrefWidth(300);

        // Result area
        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 16px;");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(80);
        resultArea.setStyle("-fx-font-family: monospace; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Generate action
        generateBtn.setOnAction(e -> {
            try {
                String hwid = hwidField.getText();
                if (hwid == null || hwid.isEmpty()) {
                    hwid = HardwareFingerprint.generateHWID();
                }

                LocalDate expiryDate = expiryPicker.getValue();
                int expiryYear = expiryDate.getYear() - 2000;
                int expiryMonth = expiryDate.getMonthValue();

                int featureMask = 3; // Default to professional

                String licenseKey = LicenseTokenEngine.generateLicenseKey(hwid, expiryYear, expiryMonth, featureMask);

                resultArea.setText(licenseKey);
                resultLabel.setText("License key generated successfully!");
                resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60;");

                // Copy to clipboard
                javafx.awt.datatransfer.Clipboard clipboard = javafx.awt.datatransfer.Clipboard.getSystemClipboard();
                javafx.awt.datatransfer.ClipboardContent content = new javafx.awt.datatransfer.ClipboardContent();
                content.putString(licenseKey);
                clipboard.setContent(content);

            } catch (Exception ex) {
                resultLabel.setText("Error: " + ex.getMessage());
                resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
                resultArea.setText("");
            }
        });

        // Copy button
        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20;");

        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(generateBtn, copyBtn);

        layout.getChildren().addAll(title, form, buttonRow, resultLabel, resultArea);

        return layout;
    }

    private VBox createSettingsView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(30));

        // Title
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Settings sections
        VBox accountSection = createSettingsSection("Account Settings",
            "Developer ID: dev_001\nEmail: developer@example.com\nAPI Key: sk_live_...");

        VBox licenseSection = createSettingsSection("License Settings",
            "Default Duration: 12 months\nAuto-renewal: Enabled\nWatermark Text: UNREGISTERED - THORIUM DEMO");

        VBox securitySection = createSettingsSection("Security",
            "Clock Rollback Protection: Enabled\nObfuscation: Recommended for production\nHardware Binding: CPU + Motherboard");

        // Save button
        Button saveBtn = new Button("Save Settings");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 12 24; -fx-font-size: 14px;");

        layout.getChildren().addAll(title, accountSection, licenseSection, securitySection, saveBtn);

        return layout;
    }

    private VBox createSettingsSection(String title, String content) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #cccccc, 3, 0.5, 1, 1);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea contentArea = new TextArea(content);
        contentArea.setEditable(false);
        contentArea.setPrefHeight(80);
        contentArea.setStyle("-fx-font-family: monospace;");

        section.getChildren().addAll(titleLabel, contentArea);

        return section;
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Thorium Admin");
        alert.setHeaderText("Thorium Licensing System");
        alert.setContentText(
            "Version: " + APP_VERSION + "\n\n" +
            "Thorium is a Licensing-as-a-Service platform that enables\n" +
            "software developers to protect their desktop applications\n" +
            "through hardware-based node-locked licensing.\n\n" +
            "© 2026 Thorium Team"
        );
        alert.showAndWait();
    }

    private String createStylesheet() {
        return """
            -fx-font-family: 'Segoe UI', Arial, sans-serif;
            -fx-background-color: #f5f6fa;
            .button {
                -fx-cursor: hand;
            }
            .text-field {
                -fx-padding: 8;
                -fx-background-radius: 4;
            }
            .tab {
                -fx-padding: 10 20;
            }
            .table-view {
                -fx-background-color: white;
            }
            """;
    }

    // Model classes
    public static class Product {
        private String id;
        private String name;
        private String version;
        private int licenseCount;

        public Product(String id, String name, String version, int licenseCount) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.licenseCount = licenseCount;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public int getLicenseCount() { return licenseCount; }
    }

    public static class License {
        private String key;
        private String productId;
        private String machineId;
        private String expiryDate;
        private boolean active;

        public License(String key, String productId, String machineId, String expiryDate, boolean active) {
            this.key = key;
            this.productId = productId;
            this.machineId = machineId;
            this.expiryDate = expiryDate;
            this.active = active;
        }

        public String getKey() { return key; }
        public String getProductId() { return productId; }
        public String getMachineId() { return machineId; }
        public String getExpiryDate() { return expiryDate; }
        public boolean isActive() { return active; }
    }
}
