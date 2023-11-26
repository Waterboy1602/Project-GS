package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import Server.ServerInt;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RunClient {
    public static void main(String[] args) throws IOException, NotBoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        new RunClient();

        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        ServerInt server = (ServerInt) registry.lookup("Server");
        Scanner scanner = new Scanner(System.in);

        Client client = null;
        Boolean registered = false;
        while(!registered){
            System.out.print("Naam: ");
            String name = scanner.nextLine();
            client = new Client(name, server);
            if(!server.registerClient(client)){
                System.out.println("Er bestaat reeds een gebruiker met deze naam. Gebruik een unieke naam");
            } else {
                registered = true;
            }
        }
        System.out.println("Client running...");

        // Client toevoegen
        System.out.print("Naam andere client: ");
        String nameOtherClient = scanner.nextLine();
        Client clientBackup = new Client(client);
        client = (Client) server.addConnection(client, nameOtherClient);
        if(client == null){
            System.out.println("Connectie opzetten met gebruiker is mislukt");
            client = new Client(clientBackup);
        } else {
            System.out.println("Succesvol verbonden met gebruiker " + nameOtherClient);
        }


        while(true){
            System.out.println("1. Client toevoegen \n" +
                    "2. Bericht verzenden \n" +
                    "0. Afsluiten");
            System.out.print("Keuze: ");
            String choice = scanner.nextLine();

            switch(choice){
                case "1":
                    System.out.print("Naam andere client: ");
                    nameOtherClient = scanner.nextLine();
                    clientBackup = new Client(client);
                    client = (Client) server.addConnection(client, nameOtherClient);
                    if(client == null){
                        System.out.println("Connectie opzetten met gebruiker is mislukt");
                        client = new Client(clientBackup);
                    } else {
                        System.out.println("Succesvol verbonden met gebruiker " + nameOtherClient);
                    }

                case "2":
                    if(server.getSendingClients(client).isEmpty()){
                        System.out.println("Nog geen verbindingen opgesteld met andere clients");
                        break;
                    } else {
                        int i = 1;
                        for(String name : server.getSendingClients(client)){
                            System.out.println(i + ". " + name);
                            i++;
                        }
                        System.out.println("Kies gebruiker waarmee je wilt sturen (nummer)");
                        System.out.print("Keuze: ");
                        String numberOfClient = scanner.nextLine();
                        String clientToSendTo = server.getSendingClients(client).get(Integer.parseInt(numberOfClient)-1);
                        System.out.println("Aan het sturen met " + clientToSendTo + "...");
                        System.out.print("Bericht: ");
                        String message = scanner.nextLine();
                        if(message.equals("\u001b")){
                            break;
                        } else {
                            server.sendMessage(client, message);
                        }
                    }
                case "0":
                    System.exit(0);
            }
        }
    }
}