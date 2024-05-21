package controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import session.Session;
import connection.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import application.Main;

public class InsertOrderController {
    @FXML
    private TextField applianceDescriptionField;
    @FXML
    private TextField applianceBrandField;
    @FXML
    private ComboBox<String> categoryComboBox;
    
    

    @FXML
    private TableView<Map<String, String>> applianceTable;

    @FXML
    private TableColumn<Map<String, String>, Integer> appareilIdColumn;
    @FXML
    private TableColumn<Map<String, String>, String> descriptionColumn;
    @FXML
    private TableColumn<Map<String, String>, String> brandColumn;
    @FXML
    private TableColumn<Map<String, String>, String> categoryColumn;
    
    private boolean isEditing = false;


    private final ObservableList<Map<String, String>> appliances = FXCollections.observableArrayList();

    @FXML
    private void handleLogout() {
        Main.showLoginRegisterView();
    }

    @FXML
    private void initialize() {
        loadCategories();

        appareilIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(Integer.parseInt(data.getValue().get("appareil_id"))).asObject());
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("description")));
        brandColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("marque")));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category")));

        applianceTable.setItems(appliances);

        loadAppliances();
    }

    /*
    @FXML
    private void handleSubmit() {
        String applianceDescription = applianceDescriptionField.getText();
        String applianceBrand = applianceBrandField.getText();
        String category = categoryComboBox.getValue();

        if (category == null) {
            showAlert("Error", "Please select a category.");
            return;
        }

        int clientId = Session.getLoggedInClientId(); 

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO Appareils (description, marque, categorie_id, client_id) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, applianceDescription);
            statement.setString(2, applianceBrand);
            statement.setInt(3, getCategoryId(connection, category));
            statement.setInt(4, clientId);
            statement.executeUpdate();

            showAlert("Success", "Repair order added successfully.");
            refreshApplianceTable();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while adding the repair order.");
        }
    }
*/
    @FXML
    private void handleSubmit() {
        String applianceDescription = applianceDescriptionField.getText();
        String applianceBrand = applianceBrandField.getText();
        String category = categoryComboBox.getValue();

        if (category == null) {
            showAlert("Error", "Please select a category.");
            return;
        }

        int clientId = Session.getLoggedInClientId(); 

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Check if the appliance already exists for this client
            if (applianceExists(connection, clientId, applianceDescription, applianceBrand, category)) {
                showAlert("Error", "This appliance already exists for this client.");
                return;
            }

            // Insert the appliance into the database
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Appareils (description, marque, categorie_id, client_id) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, applianceDescription);
                statement.setString(2, applianceBrand);
                statement.setInt(3, getCategoryId(connection, category));
                statement.setInt(4, clientId);
                statement.executeUpdate();

                showAlert("Success", "Appliance added successfully.");
                refreshApplianceTable();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while adding the appliance.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database connection error.");
        }
    }

    private boolean applianceExists(Connection connection, int clientId, String description, String brand, String category) throws SQLException {
        String query = "SELECT COUNT(*) FROM Appareils WHERE client_id = ? AND description = ? AND marque = ? AND categorie_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, clientId);
            statement.setString(2, description);
            statement.setString(3, brand);
            statement.setInt(4, getCategoryId(connection, category));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // Appliance exists if count is greater than 0
                }
            }
        }
        return false;
    }

    

    @FXML
    private void handleModifyAppliance() {
        Map<String, String> selectedRow = applianceTable.getSelectionModel().getSelectedItem();

        if (selectedRow != null) {
            if (!isEditing) {
                // Enable the form fields for editing
                applianceDescriptionField.setText(selectedRow.get("description"));
                applianceBrandField.setText(selectedRow.get("marque"));
                categoryComboBox.setValue(selectedRow.get("category"));

                applianceDescriptionField.setEditable(true);
                applianceBrandField.setEditable(true);
                categoryComboBox.setDisable(false);

                isEditing = true;
            } else {
                // Get the values from the form fields
                String applianceDescription = applianceDescriptionField.getText();
                String applianceBrand = applianceBrandField.getText();
                String category = categoryComboBox.getValue();

                if (category == null) {
                    showAlert("Error", "Please select a category.");
                    return;
                }

                int selectedApplianceId = Integer.parseInt(selectedRow.get("appareil_id"));

                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement("UPDATE Appareils SET description = ?, marque = ?, categorie_id = ? WHERE appareil_id = ?")) {

                    int categoryId = getCategoryId(connection, category);

                    if (categoryId == -1) {
                        showAlert("Error", "Selected category does not exist.");
                        return;
                    }

                    statement.setString(1, applianceDescription);
                    statement.setString(2, applianceBrand);
                    statement.setInt(3, categoryId);
                    statement.setInt(4, selectedApplianceId);

                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected > 0) {
                        selectedRow.put("description", applianceDescription);
                        selectedRow.put("marque", applianceBrand);
                        selectedRow.put("category", category);
                        applianceTable.refresh();

                        showAlert("Success", "Appliance details updated successfully.");

                        applianceDescriptionField.clear();
                        applianceBrandField.clear();
                        categoryComboBox.setValue(null);

                        applianceDescriptionField.setEditable(false);
                        applianceBrandField.setEditable(false);
                        categoryComboBox.setDisable(true);

                        isEditing = false;
                    } else {
                        showAlert("Error", "Failed to update appliance details.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "An error occurred while updating appliance details.");
                }
            }
        } else {
            showAlert("Error", "Please select an appliance to modify.");
        }
    }




    @FXML
    private void handleDeleteAppliance() {
        int selectedApplianceId = getSelectedApplianceIdFromTable();

        if (selectedApplianceId != -1) {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM Appareils WHERE appareil_id = ?")) {
                statement.setInt(1, selectedApplianceId);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert("Success", "Appliance deleted successfully.");
                    refreshApplianceTable(); // Refresh the table view after deletion
                } else {
                    showAlert("Error", "Failed to delete appliance.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while deleting appliance.");
            }
        } else {
            showAlert("Error", "Please select an appliance to delete.");
        }
    }

    private void refreshApplianceTable() {
        appliances.clear();
        loadAppliances();
    }

  
    private void loadAppliances() {
        int clientId = Session.getLoggedInClientId(); 

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT appareil_id, description, marque, categorie_id FROM Appareils WHERE client_id = ?");
        ) {
            statement.setInt(1, clientId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appareilId = resultSet.getInt("appareil_id");
                String description = resultSet.getString("description");
                String brand = resultSet.getString("marque");
                String category = getCategoryName(connection, resultSet.getInt("categorie_id"));
                addApplianceToTableView(appareilId, description, brand, category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load appliances.");
        }
    }


    private int getCategoryId(Connection connection, String category) throws SQLException {
        String query = "SELECT categorie_id FROM Categories WHERE libelle = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, category);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("categorie_id");
                }
            }
        }
        return -1;
    }

    private void loadCategories() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT libelle FROM Categories");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categoryComboBox.getItems().add(resultSet.getString("libelle"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load categories.");
        }
    }

    private String getCategoryName(Connection connection, int categoryId) throws SQLException {
        String query = "SELECT libelle FROM Categories WHERE categorie_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("libelle");
                }
            }
        }
        return "Unknown";
    }

    private void addApplianceToTableView(int appareilId, String description, String brand, String category) {
        Map<String, String> applianceData = new HashMap<>();
        applianceData.put("appareil_id", String.valueOf(appareilId));
        applianceData.put("description", description);
        applianceData.put("marque", brand);
        applianceData.put("category", category);
        appliances.add(applianceData);
    }

    private int getSelectedApplianceIdFromTable() {
        Map<String, String> selectedRow = applianceTable.getSelectionModel().getSelectedItem();
        if (selectedRow != null) {
            try {
                return Integer.parseInt(selectedRow.get("appareil_id"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                showAlert("Error", "Invalid appliance ID format.");
            }
        }
        return -1; // Return -1 if no row is selected or ID is invalid
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    @FXML
    private void handleViewFactureButton() {
        int selectedApplianceId = getSelectedApplianceIdFromTable();
        int clientId = Session.getLoggedInClientId(); // Get the logged-in client's ID

        if (selectedApplianceId > 0 && clientId > 0) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/appareils/ClientFactureView.fxml"));
                Parent root = loader.load();

                ClientFactureController controller = loader.getController();
                controller.loadFacture(selectedApplianceId, clientId);

                Stage stage = new Stage();
                stage.setTitle("Facture Details");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                showAlert("Error", "Failed to load facture view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Please select a valid appliance.");
        }
    }
}

