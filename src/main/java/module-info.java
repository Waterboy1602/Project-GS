module com.example.guiprojectgs {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.rmi;
    requires java.xml.bind;

    opens com.example.guiprojectgs to javafx.fxml;
    exports com.example.guiprojectgs;
}