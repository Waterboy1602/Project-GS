package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
        Thread thread;

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

        thread = new Thread(client);
        thread.start();
        System.out.println("Client running...");

        // Client toevoegen
        System.out.print("Naam andere client: ");
        String nameOtherClient = scanner.nextLine();
        Boolean succeeded = server.addConnection(client, nameOtherClient);
        if(!succeeded){
            System.out.println("Connectie opzetten met gebruiker is mislukt");
        } else {
            System.out.println("Succesvol verbonden met gebruiker " + nameOtherClient);
        }
        // Start thread that receives messages
        thread = new Thread(client);
        thread.start();

        while(true){
            System.out.println("1. Client toevoegen \n" +
                    "2. Bericht verzenden \n" +
                    "0. Afsluiten");
            System.out.print("Keuze: ");
            String choice = scanner.nextLine();
            int choiceInt;
            try{
                choiceInt = Integer.parseInt(choice);
                switch(choiceInt){
                    case 1:
                        thread.stop();
                        System.out.print("Naam andere client: ");
                        nameOtherClient = scanner.nextLine();
                        succeeded = server.addConnection(client, nameOtherClient);
                        if(!succeeded){
                            System.out.println("Connectie opzetten met gebruiker is mislukt");
                        } else {
                            System.out.println("Succesvol verbonden met gebruiker " + nameOtherClient);
                        }
                        thread = new Thread(client);
                        thread.start();
                        break;
                    case 2:
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
                            int numberOfClientInt;
                            try{
                                numberOfClientInt = Integer.parseInt(numberOfClient);
                            } catch (NumberFormatException e) {
                                break;
                            }
                            String clientToSendTo = server.getSendingClients(client).get(numberOfClientInt-1);
                            System.out.println("Aan het sturen met " + clientToSendTo + "...");
                            sendMessages(client, clientToSendTo, scanner);
                            break;
                        }
                    case 0:
                        System.exit(0);
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    private static void sendMessages(Client client, String clientToSendTo, Scanner scanner) throws IllegalBlockSizeException, UnsupportedEncodingException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, RemoteException, InvalidKeyException {
        while(true){
            System.out.print("Bericht: ");
            String message = scanner.nextLine();
            if(message.equals("exit")){
                return;
            } else {
                client.sendMessage(clientToSendTo, message);
            }
        }
    }
}