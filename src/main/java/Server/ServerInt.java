package Server;

import Client.ClientInt;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInt extends Remote {
//    Boolean storeMessage(String message) throws RemoteException;
    void registerClient(ClientInt client) throws RemoteException;
    void sendMessage(String message) throws RemoteException;
}