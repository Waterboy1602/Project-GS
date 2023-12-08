package Client;
import Server.ServerInt;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;


public class RunGUIClient extends Application {
    public TextField nameTextField;
    public TextField otherClientTextField;
    public Text failedToRegister;
    public Text failedToAdd;
    public Text noClientSelected;

    public ChoiceBox otherClients;
    public TextField messageTextField;
    public TextArea messagesTextArea;
    public Button confirmButton;
    public Button sendButton;
    Thread thread;
    Client client;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ChatBox.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setTitle("Client");
    }

    public void pushRegisterButton(ActionEvent event) throws NoSuchPaddingException, NoSuchAlgorithmException, RemoteException, InvalidKeyException, NotBoundException {
        if(!nameTextField.getText().isEmpty()){
            client = new Client(nameTextField.getText(), messagesTextArea, otherClients);
            if(!client.registerClient()){
                failedToRegister.setText("Failed to register");
            } else {
                failedToRegister.setFill(Color.GREEN);
                failedToRegister.setText("Successful");
                confirmButton.setDisable(true);
                nameTextField.setEditable(false);

                // Start thread that receives messages
                thread = new Thread(client);
                thread.start();

                messagesTextArea.setStyle("-fx-text-fill: black;") ;

                otherClients.getItems().add("*Everybody*");
                otherClients.getSelectionModel().select("*Everybody*");
            }
        } else {
            failedToRegister.setText("Fill in a name");
        }

    }

    public void addClient(ActionEvent event) throws NoSuchAlgorithmException, RemoteException {
        if (otherClientTextField.getText().isEmpty()) {
            failedToAdd.setText("Fill in a name");
        } else if(otherClientTextField.getText().equals(nameTextField.getText())) {
            failedToAdd.setText("Can't connect with yourself");
        } else {
            Boolean succeeded = client.addConnection(otherClientTextField.getText());
            if(!succeeded){
                failedToAdd.setText("Failed to add user");
            } else {
                otherClients.getItems().add(otherClientTextField.getText());
                otherClientTextField.setText("");
                failedToAdd.setFill(Color.GREEN);
                failedToAdd.setText("Successful");
                sendButton.setDefaultButton(true);
            }
        }
    }

    public void sendMessage(ActionEvent event) throws IllegalBlockSizeException, UnsupportedEncodingException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, RemoteException, InvalidKeyException {
        if(otherClients.getValue() == null){
            noClientSelected.setText("Select a client");
        } else if(messageTextField.getText().isEmpty()) {
            noClientSelected.setText("Write a message");
        } else if(otherClients.getValue().equals("*Everybody*")) {
            noClientSelected.setText("Can't send to *everybody*");
        } else {
            client.sendMessage((String) otherClients.getValue(), messageTextField.getText());
            messageTextField.setText("");
            noClientSelected.setText("");
        }
    }
}
