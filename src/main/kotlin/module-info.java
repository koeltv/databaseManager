module com.koeltv.databasemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.sql;
    requires kotlin.stdlib.jdk7;


    opens com.koeltv.databasemanager to javafx.fxml;
    exports com.koeltv.databasemanager;
}