module com.koeltv.databasemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.sql;
    requires javafaker;
    requires java.desktop;


    opens com.koeltv.databasemanager to javafx.fxml;
    exports com.koeltv.databasemanager;
    exports com.koeltv.databasemanager.database;
    exports com.koeltv.databasemanager.database.component;
    exports com.koeltv.databasemanager.database.parser;
}