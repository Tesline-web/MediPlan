module com.example.mediplan {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.example.mediplan to javafx.fxml;
    exports com.example.mediplan;
}