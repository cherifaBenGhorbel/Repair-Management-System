module fcd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires javafx.graphics;
    opens models to javafx.base;
    opens controllers to javafx.fxml;
    exports application;
    exports controllers;
}
