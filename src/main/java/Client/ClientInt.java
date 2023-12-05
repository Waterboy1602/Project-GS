package Client;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.rmi.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface ClientInt extends Remote {
    void createKeySaltTagId(String otherClientName) throws RemoteException, NoSuchAlgorithmException;
    void addReceivingFrom(String name, SecretKey key, byte[] salt, int[] tagId) throws RemoteException;
    String getName() throws RemoteException;
    int[] getListOfTagIdSending(String name) throws RemoteException;
    SecretKey getListOfKeysSending(String name) throws RemoteException;
    byte[] getListOfSaltSending(String name) throws RemoteException;
    List<String> getSendingClients() throws RemoteException;
    void receiveMessage() throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException;
}