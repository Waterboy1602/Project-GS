package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import Server.ServerInt;

public class RunClient {
    public static void main(String[] args) throws IOException, NotBoundException, NoSuchAlgorithmException {
        new RunClient();

        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        ServerInt server = (ServerInt) registry.lookup("Server");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Naam: ");
        String name = scanner.nextLine();

        Client client = new Client(name, server);
        server.registerClient(client);

        System.out.println("Client running...");

        System.out.println("Naam andere client: ");
        // Read a line of text from the console
        String nameOtherClient = scanner.nextLine();
        System.out.println("Maak connectie met andere client. Deel encryptie code: ");
        String privateKey = scanner.nextLine();
        client.addConnection(new Client(nameOtherClient, privateKey));


        // Read a line of text from the console
        String privateKeyOtherClient = scanner.nextLine();
        String message = "";
        while(!message.equals("quit")){
            System.out.println("Message: ");
            // Read a line of text from the console
            message = scanner.nextLine();
            // Display the entered text
            client.sendMessage(message);
            System.out.println("You entered: " + message);
        }
    }
}