package controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import java.net.URL;
import java.util.ResourceBundle;
import application.Main;

public class DashboardController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            showOrdreReparationView(); 
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    public void showCategoryView() throws IOException {
        BorderPane pane = FXMLLoader.load(getClass().getResource("/views/category/CategoryView.fxml"));
        contentArea.getChildren().setAll(pane);
    }

    public void showReplacementPartView() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/views/piecesdetachees/PiecesDetacheesView.fxml"));
        contentArea.getChildren().setAll(pane);
    }

    public void showOrdreReparationView() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/views/OrdreReparation/OrdreReparation.fxml"));
        contentArea.getChildren().setAll(pane);
    }    

    public void showPieceAChangerView() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/views/PieceAChanger/PieceAChangerView.fxml"));
        contentArea.getChildren().setAll(pane);
    }

    @FXML
    private void handleLogout() {
        Main.showLoginRegisterView();
    }
}
