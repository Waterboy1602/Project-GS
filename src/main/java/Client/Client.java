package Client;

import Server.ServerInt;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

import javax.crypto.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Client extends UnicastRemoteObject implements ClientInt, Serializable, Runnable {
    private static final long serialVersionUID = -4257989786795911819L;
    private final int N = 9999;
    private HashMap<String, SecretKey> listOfKeysSending;
    private HashMap<String, int[]> listOfTagIdSending;
    private HashMap<String, byte[]> listOfSaltSending;
    private HashMap<String, SecretKey> nextListOfKeysSending;
    private HashMap<String, int[]> nextListOfTagIdSending;

    private List<Client> receivingFromClients;
    private List<String> sendingToClients;

    private List<String> receivedMessages;
    private String name;
    private ServerInt server;
    private Cipher cipher;
    private String keyAlgorithm = "AES";
    private int keySize = 256;
    private int iterations = 10;
    private SecretKey symmetricKey;
    private byte[] salt;
    private int id;
    private int tag;
    private Random random = new Random();

    private TextArea messagesTextArea;
    private ChoiceBox otherClients;

    private Registry registry;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    Client(String name, ServerInt server) throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        this.name = name;
        this.server = server;
        this.receivingFromClients = new ArrayList<>();
        this.sendingToClients =  new ArrayList<>();
        this.listOfKeysSending = new HashMap<>();
        this.listOfSaltSending = new HashMap<>();
        this.listOfTagIdSending = new HashMap<>();
        this.nextListOfKeysSending = new HashMap<>();
        this.nextListOfTagIdSending = new HashMap<>();
        this.receivedMessages = new ArrayList<>();
    }

    Client(String name, TextArea messagesTextArea, ChoiceBox otherClients) throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        this.name = name;
        this.receivingFromClients = new ArrayList<>();
        this.sendingToClients =  new ArrayList<>();
        this.listOfKeysSending = new HashMap<>();
        this.listOfSaltSending = new HashMap<>();
        this.listOfTagIdSending = new HashMap<>();
        this.nextListOfKeysSending = new HashMap<>();
        this.nextListOfTagIdSending = new HashMap<>();
        this.receivedMessages = new ArrayList<>();
        this.messagesTextArea = messagesTextArea;
        this.otherClients = otherClients;
    }

    Client(String name, SecretKey symmetricKey, byte[] salt, int tag, int id) throws RemoteException{
        this.name = name;
        this.symmetricKey = symmetricKey;
        this.salt = salt;
        this.tag = tag;
        this.id = id;
    }

    public boolean registerClient() throws RemoteException, NotBoundException {
        this.registry = LocateRegistry.getRegistry("localhost", 1099);
        this.server = (ServerInt) registry.lookup("server");
        if(server.registerClient(this)){
            return true;
        } else {
            return false;
        }

    }

    public boolean addConnection(String name) throws NoSuchAlgorithmException, RemoteException {
        if(server.addConnection(this, name)){
            return true;
        } else {
            return false;
        }
    }

    public SecretKey createKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(keyAlgorithm);
        keyGen.init(keySize);

        SecretKey key = keyGen.generateKey();
//        System.out.println("Generated Symmetric Key: " + DatatypeConverter.printHexBinary(key.getEncoded()));
        return key;
    }

    public int[] createTagId() {
        int[] tagId = new int[2];
        tagId[0] = random.nextInt(N);
        tagId[1] = random.nextInt(N);

        return tagId;
    }

    // One time shared salt
    public byte[] createSalt(){
        byte[] salt = new byte[100];
        random.nextBytes(salt);
        return salt;
    }

    public void createKeySaltTagId(String otherClientName) throws NoSuchAlgorithmException {
        listOfKeysSending.put(otherClientName, createKey());
        listOfSaltSending.put(otherClientName, createSalt());
        listOfTagIdSending.put(otherClientName, createTagId());
        sendingToClients.add(otherClientName);
    }

    public void addReceivingFrom(String name, SecretKey key, byte[] salt, int[] tagId) throws RemoteException {
        Client receivingFromClient = new Client(name, key, salt, tagId[0], tagId[1]);
        receivingFromClients.add(receivingFromClient);
    }

    public void sendMessage(String clientToSendTo, String message) throws RemoteException, IllegalBlockSizeException, UnsupportedEncodingException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        receivedMessages.add("*YOU*||" + clientToSendTo + "||(" + dateFormat.format(timestamp) + "): " + message);

        nextListOfTagIdSending.put(clientToSendTo, createTagId());
        message += "||" + nextListOfTagIdSending.get(clientToSendTo)[1] + "||" + nextListOfTagIdSending.get(clientToSendTo)[0];

//        System.out.println("Msg: " + message);
        SecretKey secretKeyClientToSendTo = listOfKeysSending.get(clientToSendTo);
        byte[] salt = listOfSaltSending.get(clientToSendTo);
        String encryptedMessage = encryptMessage(message, secretKeyClientToSendTo);
//        System.out.println("Encrypted Msg: " + encryptedMessage);

        int tag = listOfTagIdSending.get(clientToSendTo)[0];
        int id = listOfTagIdSending.get(clientToSendTo)[1];
        server.sendMessage(tag, id, encryptedMessage);

        nextListOfKeysSending.put(clientToSendTo, newSecretKey(secretKeyClientToSendTo, salt));
        swapKeyTagId(clientToSendTo);
    }


    public SecretKey newSecretKey(SecretKey secretKeyClientToSendTo, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(DatatypeConverter.printHexBinary(secretKeyClientToSendTo.getEncoded()).toCharArray(), salt, iterations, keySize);

        // Get instance of SecretKeyFactory for PBKDF2WithHmacSHA256
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        // Derive the key
        byte[] newKey = keyFactory.generateSecret(keySpec).getEncoded();
        symmetricKey = new SecretKeySpec(newKey, 0, newKey.length, "AES");
        return symmetricKey;
    }

    public void swapKeyTagId(String clientToSendTo) {
        listOfKeysSending.put(clientToSendTo, nextListOfKeysSending.get(clientToSendTo));
        listOfTagIdSending.put(clientToSendTo, nextListOfTagIdSending.get(clientToSendTo));
        nextListOfKeysSending.remove(clientToSendTo);
        nextListOfTagIdSending.remove(clientToSendTo);
    }

    public String encryptMessage(String message, SecretKey secretKey) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        cipher = Cipher.getInstance(keyAlgorithm);
//        System.out.println("Send key: " + DatatypeConverter.printHexBinary(secretKey.getEncoded()));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public void receiveMessage() throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        List<Client> receivingFromClientsCopy = new ArrayList<>(receivingFromClients);
        for(Client client : receivingFromClientsCopy){
            String encryptedMessage = server.receiveMessage(client.getTag(), client.getId());
            if(encryptedMessage != null){
//                System.out.println("EncryptedMsg Receive: " + encryptedMessage);
                cipher = Cipher.getInstance(keyAlgorithm);
//                System.out.println("Key receive: " + DatatypeConverter.printHexBinary(client.getSymmetricKey().getEncoded()));
                cipher.init(Cipher.DECRYPT_MODE, client.getSymmetricKey());
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
                String decryptedMessage = new String(decryptedBytes, UTF_8);
//                System.out.println("DecryptedMsg Receive: " + decryptedMessage);
                String[] decryptedMessageParts = decryptedMessage.split("\\|\\|");
                client.setTag(Integer.parseInt(decryptedMessageParts[2]));
                client.setId(Integer.parseInt(decryptedMessageParts[1]));
                client.setSymmetricKey(newSecretKey(client.getSymmetricKey(), client.getSalt()));
//                System.out.println(client.getName() + ": " + decryptedMessageParts[0]);

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                receivedMessages.add(client.getName() + " (" + dateFormat.format(timestamp) + "): " + decryptedMessageParts[0]);
            }
        }
    }

    public void updateMessagesGUI(){
        String messagesOutput = "";
        if(otherClients.getValue() != null){
            if(otherClients.getValue().equals("*Everybody*")){
                for(String msg : receivedMessages){
                    String[] msgSplit = msg.split(" ");
                    if(msg.split(" ")[0].split("\\|\\|")[0].equals("*YOU*")){
                        messagesOutput += name + " " + msg.split("\\|\\|")[2] + "\n";
                    } else{
                        messagesOutput += msg + "\n";
                    }
                }
            } else {
                String selectedClient = (String) otherClients.getValue();
                for(String msg : receivedMessages){
                    if(msg.split(" ")[0].split("\\|\\|")[0].equals("*YOU*") && msg.split(" ")[0].split("\\|\\|")[1].equals(selectedClient)){
                        messagesOutput += name + " " + msg.split("\\|\\|")[2] + "\n";
                    }
                    if(msg.split(" ")[0].equals(selectedClient)){
                        messagesOutput += msg + "\n";
                    }
                }
            }
            messagesTextArea.setText(messagesOutput);
        }
    }

    public void run() {
        while(true){
            try {
                receiveMessage();
                updateMessagesGUI();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getName(){
        return name;
    }

    public SecretKey getListOfKeysSending(String name){
        return listOfKeysSending.get(name);
    }

    public byte[] getListOfSaltSending(String name){
        return listOfSaltSending.get(name);
    }

    public int[] getListOfTagIdSending(String name){
        return listOfTagIdSending.get(name);
    }

    public List<String> getSendingClients(){
        return sendingToClients;
    }

    public HashMap<String, SecretKey> getListOfKeysSending() {
        return listOfKeysSending;
    }

    public HashMap<String, byte[]>getListOfSaltSending(){
        return listOfSaltSending;
    }

    public HashMap<String, int[]> getListOfTagIdSending() {
        return listOfTagIdSending;
    }

    public List<Client> getReceivingFromClients() {
        return receivingFromClients;
    }

    public List<String> getSendingToClients() {
        return sendingToClients;
    }

    public ServerInt getServer() {
        return server;
    }

    public SecretKey getSymmetricKey(){
        return symmetricKey;
    }

    public void setSymmetricKey(SecretKey symmetricKey){
        this.symmetricKey = symmetricKey;
    }

    public byte[] getSalt(){
        return salt;
    }

    public void setSalt(byte[] salt){
        this.salt = salt;
    }

    public int getTag(){
        return tag;
    }

    public void setTag(int tag){
        this.tag = tag;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public List<String> getReceivedMessages(){
       return receivedMessages;
    }
}
