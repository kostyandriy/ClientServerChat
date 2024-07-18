package edu.school21.sockets.client;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
    private final String SERVER_ADDRESS = "localhost"; // Адрес сервера
    private Scanner scanner;
    private String chosenMethod;
    private String chosenOption;
    private Socket serverSocket;
    private String signedInClientName;
    private String roomName;
    private volatile boolean flagRun = true;
    private volatile boolean running = true;

    private BufferedReader in;
    private BufferedWriter out;

    private ObjectMapper objectMapper;

    public  void start(int port) {
        try {
            scanner = new Scanner(System.in);
            serverSocket = new Socket(SERVER_ADDRESS, port);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
            objectMapper = new ObjectMapper();

            startMessage();
            while (flagRun) {
                if (!serverSocket.isClosed()) {
                    chooseMethod();
                    executeChosenMethod();
                    chooseOption();
                    executeChosenOption();
                    messenger();
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (scanner != null) scanner.close();
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void chooseMethod() throws IOException {
        if (signedInClientName != null) {
            return;
        }
        printStartMenu();
        while (true) {
            String method = scanner.nextLine();
            if (method == null) {
                System.err.print("Method not found. Try again\n> ");
                continue;
            }

            objectMapper = new ObjectMapper();
            JsonNode message = objectMapper.createObjectNode().put("command", method);
            String jsonMessage = objectMapper.writeValueAsString(message);
            sendMessageToServer(jsonMessage);

            JsonNode receivedAnswer = objectMapper.readTree(getMessageFromServer());
            if(receivedAnswer.has("method")) {
                String answer = receivedAnswer.get("method").asText();
                switch(answer) {
                    case "signUp":
                    case "signIn":
                    case "exit":
                        chosenMethod = answer;
                        break;
                    case "unknown":
                        System.err.print("Method not found. Try again\n> ");
                        continue;
                    default:
                        System.err.print("Unknown error. Try again\n> ");
                        continue;
                }
                break;
            } else {
                System.out.println("asiudbafa");
                System.err.print("Invalid response. Try again\n> ");
            }
        }
    }

    private void executeChosenMethod() throws IOException {
        if (signedInClientName != null) {
            return;
        }
        switch (chosenMethod) {
            case "signUp":
                trySignUp();
                break;
            case "signIn":
                trySignIn();
                break;
            case "exit":
                flagRun = false;
                break;
        }
    }

    private void trySignUp() throws IOException {
        String name, password, receivedAnswer;
        while(true) {
            objectMapper = new ObjectMapper();

            System.out.print("Enter username:\n> ");
            name = scanner.nextLine();
            JsonNode messageName = objectMapper.createObjectNode().put("name", name);
            String jsonMessageName = objectMapper.writeValueAsString(messageName);
            sendMessageToServer(jsonMessageName);

            System.out.print("Enter password:\n> ");
            password = scanner.nextLine();
            JsonNode messagePassword = objectMapper.createObjectNode().put("password", password);
            String jsonMessagePassword = objectMapper.writeValueAsString(messagePassword);
            sendMessageToServer(jsonMessagePassword);

            JsonNode receivedAnswerJson = objectMapper.readTree(getMessageFromServer());

            if (name.equalsIgnoreCase("exit") || password.equalsIgnoreCase("exit")) {
                chosenMethod = "";
                break;
            }

            if (receivedAnswerJson.has("answer")) {
                receivedAnswer = receivedAnswerJson.get("answer").asText();
                if (receivedAnswer.equals("Signed up successfully")) {
                    System.out.println("Successful!");
                    break;
                } else {
                    System.err.print("Name already exists. Try again.\n> ");
                }
            } else {
                System.err.print("Unknown error. Try again.\n> ");
            }

        }
    }

    private void trySignIn() throws IOException {
        String name, password, receivedAnswer;
        while(true) {
            System.out.print("Enter username:\n> ");
            name = scanner.nextLine();
            JsonNode messageName = objectMapper.createObjectNode().put("name", name);
            String jsonMessageName = objectMapper.writeValueAsString(messageName);
            sendMessageToServer(jsonMessageName);

            System.out.print("Enter password:\n> ");
            password = scanner.nextLine();
            JsonNode messagePassword = objectMapper.createObjectNode().put("password", password);
            String jsonMessagePassword = objectMapper.writeValueAsString(messagePassword);
            sendMessageToServer(jsonMessagePassword);

            JsonNode receivedAnswerJson = objectMapper.readTree(getMessageFromServer());

            if (name.equalsIgnoreCase("exit") || password.equalsIgnoreCase("exit")) {
                chosenMethod = "";
                break;
            }

            if (receivedAnswerJson.has("answer")) {
                receivedAnswer = receivedAnswerJson.get("answer").asText();
                if (receivedAnswer.equals("Signed in successfully")) {
                    System.out.println("Successful!");
                    signedInClientName = name;
                    break;
                } else {
                    System.err.print(receivedAnswer + ". Try again.\n> ");
                }
            } else {
                System.err.print("Unknown error. Try again.\n> ");
            }
        }
    }

    private void chooseOption() throws IOException {
        if (signedInClientName == null) {
            return;
        }
        if (chosenOption != null) {
            if (chosenOption.equals("choose room")) {
                return;
            }
        }
        printOptionMenu();
        while (true) {
            String option = scanner.nextLine();
            if (option == null) {
                System.err.print("Unknown option. Try again\n> ");
                continue;
            }
            objectMapper = new ObjectMapper();
            JsonNode message = objectMapper.createObjectNode().put("option", option);
            String jsonMessage = objectMapper.writeValueAsString(message);
            sendMessageToServer(jsonMessage);

            JsonNode receivedAnswer = objectMapper.readTree(getMessageFromServer());
            if(receivedAnswer.has("option")) {
                String answer = receivedAnswer.get("option").asText();
                switch(answer) {
                    case "create room":
                    case "choose room":
                    case "exit":
                        chosenOption = answer;
                        break;
                    case "unknown":
                        System.err.print("Option not found. Try again\n> ");
                        continue;
                    default:
                        System.err.print("Option error. Try again\n> ");
                        continue;
                }
                break;
            } else {
                System.err.print("Invalid response. Try again\n> ");
            }
        }
    }

    private void executeChosenOption() throws IOException {
        if (signedInClientName == null) {
            return;
        }
        switch (chosenOption) {
            case "create room":
                tryCreateRoom();
                break;
            case "choose room":
                tryChooseRoom();
                break;
            case "exit":
                signOutUser();
                signedInClientName = null;
                break;
        }
    }

    private void signOutUser() throws IOException{
        JsonNode name = objectMapper.createObjectNode().put("name", signedInClientName);
        String nameJson = objectMapper.writeValueAsString(name);
        sendMessageToServer(nameJson);
    }

    private void tryCreateRoom() throws IOException {
        String name, receivedAnswer;
        while(true) {
            System.out.print("Enter room name:\n> ");
            name = scanner.nextLine();

            JsonNode message = objectMapper.createObjectNode()
                    .put("room name", name)
                    .put("owner", signedInClientName);

            String jsonMessage = objectMapper.writeValueAsString(message);
            sendMessageToServer(jsonMessage);

            JsonNode answer = objectMapper.readTree(getMessageFromServer());

            if (answer.has("answer")) {
                receivedAnswer = answer.get("answer").asText();
                if (receivedAnswer.equals("created successfully")) {
                    System.out.println("Room " + name + " created successfully");
                    break;
                } else if (receivedAnswer.equals("empty name")) {
                    System.err.print("Room name can't be empty. Try again.\n> ");
                } else {
                    System.err.print("Room already exists. Try again\n> ");
                }
            } else {
                System.err.print("Unknown error. Try again.\n> ");
            }
        }
    }

    private void tryChooseRoom() throws IOException {
        JsonNode answer = objectMapper.readTree(getMessageFromServer());
        if (answer.has("answer")) {
            System.err.println("There are no chatrooms.");
            chosenOption = "";
            return;
        }
        System.out.println("Rooms:");
        int i = 1;
        while (answer.has("chatroom." + i)) {
            System.out.println(i + ". " + answer.get("chatroom." + i).asText());
            i++;
        }
        System.out.print(i + ". Exit\n> ");
        while (true) {
            String numRoom = scanner.nextLine();

            if (numRoom.equals(String.valueOf(i))) {
                JsonNode message = objectMapper.createObjectNode().put("command", "exit");
                sendMessageToServer(objectMapper.writeValueAsString(message));
                chosenOption = "";
                return;
            }

            JsonNode message = objectMapper.createObjectNode().put("room number", numRoom);
            sendMessageToServer(objectMapper.writeValueAsString(message));

            JsonNode answerRoom = objectMapper.readTree(getMessageFromServer());
            if (answerRoom.has("answer")) {
                String name = answerRoom.get("answer").asText();
                if (name.equals("wrong number")) {
                    System.err.print("There is no such parameter. Try again\n> ");
                    continue;
                } else {
                    roomName = name;
                    break;
                }
            } else {
                System.err.println("Unknown error");
                break;
            }
        }
    }

    private void messenger() {
        if (roomName == null) {
            return;
        }
        try {
            System.out.println(roomName + "  ---");
            JsonNode info = objectMapper.createObjectNode().put("room name", roomName);
            sendMessageToServer(objectMapper.writeValueAsString(info));

            JsonNode messages = objectMapper.readTree(getMessageFromServer());
            int i = 1;
            while (messages.has("message." + i)) {
                String message = messages.get("message." + i).asText();
                System.out.println(message);
                i++;
            }

            Thread messagingThread = new Thread(() -> {
                try {
                    String message, author;
                    while (running && !serverSocket.isClosed()) {
                        try {
                            JsonNode messageJson = objectMapper.readTree(getMessageFromServer());
                            if (messageJson.has("message") && messageJson.has("author")) {
                                message = messageJson.get("message").asText();
                                author = messageJson.get("author").asText();
                                if (message != null && author != null) {
                                    System.out.println(author + ": " + message);
                                } else {
                                    break;
                                }
                            }
                        } catch (SocketException e) {
                            if (running) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            });
            messagingThread.start();

                while (true) {
                    String message = scanner.nextLine();

                    JsonNode messageJson;
                    if (message.equalsIgnoreCase("exit")) {
                        messageJson = objectMapper.createObjectNode()
                                .put("command", "exit")
                                .put("user name", signedInClientName);
                        sendMessageToServer(objectMapper.writeValueAsString(messageJson));
                        running = false;
                        flagRun = false;
                        messagingThread.interrupt();
                        System.out.println("You have left the chat.");
                        break;
                    }
                    messageJson = objectMapper.createObjectNode()
                                    .put("user name", signedInClientName)
                                    .put("text", message)
                                    .put("room name", roomName);
                    sendMessageToServer(objectMapper.writeValueAsString(messageJson));
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printStartMenu() {
        System.out.println("1. signIn");
        System.out.println("2. signUp");
        System.out.println("3. exit");
        System.out.print("> ");
    }

    private void printOptionMenu() {
        System.out.println("1. create room");
        System.out.println("2. choose room");
        System.out.println("3. exit");
        System.out.print("> ");
    }

    private void startMessage() throws IOException {
        JsonNode message = objectMapper.readTree(getMessageFromServer());
        if (message.has("message")) {
            System.out.println(message.get("message").asText());
        }
    }

    private void sendMessageToServer(String message) throws IOException {
        out.write(message);
        out.newLine();
        out.flush();
    }

    private String getMessageFromServer() throws IOException {
        if (serverSocket.isClosed()) {
            return null;
        }
        return in.readLine();
    }
}
