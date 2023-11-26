package Client;


import javax.crypto.SecretKey;
import java.rmi.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ClientInt extends Remote {
//    String send() throws RemoteException;
    void receiveMessage(String message) throws RemoteException;
    void createKeyTagId(String otherClientName) throws RemoteException, NoSuchAlgorithmException;
    void addReceivingFrom(String name, SecretKey key, int[] tagId) throws RemoteException;
    String getName() throws RemoteException;
    SecretKey getKey(String name) throws RemoteException;
    int[] getTagId(String name) throws RemoteException;
    List<String> getSendingClients() throws RemoteException;
}