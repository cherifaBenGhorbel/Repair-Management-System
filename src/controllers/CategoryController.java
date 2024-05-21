package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import connection.DatabaseConnection;
import application.Main;
import models.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryController {
    @FXML
    private TextField categoryNameField;
    @FXML
    private TextField categoryTariffField;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, Integer> idColumn;
    @FXML
    private TableColumn<Category, String> libColumn;
    @FXML
    private TableColumn<Category, Double> tarifColumn;

    private ObservableList<Category> categoryData;

    public CategoryController() {
        categoryData = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        libColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        tarifColumn.setCellValueFactory(new PropertyValueFactory<>("tarif"));
        
        
        categoryTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                categoryNameField.setText(newValue.getLibelle());
                categoryTariffField.setText(String.valueOf(newValue.getTarif()));
            }
        });

        loadCategoryData();
    }

    private void loadCategoryData() {
        categoryData.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Categories";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("categorie_id"); 
                String libelle = resultSet.getString("libelle");
                double tarif = resultSet.getDouble("tarif");

                Category category = new Category(id, libelle, tarif);
                categoryData.add(category);
            }
            categoryTable.setItems(categoryData);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while loading categories.");
        }
    }


    @FXML
    private void handleInsertCategory() {
        String categoryName = categoryNameField.getText();
        double categoryTariff = Double.parseDouble(categoryTariffField.getText());

        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertCategorySQL = "INSERT INTO Categories (libelle, tarif) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(insertCategorySQL);
            statement.setString(1, categoryName);
            statement.setDouble(2, categoryTariff);
            statement.executeUpdate();
            showAlert("Success", "Category added successfully.");
            loadCategoryData();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while adding the category.");
        }
    }

    @FXML
    private void handleModifyCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            String newCategoryName = categoryNameField.getText();
            double newCategoryTariff = Double.parseDouble(categoryTariffField.getText());

            try (Connection connection = DatabaseConnection.getConnection()) {
                String updateCategorySQL = "UPDATE Categories SET libelle = ?, tarif = ? WHERE categorie_id = ?"; 
                PreparedStatement statement = connection.prepareStatement(updateCategorySQL);
                statement.setString(1, newCategoryName);
                statement.setDouble(2, newCategoryTariff);
                statement.setInt(3, selectedCategory.getId());
                statement.executeUpdate();
                showAlert("Success", "Category updated successfully.");
                loadCategoryData();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while updating the category.");
            }
        } else {
            showAlert("No Selection", "Please select a category to modify.");
        }
    }
  




    @FXML
    private void handleDeleteCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String deleteCategorySQL = "DELETE FROM Categories WHERE categorie_id = ?"; 
                PreparedStatement statement = connection.prepareStatement(deleteCategorySQL);
                statement.setInt(1, selectedCategory.getId());
                statement.executeUpdate();
                showAlert("Success", "Category deleted successfully.");
                loadCategoryData();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while deleting the category.");
            }
        } else {
            showAlert("No Selection", "Please select a category to delete.");
        }
    }


    @FXML
    private void handleLogout() {
        Main.showLoginRegisterView();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
