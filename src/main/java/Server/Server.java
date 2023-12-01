package Server;

import Client.ClientInt;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server extends UnicastRemoteObject implements ServerInt, Serializable {
    private static final long serialVersionUID = 4317596311460405788L;

    private int N = 9999;
    private HashMap<String, String>[] bulletinBoard;
    private ArrayList<ClientInt> clients;

    Server() throws RemoteException {
        super();
        bulletinBoard = new HashMap[N];
        for(int i=0; i<N; i++){
            bulletinBoard[i] = new HashMap<>();
        }
        clients = new ArrayList<>();
    }

    public synchronized boolean registerClient(ClientInt client) throws RemoteException {
        for(ClientInt cl : clients){
            if(cl.getName().equals(client.getName())){
                return false;
            }
        }
        this.clients.add(client);
        System.out.println("Added client");
        return true;
    }

    public synchronized ClientInt addConnection(ClientInt client, String nameOfOtherClient) throws RemoteException, NoSuchAlgorithmException {
        // TODO ~ Probleem dat otherClient niet mee update met effectieve object. Moet gefixt worden
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
            client.createKeySaltTagId(otherClient.getName());
            SecretKey key = client.getListOfKeysSending(otherClient.getName());
            byte[] salt = client.getListOfSaltSending(otherClient.getName());
            int[] tagId = client.getListOfTagIdSending(otherClient.getName());
            otherClient.addReceivingFrom(client.getName(), key, salt, tagId);
            return client;
        }
    }

    private byte[] hashFunction(String tag) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");

        return md.digest(tag.getBytes());
    }

    public synchronized List<String> getSendingClients(ClientInt client) throws RemoteException {
        return client.getSendingClients();
    }

    public synchronized void sendMessage(int _tag, int _id, String encryptedMessage) throws RemoteException, NoSuchAlgorithmException {
        // TODO EncryptedMessage opslaan in Bulletin board ~ testen
        String tag = Integer.toString(_tag);
        int id = _id;
        byte[] hashBytes = hashFunction(tag);
        String hash = DatatypeConverter.printHexBinary(hashBytes);
        System.out.println("Hash send: " + hash);
        bulletinBoard[id].put(hash, encryptedMessage);
    }

    public synchronized String receiveMessage(int tag, int id) throws RemoteException, NoSuchAlgorithmException {
        // TODO EncryptedMessage opslaan in Bulletin board
        byte[] hashBytes = hashFunction(Integer.toString(tag));
        String hash = DatatypeConverter.printHexBinary(hashBytes);
        System.out.println("Hash send: " + hash);
        return bulletinBoard[id].get(hash);
    }
}
