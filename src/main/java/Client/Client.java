package Client;

import Server.ServerInt;

import javax.crypto.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Client implements ClientInt, Serializable, Runnable {
    private static final long serialVersionUID = -4257989786795911819L;
    private final int N = 9999;

//    private final ServerInt server;
    private HashMap<String, SecretKey> listOfKeysSending;
    private HashMap<String, int[]> listOfTagIdSending;
    private HashMap<String, byte[]> listOfSaltSending;
    private HashMap<String, SecretKey> nextListOfKeysSending;
    private HashMap<String, int[]> nextListOfTagIdSending;

    private List<Client> receivingFromClients;
    private List<String> sendingToClients;
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
    private Random random = new Random(9);

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
    }

    Client(String name, SecretKey symmetricKey, byte[] salt, int tag, int id){
        this.name = name;
//        this.symmetricKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(symmetricKey), 0, DatatypeConverter.parseHexBinary(symmetricKey).length, "AES");
        this.symmetricKey = symmetricKey;
        this.salt = salt;
        this.tag = tag;
        this.id = id;
    }

    Client(Client client) throws RemoteException {
        this.name = client.getName();
        this.server = client.getServer();
        this.receivingFromClients = client.getReceivingFromClients();
        this.sendingToClients = client.getSendingToClients();
        this.listOfKeysSending = client.getListOfKeysSending();
        this.listOfTagIdSending = client.getListOfTagIdSending();
        this.listOfSaltSending = client.getListOfSaltSending();
    }

    private synchronized SecretKey createKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(keyAlgorithm);
        keyGen.init(keySize);// Set the key size (in bits)

        SecretKey key = keyGen.generateKey();
        // Print the generated key
        System.out.println("Generated Symmetric Key: " + DatatypeConverter.printHexBinary(key.getEncoded()));
        // Generate a symmetric key
        return key;
    }

    private synchronized int[] createTagId() {
        int[] tagId = new int[2];
        tagId[0] = random.nextInt(N);
        tagId[1] = random.nextInt(N);

        return tagId;
    }

    // One time shared salt
    public synchronized byte[] createSalt(){
        byte[] salt = new byte[100];
        random.nextBytes(salt);
        return salt;
    }

    public synchronized void createKeySaltTagId(String otherClientName) throws NoSuchAlgorithmException {
        listOfKeysSending.put(otherClientName, createKey());
        listOfSaltSending.put(otherClientName, createSalt());
        listOfTagIdSending.put(otherClientName, createTagId());
        sendingToClients.add(otherClientName);
    }

    public synchronized void addReceivingFrom(String name, SecretKey key, byte[] salt, int[] tagId){
        Client receivingFromClient = new Client(name, key, salt, tagId[0], tagId[1]);
        receivingFromClients.add(receivingFromClient);
    }

    public synchronized void sendMessage(String clientToSendTo, String message) throws RemoteException, IllegalBlockSizeException, UnsupportedEncodingException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        nextListOfTagIdSending.put(clientToSendTo, createTagId());
        message += "||" + nextListOfTagIdSending.get(clientToSendTo)[1] + "||" + nextListOfTagIdSending.get(clientToSendTo)[0];

        SecretKey secretKeyClientToSendTo = listOfKeysSending.get(clientToSendTo);
        byte[] salt = listOfSaltSending.get(clientToSendTo);
        String encryptedMessage = encryptMessage(message, secretKeyClientToSendTo);
        System.out.println(encryptedMessage);

        int tag = listOfTagIdSending.get(clientToSendTo)[0];
        int id = listOfTagIdSending.get(clientToSendTo)[1];
        server.sendMessage(tag, id, encryptedMessage);

        nextListOfKeysSending.put(clientToSendTo, newSecretKey(secretKeyClientToSendTo, salt));
        swapKeyTagId(clientToSendTo);
    }


    public synchronized SecretKey newSecretKey(SecretKey secretKeyClientToSendTo, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(DatatypeConverter.printHexBinary(secretKeyClientToSendTo.getEncoded()).toCharArray(), salt, iterations, keySize);

        // Get instance of SecretKeyFactory for PBKDF2WithHmacSHA256
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        // Derive the key
        byte[] newKey = keyFactory.generateSecret(keySpec).getEncoded();
        symmetricKey = new SecretKeySpec(newKey, 0, newKey.length, "AES");
        return symmetricKey;
    }

    public synchronized void swapKeyTagId(String clientToSendTo) {
        listOfKeysSending.put(clientToSendTo, nextListOfKeysSending.get(clientToSendTo));
        listOfTagIdSending.put(clientToSendTo, nextListOfTagIdSending.get(clientToSendTo));
        nextListOfKeysSending.remove(clientToSendTo);
        nextListOfTagIdSending.remove(clientToSendTo);
    }

    public synchronized String randomStringGenerator(int length){
        String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomString = "";
        for(int i=0; i < length; i++ ){
            int randomIndex = random.nextInt(characterSet.length());
            randomString += characterSet.charAt(randomIndex);
        }
        return randomString;
    }

    public synchronized String encryptMessage(String message, SecretKey secretKey) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        cipher = Cipher.getInstance(keyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        cipher.update(message.getBytes());
        byte[] cipherText = cipher.doFinal();
        String encryptedMessage = DatatypeConverter.printHexBinary(cipherText);
        return encryptedMessage;
    }

    public synchronized void receiveMessage() throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        for(Client client : receivingFromClients){
            String encryptedMessage = server.receiveMessage(client.getTag(), client.getId());
            if(encryptedMessage != null){
                cipher = Cipher.getInstance(keyAlgorithm);
                cipher.init(Cipher.DECRYPT_MODE, client.getSymmetricKey());
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
                String decryptedMessage = DatatypeConverter.printHexBinary(decryptedBytes);
                String[] decryptedMessageParts = decryptedMessage.split("||");
                client.setTag(Integer.parseInt(decryptedMessageParts[2]));
                client.setId(Integer.parseInt(decryptedMessageParts[1]));
                client.setSymmetricKey(newSecretKey(client.getSymmetricKey(), client.getSalt()));
                System.out.println(client.getName() + ": " + decryptedMessageParts[0]);
            }
        }
    }

    public synchronized void updateReceivingFrom() {
        // TODO ~ loopt in thread, mogelijkse fix van lijst update voor messages
    }

    public void run() {
        // code in the other thread, can reference "var" variable
        while(true){
            try {
                updateReceivingFrom();
                receiveMessage();
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
}
