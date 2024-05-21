package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class ClientFactureController {

    @FXML
    private TextArea factureTextArea;

    private static final String GENERATE_FACTURE_QUERY =
        "SELECT o.ordre_id, c.nom AS client_name, a.description AS appareil_description, o.NbHeuresMO, " +
        "p.piece_ordre_id, p.quantite, pd.prix_ht, (p.quantite * pd.prix_ht) AS piece_cost, " +
        "(o.NbHeuresMO * 3000) AS labor_cost " +
        "FROM ordres_reparation o " +
        "JOIN appareils a ON o.appareil_id = a.appareil_id " +
        "JOIN clients c ON a.client_id = c.client_id " +
        "JOIN pieces_a_changer p ON o.ordre_id = p.ordre_id " +
        "JOIN pieces_detachees pd ON p.piece_id = pd.piece_id " +
        "WHERE a.appareil_id = ? AND c.client_id = ?";

    private static final String CHECK_ORDER_EXISTS_QUERY =
        "SELECT COUNT(*) AS order_count " +
        "FROM ordres_reparation o " +
        "JOIN appareils a ON o.appareil_id = a.appareil_id " +
        "WHERE a.appareil_id = ?";

    public void loadFacture(int appareilId, int clientId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean factureGenerated = false; 
            try (PreparedStatement checkStatement = connection.prepareStatement(CHECK_ORDER_EXISTS_QUERY)) {
                checkStatement.setInt(1, appareilId);
                ResultSet checkResultSet = checkStatement.executeQuery();
                if (checkResultSet.next() && checkResultSet.getInt("order_count") == 0) {
                    factureTextArea.setText("Your appliance is still in the waiting list.");
                    return;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(GENERATE_FACTURE_QUERY)) {
                statement.setInt(1, appareilId);
                statement.setInt(2, clientId);
                ResultSet resultSet = statement.executeQuery();

                StringBuilder factureContent = new StringBuilder();
                factureContent.append("Facture Details:\n\n");

                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                double totalPieceCost = 0.0; // Total cost of all pieces for the appareil
                double totalLaborCost = 0.0; // Total labor cost for the appareil

                while (resultSet.next()) {
                    if (!factureGenerated) {
                        int ordreId = resultSet.getInt("ordre_id");
                        String clientName = resultSet.getString("client_name");
                        String appareilDescription = resultSet.getString("appareil_description");

                        factureContent.append(String.format("Order ID: %d\n", ordreId));
                        factureContent.append(String.format("Client Name: %s\n", clientName));
                        factureContent.append(String.format("Appliance: %s\n", appareilDescription));
                        factureContent.append("--------------------------------------------------\n");

                        factureGenerated = true; 
                    }

                    int quantity = resultSet.getInt("quantite");
                    double pieceCost = resultSet.getDouble("piece_cost");
                    double laborCost = resultSet.getDouble("labor_cost");

                    totalPieceCost += pieceCost; 
                    totalLaborCost += laborCost; 

                    // Append piece details to factureContent
                    factureContent.append(String.format("Quantity: %d\n", quantity));
                    factureContent.append(String.format("Piece Cost: $%s\n", decimalFormat.format(pieceCost)));
                    factureContent.append(String.format("Labor Cost: $%s\n", decimalFormat.format(laborCost)));
                    factureContent.append("--------------------------------------------------\n");
                }

                // Append total costs to factureContent
                factureContent.append(String.format("Total Piece Cost: $%s\n", decimalFormat.format(totalPieceCost)));
                factureContent.append(String.format("Total Labor Cost: $%s\n", decimalFormat.format(totalLaborCost)));

                factureTextArea.setText(factureContent.toString());
            }
        } catch (SQLException e) {
            showAlert("Error", "An error occurred while generating the facture: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void handleCloseButton() {
        Stage stage = (Stage) factureTextArea.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}