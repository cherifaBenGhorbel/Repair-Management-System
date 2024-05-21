package controllers;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.Main;
import connection.DatabaseConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import models.OrdreReparation;
import models.PieceAChanger;
import models.PiecesDetachees;

public class PieceAChangerController {

    @FXML
    private TableView<PieceAChanger> pieceTable;

    @FXML
    private TableColumn<PieceAChanger, Integer> pieceOrdreIdColumn;

    @FXML
    private TableColumn<PieceAChanger, String> ordreIdColumn;

    @FXML
    private TableColumn<PieceAChanger, String> clientNameColumn;

    @FXML
    private TableColumn<PieceAChanger, String> pieceNameColumn;


    @FXML
    private TableColumn<PieceAChanger, Integer> quantiteColumn;

    @FXML
    private ComboBox<Integer> ordreIdComboBox;

    @FXML
    private ComboBox<Integer> pieceIdComboBox;

    @FXML
    private TextField quantiteField;

    @FXML
    private Button addButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button deleteButton;

    private ObservableList<PieceAChanger> pieceAChangerList;

    @FXML
    private void initialize() {
        pieceOrdreIdColumn.setCellValueFactory(new PropertyValueFactory<>("pieceOrdreId"));
        ordreIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrdre().toString()));
        clientNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClientName()));
        pieceNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPiece().getName()));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        pieceAChangerList = FXCollections.observableArrayList();
        pieceTable.setItems(pieceAChangerList);

        pieceTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> populateFields(newValue));

        loadOrdres();
        loadPieces();
        loadPieceAChanger();
    }

    private void populateFields(PieceAChanger pieceAChanger) {
        if (pieceAChanger != null) {
            ordreIdComboBox.setValue(pieceAChanger.getOrdre().getOrdreId());
            pieceIdComboBox.setValue(pieceAChanger.getPiece().getId());
            quantiteField.setText(String.valueOf(pieceAChanger.getQuantite()));
        } else {
            ordreIdComboBox.setValue(null);
            pieceIdComboBox.setValue(null);
            quantiteField.clear();
        }
    }

    private void loadOrdres() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT ordre_id FROM ordres_reparation");
             ResultSet resultSet = statement.executeQuery()) {

            ObservableList<Integer> ordreIds = FXCollections.observableArrayList();
            while (resultSet.next()) {
                ordreIds.add(resultSet.getInt("ordre_id"));
            }
            ordreIdComboBox.setItems(ordreIds);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPieces() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT piece_id FROM pieces_detachees");
             ResultSet resultSet = statement.executeQuery()) {

            ObservableList<Integer> pieceIds = FXCollections.observableArrayList();
            while (resultSet.next()) {
                pieceIds.add(resultSet.getInt("piece_id"));
            }
            pieceIdComboBox.setItems(pieceIds);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPieceAChanger() {
        pieceAChangerList.clear();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT PAC.piece_ordre_id, ORDR.ordre_id, C.nom AS client_name, A.description AS ordre_description, PAC.quantite, P.piece_id, P.nom AS piece_name, P.photo " +
                             "FROM pieces_a_changer PAC " +
                             "INNER JOIN ordres_reparation ORDR ON PAC.ordre_id = ORDR.ordre_id " +
                             "INNER JOIN appareils A ON ORDR.appareil_id = A.appareil_id " +
                             "INNER JOIN clients C ON A.client_id = C.client_id " +
                             "INNER JOIN pieces_detachees P ON PAC.piece_id = P.piece_id"
             );
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                OrdreReparation ordre = new OrdreReparation(resultSet.getInt("ordre_id"), resultSet.getString("ordre_description"));
                PiecesDetachees piece = new PiecesDetachees(resultSet.getInt("piece_id"), resultSet.getString("piece_name"), resultSet.getBlob("photo"));
                PieceAChanger pieceAChanger = new PieceAChanger(
                        resultSet.getInt("piece_ordre_id"),
                        ordre,
                        piece,
                        resultSet.getInt("quantite")
                );
                pieceAChanger.setClientName(resultSet.getString("client_name")); // Set client name
                pieceAChangerList.add(pieceAChanger);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddButton() {
        Integer selectedOrdreId = ordreIdComboBox.getValue();
        Integer selectedPieceId = pieceIdComboBox.getValue();
        String quantiteText = quantiteField.getText();

        if (selectedOrdreId == null || selectedPieceId == null) {
            showAlert("Error", "Please select both an ordre and a piece.");
            return;
        }

        if (quantiteText == null || quantiteText.trim().isEmpty()) {
            showAlert("Error", "Please enter a quantity.");
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(quantiteText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for quantity.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO pieces_a_changer (ordre_id, piece_id, quantite) VALUES (?, ?, ?)")) {

            statement.setInt(1, selectedOrdreId);
            statement.setInt(2, selectedPieceId);
            statement.setInt(3, quantite);
            statement.executeUpdate();

            loadPieceAChanger();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifyButton() {
        PieceAChanger selectedPieceAChanger = pieceTable.getSelectionModel().getSelectedItem();
        if (selectedPieceAChanger == null) {
            showAlert("Error", "Please select a piece to modify.");
            return;
        }

        String quantiteText = quantiteField.getText();
        if (quantiteText == null || quantiteText.trim().isEmpty()) {
            showAlert("Error", "Please enter a valid quantity.");
            return;
        }

        int newQuantite;
        try {
            newQuantite = Integer.parseInt(quantiteText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for quantity.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE pieces_a_changer SET quantite = ? WHERE piece_ordre_id = ?")) {

            statement.setInt(1, newQuantite);
            statement.setInt(2, selectedPieceAChanger.getPieceOrdreId());
            statement.executeUpdate();

            loadPieceAChanger();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteButton() {
        PieceAChanger selectedPieceAChanger = pieceTable.getSelectionModel().getSelectedItem();
        if (selectedPieceAChanger == null) {
            showAlert("Error", "Please select a piece to delete.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM pieces_a_changer WHERE piece_ordre_id = ?")) {

            statement.setInt(1, selectedPieceAChanger.getPieceOrdreId());
            statement.executeUpdate();

            loadPieceAChanger();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleLogout() {
        Main.showLoginRegisterView();
    }
}
