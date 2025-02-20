package com.bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BddManager {

    public Connection connection(){
        Connection conn = null;
        try {
            return conn = DriverManager.getConnection("jdbc:mysql://localhost/planning","root","");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
