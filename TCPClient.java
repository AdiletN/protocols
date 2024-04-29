import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class TCPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 7856;
    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String EDIT = "EDIT";
    private static final String EDIT_KEY = "EDIT_KEY";
    private static final String EDIT_VALUE = "EDIT_VALUE";
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    private static Socket socket;

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            while (true) {
                displayMenu();
                String timeStamp = getTimeStamp();
                String input = scanner.nextLine().trim();
                String[] parts = input.split(" ");
                String command = parts[0].toUpperCase();
                switch (command) {
                    case QUIT:
                        sendCommand(QUIT);
                        System.out.println("["+timeStamp+"] Thank you for cooperation!");
                        cleanUp();
                        return;
                    case KEYS:
                        if (parts.length!=1) {
                            System.out.println("["+timeStamp+"] Invalid command. Usage: KEYS");
                            break;
                        }
                        handleKeysRequest();
                        break;
                    case PUT:
                        if (parts.length < 3 || parts.length > 3) {
                            System.out.println("["+timeStamp+"] Invalid command. Usage: PUT <key> <value>");
                            break;
                        }
                        handlePutRequest(parts[1], parts[2]);
                        break;
                    case DELETE:
                        if (parts.length < 2 || parts.length > 2) {
                            System.out.println("["+timeStamp+"] Invalid command. Usage: DELETE <key>");
                            break;
                        }
                        handleDelRequest(parts[1]);
                        break;
                    case GET:
                        if (parts.length < 2 || parts.length > 2) {
                            System.out.println("["+timeStamp+"] Invalid command. Usage: GET <key>");
                            break;
                        }
                        handleGetRequest(parts[1]);
                        break;
                    case EDIT:
                        if (parts.length!=1) {
                            System.out.println("["+timeStamp+"] Invalid command. Usage: EDIT");
                            break;
                        }
                        handleEditMenu();
                        break;
                    default:
                        System.out.println("["+timeStamp+"] Invalid command.");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void displayMenu() {
        System.out.println("\nChoose an option:");
        System.out.println("1. GET <key>");
        System.out.println("2. PUT <key> <value>");
        System.out.println("3. DELETE <key>");
        System.out.println("4. KEYS");
        System.out.println("5. EDIT");
        System.out.println("6. QUIT");
        System.out.print("Enter your choice: ");
    }

    private static void handleEditMenu() {
        System.out.println("\nEdit Menu:");
        System.out.println("1. Edit key");
        System.out.println("2. Edit value");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
        Scanner scanner = new Scanner(System.in);

        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleEditKey();
                    break;
                case 2:
                    handleEditValue();
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid choice. Please choose between 1 and 3.");
                    break;
            }
        } 
        else {
            System.out.println("Invalid input. Please enter a number between 1 and 3.");
            scanner.nextLine();
        }
    }

    private static void handleEditKey() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the old key: ");
            String oldKey = scanner.nextLine().trim();
            System.out.print("Enter the new key: ");
            String newKey = scanner.nextLine().trim();
            sendCommand(EDIT_KEY + " " + oldKey + " " + newKey);
            String response = dataIn.readUTF();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleEditValue() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the key: ");
            String key = scanner.nextLine().trim();
            System.out.print("Enter the new value: ");
            String newValue = scanner.nextLine().trim();
            sendCommand(EDIT_VALUE + " " + key + " " + newValue);
            String response = dataIn.readUTF();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendCommand(String command) throws IOException {
        dataOut.writeUTF(command);
    }

    private static void handleGetRequest(String key) throws IOException {
        sendCommand(GET + " " + key);
        String response = dataIn.readUTF();
        System.out.println(response);
    }

    private static void handlePutRequest(String key, String value) throws IOException {
        sendCommand(PUT + " " + key + " " + value);
        String response = dataIn.readUTF();
        System.out.println(response);
    }

    private static void handleDelRequest(String key) throws IOException {
        sendCommand(DELETE + " " + key);
        String response = dataIn.readUTF();
        System.out.println(response);
    }

    private static void handleKeysRequest() throws IOException {
        sendCommand(KEYS);
        String response = dataIn.readUTF();
        System.out.println(response);
    }
    private static String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }
    private static void cleanUp() {
        try {
            dataIn.close();
            dataOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
