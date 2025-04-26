module com.example.btl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.application.test to javafx.fxml;
    exports com.application.test;
    exports com.application.test.Controller;
    opens com.application.test.Controller to javafx.fxml;
    exports com.application.test.Model;
    opens com.application.test.Model to javafx.fxml;
}