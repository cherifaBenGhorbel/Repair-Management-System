package controllers;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Appareil;
import models.OrdreReparation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import application.Main;
import connection.DatabaseConnection;

public class OrdreReparationController {

    @FXML
    private TableView<OrdreReparation> ordreReparationTable;

    @FXML
    private TableColumn<OrdreReparation, Integer> ordreIdColumn;

    @FXML
    private TableColumn<OrdreReparation, String> clientNameColumn;

    @FXML
    private TableColumn<OrdreReparation, String> appareilColumn;

    @FXML
    private TableColumn<OrdreReparation, Integer> nbHeuresMOColumn;

    @FXML
    private ChoiceBox<Appareil> appareilChoiceBox;

    @FXML
    private TextField nbHeuresMOTextField;

    @FXML
    private Button addButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;
    
    @FXML
    private Button displayFactureButton;

    private ObservableList<OrdreReparation> ordreReparationList;

    private static final String LOAD_APPAREILS_QUERY = "SELECT * FROM appareils";
    private static final String LOAD_ORDRE_REPARATIONS_QUERY = 
        "SELECT ORDR.ordre_id, C.nom AS client_name, A.description AS appareil_description, ORDR.NbHeuresMO " +
        "FROM ordres_reparation ORDR " +
        "INNER JOIN appareils A ON ORDR.appareil_id = A.appareil_id " +
        "INNER JOIN Clients C ON A.client_id = C.client_id";
    private static final String INSERT_ORDRE_REPARATION_QUERY = 
        "INSERT INTO ordres_reparation (appareil_id, NbHeuresMO) VALUES (?, ?)";
    private static final String DELETE_ORDRE_REPARATION_QUERY = 
        "DELETE FROM ordres_reparation WHERE ordre_id = ?";
    private static final String UPDATE_ORDRE_REPARATION_QUERY = 
        "UPDATE ordres_reparation SET appareil_id = ?, NbHeuresMO = ? WHERE ordre_id = ?";
    private static final String GENERATE_FACTURE_QUERY =
    	    "SELECT o.ordre_id, c.nom AS client_name, a.description AS appareil_description, o.NbHeuresMO, " +
    	    "p.piece_ordre_id, p.quantite, pd.prix_ht, (p.quantite * pd.prix_ht) AS piece_cost, " +
    	    "(o.NbHeuresMO * 3000) AS labor_cost " +
    	    "FROM ordres_reparation o " +
    	    "JOIN appareils a ON o.appareil_id = a.appareil_id " +
    	    "JOIN clients c ON a.client_id = c.client_id " +
    	    "JOIN pieces_a_changer p ON o.ordre_id = p.ordre_id " +
    	    "JOIN pieces_detachees pd ON p.piece_id = pd.piece_id " +
    	    "WHERE o.ordre_id = ?";
    @FXML
    private void handleGenerateFactureButton() {
        OrdreReparation selectedOrder = ordreReparationTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            generateFacture(selectedOrder.getOrdreId());
        } else {
            showAlert("Error", "Please select an order to generate the facture.");
        }
    }
    private void generateFacture(int orderId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(GENERATE_FACTURE_QUERY)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            displayFacture(resultSet);

        } catch (SQLException e) {
            showAlert("Error", "An error occurred while generating the facture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayFacture(ResultSet resultSet) {
        StringBuilder factureContent = new StringBuilder();
        factureContent.append("Facture Details:\n\n");

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        try {
            while (resultSet.next()) {
                int orderId = resultSet.getInt("ordre_id");
                String clientName = resultSet.getString("client_name");
                String appareilDescription = resultSet.getString("appareil_description");
                int nbHeuresMO = resultSet.getInt("NbHeuresMO");
                int pieceOrderId = resultSet.getInt("piece_ordre_id");
                int quantity = resultSet.getInt("quantite");
                double piecePrice = resultSet.getDouble("prix_ht");
                double pieceCost = resultSet.getDouble("piece_cost");
                double laborCost = resultSet.getDouble("labor_cost");

                factureContent.append(String.format("Order ID: %d\n", orderId));
                factureContent.append(String.format("Client Name: %s\n", clientName));
                factureContent.append(String.format("Appareil: %s\n", appareilDescription));
                factureContent.append(String.format("NbHeuresMO: %d\n", nbHeuresMO));
                factureContent.append(String.format("Piece ID: %d\n", pieceOrderId));
                factureContent.append(String.format("Quantity: %d\n", quantity));
                factureContent.append(String.format("Piece Price: $%s\n", decimalFormat.format(piecePrice)));
                factureContent.append(String.format("Piece Cost: $%s\n", decimalFormat.format(pieceCost)));
                factureContent.append(String.format("Labor Cost: $%s\n", decimalFormat.format(laborCost)));
                factureContent.append("--------------------------------------------------\n");
            }
        } catch (SQLException e) {
            showAlert("Error", "An error occurred while fetching facture details: " + e.getMessage());
            e.printStackTrace();
        } finally {
            displayFactureContent(factureContent.toString());
        }
    }

    
    private void displayFactureContent(String factureContent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Facture Details");
        alert.setHeaderText(null);
        alert.setContentText(factureContent);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        ordreIdColumn.setCellValueFactory(new PropertyValueFactory<>("ordreId"));
        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        appareilColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAppareil().toString()));
        nbHeuresMOColumn.setCellValueFactory(new PropertyValueFactory<>("nbHeuresMO"));

        ordreReparationList = FXCollections.observableArrayList();
        ordreReparationTable.setItems(ordreReparationList);

        loadAppareils();
        loadOrdreReparations();

        ordreReparationTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFields(newValue);
            }
        });
    }

    private void populateFields(OrdreReparation ordreReparation) {
        appareilChoiceBox.setValue(ordreReparation.getAppareil());
        nbHeuresMOTextField.setText(String.valueOf(ordreReparation.getNbHeuresMO()));
    }

    private void loadAppareils() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOAD_APPAREILS_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Appareil appareil = new Appareil(
                        resultSet.getInt("appareil_id"),
                        resultSet.getString("description"),
                        resultSet.getString("marque"),
                        resultSet.getInt("client_id"),
                        resultSet.getInt("categorie_id"));
                appareilChoiceBox.getItems().add(appareil);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appareils: " + e.getMessage());
        }
    }

    private void loadOrdreReparations() {
        ordreReparationList.clear();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOAD_ORDRE_REPARATIONS_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Appareil appareil = new Appareil(
                        resultSet.getString("appareil_description"), "", 0, 0);
                OrdreReparation ordreReparation = new OrdreReparation(
                        resultSet.getInt("ordre_id"),
                        resultSet.getString("client_name"),
                        resultSet.getString("appareil_description"),
                        appareil,
                        resultSet.getInt("NbHeuresMO"));
                ordreReparationList.add(ordreReparation);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load ordre reparations: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddButton() {
        Appareil selectedAppareil = appareilChoiceBox.getValue();
        if (selectedAppareil == null) {
            showAlert("Error", "Please select an appareil.");
            return;
        }

        int nbHeuresMO;
        try {
            nbHeuresMO = Integer.parseInt(nbHeuresMOTextField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number of labor hours.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ORDRE_REPARATION_QUERY)) {

            statement.setInt(1, selectedAppareil.getAppareilId());
            statement.setInt(2, nbHeuresMO);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 1) {
                showAlert("Success", "Repair order added successfully.");
                loadOrdreReparations();
            } else {
                showAlert("Error", "Failed to add repair order.");
            }

        } catch (SQLException e) {
            showAlert("Error", "An error occurred while adding the repair order: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteButton() {
        OrdreReparation selectedOrdre = ordreReparationTable.getSelectionModel().getSelectedItem();
        if (selectedOrdre == null) {
            showAlert("Error", "Please select a repair order to delete.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ORDRE_REPARATION_QUERY)) {

            statement.setInt(1, selectedOrdre.getOrdreId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 1) {
                showAlert("Success", "Repair order deleted successfully.");
                ordreReparationList.remove(selectedOrdre);
            } else {
                showAlert("Error", "Failed to delete repair order.");
            }

        } catch (SQLException e) {
            showAlert("Error", "An error occurred while deleting the repair order: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifyButton() {
        OrdreReparation selectedOrdre = ordreReparationTable.getSelectionModel().getSelectedItem();
        if (selectedOrdre == null) {
            showAlert("Error", "Please select a repair order to modify.");
            return;
        }

        Appareil newAppareil = appareilChoiceBox.getValue();
        if (newAppareil == null) {
            showAlert("Error", "Please select an appareil.");
            return;
        }

        int newNbHeuresMO;
        try {
            newNbHeuresMO = Integer.parseInt(nbHeuresMOTextField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number of labor hours.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ORDRE_REPARATION_QUERY)) {

            statement.setInt(1, newAppareil.getAppareilId());
            statement.setInt(2, newNbHeuresMO);
            statement.setInt(3, selectedOrdre.getOrdreId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 1) {
                showAlert("Success", "Repair order modified successfully.");
                loadOrdreReparations();
            } else {
                showAlert("Error", "Failed to modify repair order.");
            }

        } catch (SQLException e) {
            showAlert("Error", "An error occurred while modifying the repair order: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchButton() {
        String clientName = searchTextField.getText().trim();
        if (clientName.isEmpty()) {
            showAlert("Error", "Please enter a client name to search.");
            return;
        }
        searchOrdreReparationByClientName(clientName);
    }

    private void searchOrdreReparationByClientName(String clientName) {
        ordreReparationList.clear();
        String searchQuery = 
            "SELECT ORDR.ordre_id, C.nom AS client_name, A.description AS appareil_description, ORDR.NbHeuresMO " +
            "FROM ordres_reparation ORDR " +
            "INNER JOIN appareils A ON ORDR.appareil_id = A.appareil_id " +
            "INNER JOIN Clients C ON A.client_id = C.client_id " +
            "WHERE C.nom LIKE ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(searchQuery)) {

            statement.setString(1, "%" + clientName + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Appareil appareil = new Appareil(
                        resultSet.getString("appareil_description"), "", 0, 0);
                OrdreReparation ordreReparation = new OrdreReparation(
                        resultSet.getInt("ordre_id"),
                        resultSet.getString("client_name"),
                        resultSet.getString("appareil_description"),
                        appareil,
                        resultSet.getInt("NbHeuresMO"));
                ordreReparationList.add(ordreReparation);
            }
        } catch (SQLException e) {
            showAlert("Error", "An error occurred while searching for repair orders: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleLogout() {
        Main.showLoginRegisterView();
    }
}
