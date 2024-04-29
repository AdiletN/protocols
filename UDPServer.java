import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class UDPServer {
    private static final int SERVER_PORT = 7856;
    private static DatagramSocket serverSocket;
    private static HashMap<String, String> keyValStore;
    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String EDIT_KEY = "EDIT_KEY";
    private static final String EDIT_VALUE = "EDIT_VALUE";

    public UDPServer() {
        keyValStore = new HashMap<>();
    }

    public void startServer() {
        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            System.out.println("Server started, waiting for clients...");
            boolean clientConnected = false;

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String timeStamp = getTimeStamp();
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                if (!clientConnected) {
                    System.out.println("["+timeStamp+"] Client connected: " + clientAddress.getHostAddress());
                    clientConnected = true;
                }

                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("[" + timeStamp + "] Received: " + receivedMessage + " from " + clientAddress.getHostAddress() + ":" + clientPort);

                String[] parts = receivedMessage.split(" ");

                switch (parts[0]) {
                    case PUT:
                        handlePutRequest(clientAddress, clientPort, parts[1], parts[2]);
                        break;
                    case DELETE:
                        handleDelRequest(clientAddress, clientPort, parts[1]);
                        break;
                    case GET:
                        handleGetRequest(clientAddress, clientPort, parts[1]);
                        break;
                    case KEYS:
                        handleKeysRequest(clientAddress, clientPort);
                        break;
                    case QUIT:
                        System.out.println("["+timeStamp+"] Client connection closed.");
                        clientConnected = false;
                        break;
                    case EDIT_KEY:
                        handleEditKeyRequest(clientAddress, clientPort, parts[1], parts[2]);
                        break;
                    case EDIT_VALUE:
                        handleEditValueRequest(clientAddress, clientPort, parts[1], parts[2]);
                        break;
                    default:
                        sendDataPacket(clientAddress, clientPort,"[" + timeStamp + "] Invalid command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePutRequest(InetAddress clientAddress, int clientPort, String key, String value) {
        if (key.length() > 10 || value.length() > 10) {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Key or value length exceeds 10 characters.");
        } else if (keyValStore.containsKey(key.toLowerCase())) {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Key already exists. Cannot add duplicate keys.");
        } else {
            keyValStore.put(key.toLowerCase(), value);
            sendDataPacket(clientAddress, clientPort, "[" + getTimeStamp() + "] Successfully: Key ["+key+"] with value ["+value+"] added successfully");
        }
    }

    private void handleDelRequest(InetAddress clientAddress, int clientPort, String key) {
        if (key.length() > 10) {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Key length exceeds 10 characters.");
        } 
        else if (keyValStore.containsKey(key)) {
            keyValStore.remove(key);
            sendDataPacket(clientAddress, clientPort, "[" + getTimeStamp() + "] Successfully: Key ["+key+"] removed successfully");
        } 
        else {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Error: Key does not exist or not found");
        }
    }

    private void handleGetRequest(InetAddress clientAddress, int clientPort, String key) {
        if (key.length() > 10) {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Key length exceeds 10 characters.");
        } 
        else if (keyValStore.containsKey(key)) {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Key ["+key+"] with value ["+keyValStore.get(key)+"] ");
        } 
        else {
            sendDataPacket(clientAddress, clientPort,"[" + getTimeStamp() + "] Error: Key does not exist or not found");
        }
    }

    private void handleKeysRequest(InetAddress clientAddress, int clientPort) {
        if (keyValStore.isEmpty()) {
            sendDataPacket(clientAddress, clientPort, "[" + getTimeStamp() + "] Key Store: Empty");
        } 
        else {
            StringBuilder keys = new StringBuilder();
            for (String key : keyValStore.keySet()) {
                keys.append("[").append(key).append("] ");
            }
            sendDataPacket(clientAddress, clientPort, "[" + getTimeStamp() + "] Key Store: " + keys.toString());
        }
    }
    private void handleEditKeyRequest(InetAddress clientAddress, int clientPort, String oldKey, String newKey) {
        if (keyValStore.containsKey(oldKey.toLowerCase())) {
            if (!keyValStore.containsKey(newKey.toLowerCase())) {
                String value = keyValStore.get(oldKey.toLowerCase());
                keyValStore.remove(oldKey.toLowerCase());
                keyValStore.put(newKey.toLowerCase(), value);
                sendDataPacket(clientAddress, clientPort, "Key [" + oldKey + "] has been successfully changed to [" + newKey + "]");
            } 
            else {
                sendDataPacket(clientAddress, clientPort, "Key with name [" + newKey + "] already exists. Please choose a different key name.");
            }
        } 
        else {
            sendDataPacket(clientAddress, clientPort, "Key with name [" + oldKey + "] does not exist.");
        }
    }

    private void handleEditValueRequest(InetAddress clientAddress, int clientPort, String key, String newValue) {
        if (keyValStore.containsKey(key.toLowerCase())) {
            keyValStore.put(key.toLowerCase(), newValue);
            sendDataPacket(clientAddress, clientPort, "Value for key [" + key + "] has been successfully changed to [" + newValue + "]");
        } 
        else {
            sendDataPacket(clientAddress, clientPort, "Key with name [" + key + "] does not exist.");
        }
    }

    private void sendDataPacket(InetAddress clientAddress, int clientPort, String data) {
        try {
            byte[] sendData = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.startServer();
    }
}

