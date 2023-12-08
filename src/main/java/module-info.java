module Client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.rmi;
    requires java.xml.bind;
    requires java.sql;

    opens Client to javafx.fxml;
    exports Client;
    exports Server;
}