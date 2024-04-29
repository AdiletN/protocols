import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TCPServer {
    private ServerSocket serverSocket;
    private HashMap<String, String> keyValStore;
    private final String QUIT = "QUIT";
    private final String KEYS = "KEYS";
    private final String PUT = "PUT";
    private final String DELETE = "DELETE";
    private final String GET = "GET";
    private final String EDIT_KEY = "EDIT_KEY";
    private final String EDIT_VALUE = "EDIT_VALUE";

    public TCPServer() {
        keyValStore = new HashMap<>();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(7856);
            System.out.println("Server started, waiting for clients...");
            while (true) {
                String timeStamp = getTimeStamp();
                Socket clientSocket = serverSocket.accept();
                System.out.println("["+timeStamp+"] Client connected: " + clientSocket.getInetAddress().getHostAddress());
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());
        ) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (true) {
                String timeStamp = getTimeStamp();
                String command;
                try {
                    command = dataIn.readUTF();
                } catch (SocketException e) {
                    System.out.println("["+timeStamp+"] Client connection closed.");
                    break;
                }
                System.out.println("[" + getTimeStamp() + "] Received command from client: " + command);
                String[] parts = command.split(" ");
                switch (parts[0]) {
                    case PUT:
                        handlePutRequest(dataOut, parts[1], parts[2]);
                        break;
                    case DELETE:
                        handleDelRequest(dataOut, parts[1]);
                        break;
                    case GET:
                        handleGetRequest(dataOut, parts[1]);
                        break;
                    case KEYS:
                        handleKeysRequest(dataOut);
                        break;
                    case EDIT_KEY:
                        handleEditKeyRequest(dataOut, parts[1], parts[2]);
                        break;
                    case EDIT_VALUE:
                        handleEditValueRequest(dataOut, parts[1], parts[2]);
                        break;
                    case QUIT:
                        cleanUp(dataIn, dataOut, clientSocket);
                        System.out.println("["+timeStamp+"] Client connection closed.");
                        return;
                    default:
                        dataOut.writeUTF("["+timeStamp+"] Invalid command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePutRequest(DataOutputStream dataOut, String key, String value) throws IOException {
        if (key.length() > 10 || value.length() > 10) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key or value length exceeds 10 characters.");
        }
        else if (keyValStore.containsKey(key.toLowerCase())) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key already exists. Cannot add duplicate keys.");
        }else {
            keyValStore.put(key.toLowerCase(), value);
            dataOut.writeUTF("[" + getTimeStamp() + "] Successfully: Key ["+key+"] with value ["+value+"] added successfully");
        }
    }

    private void handleDelRequest(DataOutputStream dataOut, String key) throws IOException {
        if (key.length() > 10) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key length exceeds 10 characters.");
        }
        else if (keyValStore.containsKey(key)) {
            keyValStore.remove(key);
            dataOut.writeUTF("[" + getTimeStamp() + "] Successfully: Key ["+key+"] removed successfully");
        }
        else {
            dataOut.writeUTF("[" + getTimeStamp() + "] Error: Key does not exist or not found");
        }
    }

    private void handleGetRequest(DataOutputStream dataOut, String key) throws IOException {
        if (key.length() > 10) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key length exceeds 10 characters.");
        }
        else if (keyValStore.containsKey(key)) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key ["+key+"] with value ["+keyValStore.get(key)+"] ");
        }
        else {
            dataOut.writeUTF("[" + getTimeStamp() + "] Error: Key does not exist or not found");
        }
    }

    private void handleKeysRequest(DataOutputStream dataOut) throws IOException {
        if (keyValStore.isEmpty()) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key Store: Empty");
        } 
        else {
            StringBuilder keys = new StringBuilder();
            for (String key : keyValStore.keySet()) {
                keys.append("[").append(key).append("]");
            }
            dataOut.writeUTF("[" + getTimeStamp() + "] Key Store: " + keys.toString());
        }
    }



    private void handleEditKeyRequest(DataOutputStream dataOut, String oldKey, String newKey) throws IOException {
        if (newKey.length() > 10) {
            dataOut.writeUTF("[" + getTimeStamp() + "] New key length exceeds 10 characters.");
        } else if (keyValStore.containsKey(newKey.toLowerCase())) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key with this name already exists. Please edit it again!");
        } else if (!keyValStore.containsKey(oldKey.toLowerCase())) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key does not exist.");
        } else {
            String value = keyValStore.remove(oldKey.toLowerCase());
            keyValStore.put(newKey.toLowerCase(), value);
            dataOut.writeUTF("[" + getTimeStamp() + "] Key updated successfully.");
        }
    }

    private void handleEditValueRequest(DataOutputStream dataOut, String key, String newValue) throws IOException {
        if (key.length() > 10 || newValue.length() > 10) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key or value length exceeds 10 characters.");
        } else if (!keyValStore.containsKey(key.toLowerCase())) {
            dataOut.writeUTF("[" + getTimeStamp() + "] Key does not exist.");
        } else {
            keyValStore.put(key.toLowerCase(), newValue);
            dataOut.writeUTF("[" + getTimeStamp() + "] Value updated successfully.");
        }
    }

    private String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void cleanUp(DataInputStream dataIn, DataOutputStream dataOut, Socket clientSocket) {
        try {
            dataIn.close();
            dataOut.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        server.startServer();
    }
}
