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

    public void createUser(String email, String password) {

        String hashedPassword = hashPassword(password);

        String query = "INSERT INTO user (mail, password) VALUES (?, ?)";

        try (Connection conn = connection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);

            pstmt.executeUpdate();
            System.out.println("User created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static void main(String[] args) {
        Create_User_Emergency createUserEmergency = new Create_User_Emergency();

        String email = "Tesline";
        String password = "Admin7894$$";

        createUserEmergency.createUser(email, password);
    }
}