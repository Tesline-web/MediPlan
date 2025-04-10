package com.example.mediplan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    public AnchorPane addAlerte;
    @FXML
    private DatePicker datePicker;
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
    private TextField noteField;
    @FXML
    private VBox step1;
    @FXML
    private VBox step2;
    @FXML
    private GridPane patientGrid;
    @FXML
    private TextField rowField;
    @FXML
    private TextField colField;
    @FXML
    private Button alerteButton;

    private Connection connection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/planning", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HomeController instance;

    @FXML
    private void initialize() {
        if (patientGrid != null) {
            loadPatients();  // Charger les patients au démarrage de la vue
        } else {
            System.out.println("patientGrid est nul !");
        }
    }

    public HomeController() {
        instance = this;
    }

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

    private boolean validateLogin(String email, String password) {
        String query = "SELECT password FROM user WHERE mail = ?";

        try (Connection conn = connection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Get the hashed password from the database
                String hashedPassword = rs.getString("password");

                // Use bcrypt to check if the entered password matches the stored hash
                return BCrypt.checkpw(password, hashedPassword);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void loadMainView() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/MainView.fxml"));
            AnchorPane root = loader.load();
            HomeController homeController = loader.getController();

            // Créer la scène et la définir
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Accueil");
            stage.show();

            // Une fois que tout est bien affiché, on charge les patients
            Platform.runLater(homeController::loadPatients);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleHome(ActionEvent event) {
        try {
            Stage stage = (Stage) homeButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/MainView.fxml"));
            AnchorPane root = loader.load();

            // Une fois la vue principale chargée, récupérez le contrôleur et rechargez les rendez-vous
            HomeController homeController = loader.getController();
            homeController.loadPatients();

            stage.setScene(new Scene(root));
            stage.setTitle("Main View");
            stage.show();
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
        String note = this.noteField.getText();

        if (name.isEmpty() || cv.isEmpty() || note.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/planning", "root", "")) {
            String query = "INSERT INTO rendezvous (name, cv, note, id_rappel) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, cv);
                pstmt.setString(3, note);
                pstmt.setInt(4, 1); // Remplace "1" par la vraie valeur de id_rappel

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

    public void submitApt(ActionEvent event) {
        String patientName = nameField.getText();
        String cv = cvField.getText();
        String patientNote = noteField.getText();

        try {
            String heure = rowField.getText();  // Heure sous forme de chaîne (par exemple "8" pour 08:00)
            String jour = colField.getText();   // Jour sous forme de chaîne (par exemple "Lundi")

            // Convertir l'heure en ligne (row)
            int row = getRowForHour(heure);
            int col = getColForDay(jour);

            if (row == -1 || col == -1) {
                showAlert("Erreur", "Heure ou jour invalide !");
                return;
            }

            // Enregistrer le patient dans la base de données
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/planning", "root", "")) {
                String query = "INSERT INTO rendezvous (name, cv, note, jour, heure) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, patientName);
                    pstmt.setString(2, cv);
                    pstmt.setString(3, patientNote);
                    pstmt.setString(4, jour);  // Utilise le nom du jour (ex: "Lundi")
                    pstmt.setInt(5, Integer.parseInt(heure));  // Heure en entier (ex: "8" pour 08:00)
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Ouvrir la fenêtre principale (MainView.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/MainView.fxml"));
            Parent root = loader.load();
            HomeController homeController = loader.getController();
            homeController.addPatientToGrid(row, col, patientName, patientNote);

            // Afficher la fenêtre principale avec la mise à jour
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des nombres valides pour la ligne et la colonne !");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue principale.");
        }
    }

    public void loadPatients() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/planning", "root", "")) {
            String query = "SELECT * FROM rendezvous";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String note = rs.getString("note");
                    String jour = rs.getString("jour");
                    int heure = rs.getInt("heure");

                    int row = getRowForHour(String.valueOf(heure));
                    int col = getColForDay(jour);

                    if (row != -1 && col != -1) {
                        System.out.println("Ajout du patient : " + name + " dans la case [" + row + "," + col + "] avec l'heure : " + heure + " et le jour : " + jour);
                        addPatientToGrid(row, col, name, note);
                    } else {
                        System.out.println("Erreur : Heure ou jour invalide pour le patient : " + name);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fonction pour convertir l'heure en ligne
    private int getRowForHour(String hour) {
        switch (hour) {
            case "8":
                return 0;
            case "9":
                return 1;
            case "10":
                return 2;
            case "11":
                return 3;
            case "12":
                return 4;
            case "13":
                return 5;
            case "14":
                return 6;
            case "15":
                return 7;
            case "16":
                return 8;
            case "17":
                return 9;
            case "18":
                return 10;
            default:
                return -1; // Heure invalide
        }
    }

    // Fonction pour convertir le jour en colonne
    private int getColForDay(String day) {
        switch (day.toLowerCase()) {
            case "lundi":
                return 0;
            case "mardi":
                return 1;
            case "mercredi":
                return 2;
            case "jeudi":
                return 3;
            case "vendredi":
                return 4;
            case "samedi":
                return 5;
            case "dimanche":
                return 6;
            default:
                return -1; // Jour invalide
        }
    }

    public void addPatientToGrid(int row, int col, String patientName, String patientNote) {
        if (patientGrid == null) {
            System.out.println("GridPane non initialisé !");
            return;
        }

        boolean isCellOccupied = false;
        for (javafx.scene.Node child : patientGrid.getChildren()) {
            Integer cellRow = GridPane.getRowIndex(child);
            Integer cellCol = GridPane.getColumnIndex(child);
            if (cellRow != null && cellRow == row && cellCol != null && cellCol == col) {
                isCellOccupied = true;
                break;
            }
        }

        if (!isCellOccupied) {
            // Ajouter un label avec le nom et la note du patient
            Label patientLabel = new Label(patientName + "\nNote: " + patientNote);
            patientLabel.setStyle("-fx-background-color: lightblue; -fx-padding: 5px; -fx-border-color: black;");
            patientGrid.add(patientLabel, col, row); // Ajouter le label dans le GridPane
            System.out.println("Patient ajouté dans la case [" + col + ", " + row + "] : " + patientName + " - " + patientNote);
        } else {
            System.out.println("La cellule [" + col + ", " + row + "] est déjà occupée !");
        }
    }

    public void addAlerte(ActionEvent event) {
        try {
            Stage stage = (Stage) alerteButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mediplan/Alerte.fxml"));
            AnchorPane root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Rappel de rendez-vous");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFormulaire(ActionEvent event) {
        // Récupérer les informations du patient à supprimer.
        // Cela peut être fait via un sélectionneur dans l'interface utilisateur, comme un label ou un autre contrôle.

        String patientName = nameField.getText(); // Exemple d'utilisation d'un champ de texte pour obtenir le nom du patient
        String patientNote = noteField.getText(); // Exemple pour récupérer la note
        String patientId = rowField.getText();  // Ou tu peux utiliser un champ caché pour l'ID du rendez-vous (si c'est un formulaire de suppression)

        if (patientName.isEmpty() || patientNote.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner un patient à supprimer !");
            return;
        }

        // Suppression du patient dans la base de données
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/planning", "root", "")) {
            // Supposons que 'id_rendezvous' est un identifiant unique dans la table 'rendezvous'
            String query = "DELETE FROM rendezvous WHERE name = ? AND note = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, patientName);
                pstmt.setString(2, patientNote);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Si la suppression a réussi, on enlève aussi le patient du GridPane
                    deletePatientFromGrid(patientName, patientNote);
                    showAlert("Succès", "Le patient a été supprimé avec succès !");
                } else {
                    showAlert("Erreur", "Échec de la suppression du patient !");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression du patient.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème de connexion à la base de données.");
        }
    }

    private void deletePatientFromGrid(String patientName, String patientNote) {
        if (patientGrid == null) {
            System.out.println("GridPane non initialisé !");
            return;
        }

        // Parcours des enfants du GridPane pour trouver la cellule contenant le nom du patient
        for (javafx.scene.Node child : patientGrid.getChildren()) {
            if (child instanceof Label) {
                Label label = (Label) child;
                // Vérifier si le label correspond au patient (nom et note)
                if (label.getText().contains(patientName) && label.getText().contains(patientNote)) {
                    patientGrid.getChildren().remove(child); // Retirer le label du GridPane
                    System.out.println("Patient " + patientName + " supprimé de la grille.");
                    break;
                }
            }
        }
    }
}
