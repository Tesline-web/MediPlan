package com.bdd;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Create_User_Emergency {

    public Connection connection() {
        Connection conn = null;
        try {
            return conn = DriverManager.getConnection("jdbc:mysql://localhost/planning", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    // Method to hash password and insert a new user into the database
    public void createUser(String email, String password) {
        // Hash the password using bcrypt
        String hashedPassword = hashPassword(password);

        // SQL query to insert the user
        String query = "INSERT INTO user (mail, password) VALUES (?, ?)";

        try (Connection conn = connection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);

            // Execute the insert query
            pstmt.executeUpdate();
            System.out.println("User created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to hash the password using bcrypt
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Main method for testing the user creation
    public static void main(String[] args) {
        Create_User_Emergency createUserEmergency = new Create_User_Emergency();
        // Provide the email and password for the user
        String email = "Tesline";  // The email provided
        String password = "Admin7894$$";  // The password provided

        // Call the createUser method to insert the new user
        createUserEmergency.createUser(email, password);
    }
}
