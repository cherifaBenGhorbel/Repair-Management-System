package controllers;

import javafx.util.Duration;
import session.Session;
import application.Main;
import connection.DatabaseConnection;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CombinedController {

	@FXML
    private AnchorPane Create_form;

    @FXML
    private Button create_Btn;

    @FXML
    private TextField create_email;

    @FXML
    private PasswordField create_password;
    
    @FXML
    private TextField create_emailAdresse;

    @FXML
    private TextField create_emailName;

    @FXML
    private TextField create_emailTel;

    @FXML
    private TextField create_username;

    @FXML
    private Button login_Btn;

    @FXML
    private TextField login_email;

    @FXML
    private AnchorPane login_form;

    @FXML
    private PasswordField login_password;

    @FXML
    private Button sideCreate_Btn;

    @FXML
    private Button side_alreadyHave;

    @FXML
    private AnchorPane side_form;
    
    
/*****************************************************login method****************************/
    private void handleLogin(String username, String password) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (!usernameExists(connection, username)) {
                showAlert("Login Failed", "Invalid username.");
                return;
            }
            
            // Validate the username and password
            String query = "SELECT u.user_id, u.user_type, c.client_id FROM Users u LEFT JOIN Clients c ON u.user_id = c.user_id WHERE u.username = ? AND u.password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String userType = resultSet.getString("user_type");
                int userId = resultSet.getInt("user_id");
                int clientId = resultSet.getInt("client_id");


                Session.setLoggedInUserId(userId);
                if (!userType.equals("admin")) {
                    Session.setLoggedInClientId(clientId);
                }

                // Navigate to the appropriate dashboard
                if (userType.equals("admin")) {
                    Main.showDashboard();
                } else {
                    Main.showInsertOrderView();
                }
            } else {
                showAlert("Login Failed", "Invalid password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during login.");
        }
    }

/*************************** login handle ****************************************************/
    @FXML
    private void handleLoginAction(ActionEvent event) {
        String username = login_email.getText();
        String password = login_password.getText(); 
        handleLogin(username, password);
    }
    
/**************************************************register method ************************************/

    @FXML
    private void handleRegister() {
        String username = create_email.getText();
        String password = create_password.getText();
        String name = create_emailName.getText();
        String address = create_emailAdresse.getText();
        String phone = create_emailTel.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Insert user into Users table
            String userQuery = "INSERT INTO Users (username, password, user_type) VALUES (?, ?, 'client')";
            PreparedStatement userStatement = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            userStatement.setString(1, username);
            userStatement.setString(2, password);
            userStatement.executeUpdate();
            
            // Get the generated user_id
            ResultSet generatedKeys = userStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                // Insert client into Clients table
                String clientQuery = "INSERT INTO Clients (user_id, nom, adresse, numero_telephone) VALUES (?, ?, ?, ?)";
                PreparedStatement clientStatement = connection.prepareStatement(clientQuery);
                clientStatement.setInt(1, userId);
                clientStatement.setString(2, name);
                clientStatement.setString(3, address);
                clientStatement.setString(4, phone);
                clientStatement.executeUpdate();

                showAlert("Registration Successful", "Account created successfully.");
                Main.showLoginRegisterView();
            } else {
                showAlert("Registration Failed", "Failed to retrieve user ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during registration.");
        }
    }

    
    /*
    private int getUserIdByUsername(String username) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT user_id FROM Users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; 
    }
    */
    
/******************************both register and switch method *****************************/    
    @FXML
    private void handleRegisterAndSwitchForm(ActionEvent event) {
        if ("Login".equals(sideCreate_Btn.getText())) {
            switchForm(event);
        } else {
            if (validateForm()) {
                handleRegister();
            }
        }
    }
/***************************************  alert ***************************************************************/
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
/*****************************************************************switch method**********************************/
    public void switchForm(ActionEvent event) {
    	TranslateTransition slider = new TranslateTransition();
    	if(event.getSource()==sideCreate_Btn) {
    		slider.setNode(side_form);
    		slider.setToX(410);
    		slider.setDuration(Duration.seconds(0.5));
    		slider.setOnFinished((ActionEvent e)->{
    			side_alreadyHave.setVisible(true);
    			sideCreate_Btn.setVisible(false);
    		});
    		slider.play();
    	}
    	else if(event.getSource()==side_alreadyHave) {
    		slider.setNode(side_form);
    		slider.setToX(0);
    		slider.setDuration(Duration.seconds(0.5));
    		slider.setOnFinished((ActionEvent e)->{
    			side_alreadyHave.setVisible(false);
    			sideCreate_Btn.setVisible(true);
    		});
    		slider.play();
    	}
    }
    
/************************************************************* check ******************************************/
    private boolean usernameExists(Connection connection, String username) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM Users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt("count");
        return count > 0;
    }
    
/****************************** validation for the registeration form **************************************/
    private boolean validateForm() {
        boolean isValid = true;

        String username = create_email.getText();
        String password = create_password.getText();
        String name = create_emailName.getText();
        String address = create_emailAdresse.getText();
        String phone = create_emailTel.getText();

        if (username.isEmpty() && password.isEmpty() && name.isEmpty() && address.isEmpty() && phone.isEmpty()) {
            showAlertWarning("Empty Fields", "Please fill in all fields.");
            return false; 
        }
        
        if (username.length() < 3 || username.isEmpty()) {
        	showAlertWarning("Invalid Username", "Username must be at least 3 characters long.");
            isValid = false;
        }

        if (password.length() < 3 || password.isEmpty()) {
        	showAlertWarning("Invalid Password", "Password must be at least 3 characters long.");
            isValid = false;
        }

        if (name.isEmpty() || name.length() < 3) {
        	showAlertWarning("Invalid Name", "Name must be at least 3 characters long");
            isValid = false;
        }

        if (address.isEmpty() || address.length() < 3) {
        	showAlertWarning("Invalid Adress", "Adress must be at least 3 characters long");
            isValid = false;
        }

        if (phone.length() != 8 || !phone.matches("\\d+")) {
        	showAlertWarning("Invalid Phone Number", "Phone number must be 8 digits long and contain only numbers.");
            isValid = false;
        }

        return isValid;
    }
    
/***************** alert for the fields validation **********************/
    private void showAlertWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
