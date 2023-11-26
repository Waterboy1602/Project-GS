package Server;

import Client.ClientInt;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ServerInt extends Remote {
//    Boolean storeMessage(String message) throws RemoteException;
    boolean registerClient(ClientInt client) throws RemoteException;
    ClientInt addConnection(ClientInt client, String nameOfOtherClient) throws RemoteException, NoSuchAlgorithmException;
    List<String> getSendingClients(ClientInt client) throws RemoteException;
    void sendMessage(ClientInt client, String message) throws RemoteException;

}