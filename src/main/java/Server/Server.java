package Server;

import Client.ClientInt;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class Server extends UnicastRemoteObject implements ServerInt, Serializable {
    private static final long serialVersionUID = 1L;

    private int n = 15;
    private HashMap<String, String>[] bulletinBoard;
    private ArrayList<ClientInt> clients;

    Server() throws RemoteException {
        super();
        bulletinBoard = new HashMap[n];
        clients = new ArrayList<>();
    }

//    public boolean storeMessage(String message){
//        return true;
//    }

    public synchronized void registerClient(ClientInt client) throws RemoteException {
        this.clients.add(client);
        client.receiveMessage("Welcome, you're registered");
        System.out.println("Added client");
    }

    public synchronized void sendMessage(String message) throws RemoteException {

    }
}
