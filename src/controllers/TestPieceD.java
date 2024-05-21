package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import models.PiecesDetachees;

public class TestPieceD {
    @FXML
    private TableView<PiecesDetachees> piecesTableView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private Button uploadImageButton;

    private File selectedImageFile;
    private Image selectedImage;

    @FXML
    private void initialize() {
        uploadImageButton.setOnAction(event -> handleUploadImage());
    }

    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                setSelectedImage(new Image(new FileInputStream(selectedImageFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreatePiece() {
        String name = nameField.getText();
        double price = Double.parseDouble(priceField.getText());

        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertPieceSQL = "INSERT INTO PiecesDetachees (nom, prix_ht, photo) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(insertPieceSQL);
            statement.setString(1, name);
            statement.setDouble(2, price);
            if (selectedImageFile != null) {
                statement.setBinaryStream(3, new FileInputStream(selectedImageFile));
            } else {
                statement.setBinaryStream(3, null);
            }
            statement.executeUpdate();
            showAlert("Success", "Piece added successfully.");
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while adding the piece.");
        }
    }

    @FXML
    private void handleUpdatePiece() {
        PiecesDetachees selectedPiece = piecesTableView.getSelectionModel().getSelectedItem();
        if (selectedPiece != null) {
            String newName = nameField.getText();
            double newPrice = Double.parseDouble(priceField.getText());

            try (Connection connection = DatabaseConnection.getConnection()) {
                String updatePieceSQL = "UPDATE PiecesDetachees SET nom = ?, prix_ht = ?, photo = ? WHERE piece_id = ?";
                PreparedStatement statement = connection.prepareStatement(updatePieceSQL);
                statement.setString(1, newName);
                statement.setDouble(2, newPrice);
                if (selectedImageFile != null) {
                    statement.setBinaryStream(3, new FileInputStream(selectedImageFile));
                } else {
                    statement.setBinaryStream(3, null);
                }
                statement.setInt(4, selectedPiece.getId());
                statement.executeUpdate();
                showAlert("Success", "Piece updated successfully.");
            } catch (SQLException | FileNotFoundException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while updating the piece.");
            }
        } else {
            showAlert("No Selection", "Please select a piece to update.");
        }
    }

    @FXML
    private void handleDeletePiece() {
        PiecesDetachees selectedPiece = piecesTableView.getSelectionModel().getSelectedItem();
        if (selectedPiece != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String deletePieceSQL = "DELETE FROM PiecesDetachees WHERE piece_id = ?";
                PreparedStatement statement = connection.prepareStatement(deletePieceSQL);
                statement.setInt(1, selectedPiece.getId());
                statement.executeUpdate();
                showAlert("Success", "Piece deleted successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while deleting the piece.");
            }
        } else {
            showAlert("No Selection", "Please select a piece to delete.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

	public Image getSelectedImage() {
		return selectedImage;
	}

	public void setSelectedImage(Image selectedImage) {
		this.selectedImage = selectedImage;
	}
}
