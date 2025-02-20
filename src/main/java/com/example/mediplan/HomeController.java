package com.example.mediplan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;


public class HomeController {

    public AnchorPane createApt;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button homeButton;

    @FXML
    private Button addApt;

    @FXML
    private Button submitButton;

    @FXML
    private TextField nameField;

    @FXML
    private TextField cvField;

    @FXML
    private VBox step1;

    @FXML
    private VBox step2;

    @FXML
    private GridPane planing;

    @FXML
    private TextField rowField;

    @FXML
    private TextField colField;

    private Connection connection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/planning", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HomeController instance;

    // Constructeur ou méthode d'initialisation
    public HomeController() {
        instance = this;
    }

    // Permet d'obtenir l'instance du contrôleur
    public static HomeController getInstance() {
        return instance;
    }



    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (validateLogin(email, password)) {
            loadMainView();
        } else {
            showAlert("Erreur", "Email ou mot de passe incorrect !");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateLogin(String mail, String password) {
        String query = "SELECT * FROM user WHERE mail = ? AND password = ?";

        try (Connection conn = connection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, mail);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadMainView() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/MainView.fxml"));
            AnchorPane root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleHome(ActionEvent event) {
        try {
            Stage stage = (Stage) homeButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/LoginView.fxml"));
            AnchorPane root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openFormulaire(ActionEvent event) {
        try {
            Stage stage = (Stage) addApt.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/AddPatient.fxml"));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle("Ajouter un rendez-vous");
            stage.setScene(scene);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToStep2(ActionEvent event) {
        String name = this.nameField.getText();
        String cv = this.cvField.getText();

        if (name.isEmpty() || cv.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/planning", "root", "")) {
            String query = "INSERT INTO rendezvous (name, cv) VALUES (?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, cv);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    step1.setVisible(false);
                    step2.setVisible(true);
                } else {
                    showAlert("Erreur", "Échec de l'insertion.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void submitApt(ActionEvent event) {
        String patientName = nameField.getText();

        // Vérification des champs de ligne et de colonne
        try {
            int row = Integer.parseInt(rowField.getText());
            int col = Integer.parseInt(colField.getText());

            // Ajouter au GridPane de la MainView
            HomeController.getInstance().addPatientToGrid(row, col, patientName);

            // Fermer la fenêtre actuelle après validation
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des nombres valides pour la ligne et la colonne !");
        }
    }


    public void addPatientToGrid(int row, int col, String patientName) {
        if (planing == null) {
            System.out.println("GridPane non initialisé !");
            return;
        }

        // Vérifier si la cellule est déjà occupée
        boolean isCellOccupied = false;
        for (javafx.scene.Node child : planing.getChildren()) {
            Integer cellRow = GridPane.getRowIndex(child);
            Integer cellCol = GridPane.getColumnIndex(child);
            if (cellRow != null && cellRow == row && cellCol != null && cellCol == col) {
                isCellOccupied = true;
                break;
            }
        }

        if (!isCellOccupied) {
            Label patientLabel = new Label(patientName);
            planing.add(patientLabel, col, row);
        } else {
            System.out.println("La cellule est déjà occupée !");
        }
    }
}
