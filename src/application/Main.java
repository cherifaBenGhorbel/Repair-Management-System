package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            Main.primaryStage = primaryStage;
            showLoginRegisterView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void showLoginRegisterView() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/views/Combined.fxml"));
            Scene scene = new Scene(root, 820, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login and Register");
            primaryStage.setWidth(820); 
            primaryStage.setHeight(600); 
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/dashboard/AdminDashboard.fxml"));
            Scene scene = new Scene(loader.load(), 820, 600); 
            primaryStage.setScene(scene);
            primaryStage.setTitle("Admin Dashboard");
            primaryStage.setWidth(820); 
            primaryStage.setHeight(600); 
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void showInsertOrderView() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/views/appareils/InsertOrder.fxml"));
            Scene scene = new Scene(root, 820, 700);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Insert Order");
            primaryStage.setWidth(820); 
            primaryStage.setHeight(700); 
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void showCategoryView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/category/CategoryView.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600); 
            primaryStage.setScene(scene);
            primaryStage.setTitle("Manage Categories");
            primaryStage.setWidth(800); 
            primaryStage.setHeight(600); 
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void showReplacementPartView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/piecesdetachees/PiecesDetacheesView.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600); 
            primaryStage.setScene(scene);
            primaryStage.setTitle("Manage New Replacement Parts");
            primaryStage.setWidth(800); 
            primaryStage.setHeight(600); 
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void showOrdreReparationView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/OrdreReparation/OrdreReparation.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600); 
            primaryStage.setScene(scene);
            primaryStage.setTitle("Manage Ordre Reparation");
            primaryStage.setWidth(800); 
            primaryStage.setHeight(600); 
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}


    public static void main(String[] args) {
        launch(args);
    }

	
}

