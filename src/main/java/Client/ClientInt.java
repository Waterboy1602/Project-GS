package Client;


import java.rmi.*;

public interface ClientInt extends Remote {
//    String send() throws RemoteException;
    void receiveMessage(String message) throws RemoteException;

}