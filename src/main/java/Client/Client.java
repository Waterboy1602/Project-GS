package Client;

import Server.ServerInt;

import javax.crypto.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Client implements ClientInt, Serializable {
    private static final long serialVersionUID = -4257989786795911819L;
    private final int N = 9999;

//    private final ServerInt server;
    private HashMap<String, SecretKey> listOfKeysSending;
    private HashMap<String, int[]> listOfTagIdSending;
    private List<Client> receivingFromClients;
    private List<String> sendingToClients;
    private String name;
    private ServerInt server;
    private Cipher cipher;
    private String keyAlgorithm = "AES";
    private int keySize = 256;
    private int iterations = 10;
    private SecretKey symmetricKey;
    private int idx;
    private int tag;
    private Random random = new Random(9);

    Client(String name, ServerInt server) throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        this.name = name;
        this.server = server;
        this.receivingFromClients = new ArrayList<>();
        this.sendingToClients =  new ArrayList<>();
        this.listOfKeysSending = new HashMap<>();
        this.listOfTagIdSending = new HashMap<>();
    }

    Client(String name, SecretKey symmetricKey, int tag, int idx){
        this.name = name;
//        this.symmetricKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(symmetricKey), 0, DatatypeConverter.parseHexBinary(symmetricKey).length, "AES");
        this.symmetricKey = symmetricKey;
        this.tag = tag;
        this.idx = idx;
    }

    Client(Client client) throws RemoteException {
        this.name = client.getName();
        this.server = client.getServer();
        this.receivingFromClients = client.getReceivingFromClients();
        this.sendingToClients = client.getSendingToClients();
        this.listOfKeysSending = client.getListOfKeysSending();
        this.listOfTagIdSending = client.getListOfTagIdSending();
    }

    private SecretKey createKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(keyAlgorithm);
        keyGen.init(keySize);// Set the key size (in bits)

        SecretKey key = keyGen.generateKey();
        // Print the generated key
        System.out.println("Generated Symmetric Key: " + DatatypeConverter.printHexBinary(key.getEncoded()));
        // Generate a symmetric key
        return key;
    }

    private int[] firstTagId() {
        int[] tagId = new int[2];
        tagId[0] = random.nextInt(100);
        tagId[1] = random.nextInt(100);

        return tagId;
    }

    public void createKeyTagId(String otherClientName) throws NoSuchAlgorithmException {
        listOfKeysSending.put(otherClientName, createKey());
        listOfTagIdSending.put(otherClientName, firstTagId());
        sendingToClients.add(otherClientName);
    }

    public void addReceivingFrom(String name, SecretKey key, int[] tagId){
        Client receivingFromClient = new Client(name, key, tagId[0], tagId[1]);
        receivingFromClients.add(receivingFromClient);
    }

    public void sendMessage(String message) throws RemoteException, IllegalBlockSizeException, UnsupportedEncodingException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        int nextId = random.nextInt(100);
        String nextTag = randomStringGenerator(20);
        message += "||" + Integer.toString(nextId) + "||" + nextTag;
        String encryptedMessage = encryptMessage(message);

//        server.sendMessage(encryptedMessage);
        newSecretKey();
    }

    public void newSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(DatatypeConverter.printHexBinary(symmetricKey.getEncoded()).toCharArray(), null, iterations, keySize);

        // Get instance of SecretKeyFactory for PBKDF2WithHmacSHA256
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        // Derive the key
        byte[] newKey = keyFactory.generateSecret(keySpec).getEncoded();
        symmetricKey = new SecretKeySpec(newKey, 0, newKey.length, "AES");
    }

    public String randomStringGenerator(int length){
        String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomString = "";
        for(int i=0; i < length; i++ ){
            int randomIndex = random.nextInt(characterSet.length());
            randomString += characterSet.charAt(randomIndex);
        }
        return randomString;
    }

    public String encryptMessage(String message) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        cipher = Cipher.getInstance(keyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        cipher.update(message.getBytes());
        byte[] cipherText = cipher.doFinal();
        String encryptedMessage = new String(cipherText, "UTF8");
        return encryptedMessage;
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        new Thread(() -> System.out.println("Ontvangen: " + message)).start();
//        newSecretKey();

//
//        System.out.println("Waarom doet die dit??");
//        System.out.println(message);
    }

    public String getHash(){
        return null;
    }

    public String getName(){
        return name;
    }

    public SecretKey getKey(String name){
        return listOfKeysSending.get(name);
    }

    public int[] getTagId(String name){
        return listOfTagIdSending.get(name);
    }

    public List<String> getSendingClients(){
        return sendingToClients;
    }

    public HashMap<String, SecretKey> getListOfKeysSending() {
        return listOfKeysSending;
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
}
