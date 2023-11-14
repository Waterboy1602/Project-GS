package Client;

import Server.ServerInt;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Client implements ClientInt, Serializable {
    private static final long serialVersionUID = 1L;

//    private final ServerInt server;
    private SecretKey symmetricKey;
    private List<ClientInt> connectedClients;
    private String name;
    private ServerInt server;

    Client(String name, ServerInt server) throws RemoteException, NoSuchAlgorithmException {
        this.name = name;
        this.server = server;
        this.connectedClients = new ArrayList<ClientInt>();
        createKeys();
    }

    Client(String name, String symmetricKey){
        this.name = name;
        this.symmetricKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(symmetricKey), 0, DatatypeConverter.parseHexBinary(symmetricKey).length, "AES");
    }

    private void createKeys() throws NoSuchAlgorithmException {
        String algorithm = "AES";
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);

        int keySize = 128; // You can choose other key sizes based on your security requirements
        keyGen.init(keySize);// Set the key size (in bits)

        // Generate a symmetric key
        this.symmetricKey = keyGen.generateKey();

        // Print the generated key
        System.out.println("Generated Symmetric Key: " + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
    }

    public void addConnection(Client otherClient){
        connectedClients.add(otherClient);
    }

    public void sendMessage(String message) throws RemoteException {
        String encryptedMessage = message;
        

        server.sendMessage(encryptedMessage);
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        new Thread(() -> System.out.println("Ontvangen: " + message)).start();
//
//        System.out.println("Waarom doet die dit??");
//        System.out.println(message);
    }

    public String getHash(){
        return null;
    }

    public Boolean connect(String hashOfUser){
        return true;
    }
}
