package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.PiecesDetachees;
import connection.DatabaseConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import application.Main;

public class PiecesDetacheesController {
    @FXML
    private TableView<PiecesDetachees> piecesTableView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    
    @FXML
    private ImageView imageView;
    @FXML
    private Button uploadImageButton;

    private File selectedImageFile;

    @FXML
    private void initialize() {
        
        setupTable();
        loadPieces();
        
        
        piecesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFieldsWithPiece(newSelection);
            }
        });
    }

    private void populateFieldsWithPiece(PiecesDetachees piece) {
        nameField.setText(piece.getName());
        priceField.setText(String.valueOf(piece.getPrice()));
        imageView.setImage(piece.getPhoto());
    }

    
	private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
    }
    

    @FXML
    private void onBtnChooseImageAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedImageFile));
                imageView.setImage(image);
            } catch (FileNotFoundException e) {
                showAlert("Error", "File not found: " + selectedImageFile.getAbsolutePath());
            } catch (IllegalArgumentException e) {
                showAlert("Error", "Invalid image file: " + selectedImageFile.getAbsolutePath());
            }
        }
    }
    @FXML
    private void handleCreatePiece() {
        String name = nameField.getText();
        double price = Double.parseDouble(priceField.getText());

        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertPieceSQL = "INSERT INTO pieces_detachees (nom, prix_ht, photo) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertPieceSQL)) {
                statement.setString(1, name);
                statement.setDouble(2, price);
                if (selectedImageFile != null) {
                    statement.setBinaryStream(3, new FileInputStream(selectedImageFile), (int) selectedImageFile.length());
                } else {
                    statement.setNull(3, Types.BLOB);
                }
                statement.executeUpdate();
                showAlert("Success", "Pièce ajoutée avec succès.");
                loadPieces(); // we have to Refresh after adding a new one
            }
        } catch (SQLException | FileNotFoundException e) {
            handleException(e, "Une erreur s'est produite lors de l'ajout de la pièce.");
        }
    }


    @FXML
    private void handleUpdatePiece() {
        PiecesDetachees selectedPiece = piecesTableView.getSelectionModel().getSelectedItem();
        if (selectedPiece != null) {
            String newName = nameField.getText();
            double newPrice = Double.parseDouble(priceField.getText());

            try (Connection connection = DatabaseConnection.getConnection()) {
                String updatePieceSQL = "UPDATE pieces_detachees SET nom = ?, prix_ht = ?, photo = ? WHERE piece_id = ?";
                PreparedStatement statement = connection.prepareStatement(updatePieceSQL);
                statement.setString(1, newName);
                statement.setDouble(2, newPrice);
                if (selectedImageFile != null) {
                    statement.setBinaryStream(3, new FileInputStream(selectedImageFile), (int) selectedImageFile.length());
                } else {
                    statement.setNull(3, Types.BLOB);
                }
                statement.setInt(4, selectedPiece.getId());
                statement.executeUpdate();
                showAlert("Success", "Pièce mise à jour avec succès.");
                loadPieces(); // we have to Refresh after updating
            } catch (SQLException | FileNotFoundException e) {
                handleException(e, "Une erreur s'est produite lors de la mise à jour de la pièce.");
            }
        } else {
            showAlert("No Selection", "Veuillez sélectionner une pièce à mettre à jour.");
        }
    }


    @FXML
    private void handleDeletePiece() {
        PiecesDetachees selectedPiece = piecesTableView.getSelectionModel().getSelectedItem();
        if (selectedPiece != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String deletePieceSQL = "DELETE FROM pieces_detachees WHERE piece_id = ?";
                PreparedStatement statement = connection.prepareStatement(deletePieceSQL);
                statement.setInt(1, selectedPiece.getId());
                statement.executeUpdate();
                showAlert("Success", "Piece deleted successfully.");
                loadPieces(); // we have to Refresh after deleting
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while deleting the piece.");
            }
        } else {
            showAlert("No Selection", "Please select a piece to delete.");
        }
    }

    private void loadPieces() {
        List<PiecesDetachees> piecesList = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String selectPiecesSQL = "SELECT * FROM pieces_detachees";
            try (PreparedStatement statement = connection.prepareStatement(selectPiecesSQL);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("piece_id");
                    String name = resultSet.getString("nom");
                    double price = resultSet.getDouble("prix_ht");
                    Blob photoBlob = resultSet.getBlob("photo");
                    Image photoImage = null;
                    if (photoBlob != null) {
                        try {
                            photoImage = new Image(photoBlob.getBinaryStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    piecesList.add(new PiecesDetachees(id, name, price, photoImage));
                }
            }
        } catch (SQLException e) {
            handleException(e, "Une erreur s'est produite lors du chargement des pièces.");
        }
        piecesTableView.getItems().setAll(piecesList);
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<PiecesDetachees, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<PiecesDetachees, String> nameColumn = new TableColumn<>("Nom");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<PiecesDetachees, Double> priceColumn = new TableColumn<>("Prix HT");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<PiecesDetachees, Image> photoColumn = new TableColumn<>("Photo");
        photoColumn.setCellValueFactory(new PropertyValueFactory<>("photo"));
        photoColumn.setCellFactory(column -> new TableCell<PiecesDetachees, Image>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        piecesTableView.getColumns().setAll(idColumn, nameColumn, priceColumn, photoColumn);
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleException(Exception e, String message) {
        e.printStackTrace();
        showAlert("Error", message);
    }
    
    @FXML
    private void handleLogout() {
        Main.showLoginRegisterView();
    }
}
