package Server;

import Client.ClientInt;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server extends UnicastRemoteObject implements ServerInt, Serializable {
    private static final long serialVersionUID = 4317596311460405788L;

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

    public synchronized boolean registerClient(ClientInt client) throws RemoteException {
        for(ClientInt cl : clients){
            if(cl.getName().equals(client.getName())){
                return false;
            }
        }
        this.clients.add(client);
        client.receiveMessage("Welcome, you're registered");
        System.out.println("Added client");
        return true;
    }

    public synchronized ClientInt addConnection(ClientInt client, String nameOfOtherClient) throws RemoteException, NoSuchAlgorithmException {
        ClientInt otherClient = null;

        if(client.getSendingClients().contains(nameOfOtherClient)){
            return null;
        }
        for(ClientInt cl : clients){
            if(cl.getName().equals(nameOfOtherClient)){
                otherClient = cl;
            }
        }
        if(otherClient == null){
            return null;
        } else {
            client.createKeyTagId(otherClient.getName());
            SecretKey key = client.getKey(otherClient.getName());
            int[] tagId = client.getTagId(otherClient.getName());
            otherClient.addReceivingFrom(client.getName(), key, tagId);
            return client;
        }
    }

    public synchronized List<String> getSendingClients(ClientInt client) throws RemoteException {
        return client.getSendingClients();
    }

    public synchronized void sendMessage(ClientInt client, String message) throws RemoteException {

    }
}
