package edu.school21.sockets.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;
import edu.school21.sockets.services.ChatroomsService;
import edu.school21.sockets.services.MessagesService;
import edu.school21.sockets.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component("Server")
public class Server {

    @Autowired()
    @Qualifier("UsersService")
    private UsersService usersService;

    @Autowired()
    @Qualifier("MessagesService")
    private MessagesService messagesService;

    @Autowired()
    @Qualifier("ChatroomsSeervice")
    private ChatroomsService chatroomsService;

    private volatile boolean flagRun = true;
    private final ConcurrentHashMap<Socket, String> test = new ConcurrentHashMap<>();

    public void runServer(int port) {
        System.out.println("Server is starting on port: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)){
            while(flagRun) {
                Socket clientSocket = serverSocket.accept();
                printStartInfo(clientSocket);

                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        String chosenMethod = "";
        String chosenOption = "";
        String roomName = "";
        boolean signInExit = false;
        ObjectMapper objectMapper = new ObjectMapper();
        boolean flagClosed = false;
        while(true) {
            if (!chosenMethod.equals("signIn")) {
                do {
                    if (clientSocket.isClosed()) {
                        flagClosed = true;
                        break;
                    }
                    chosenMethod = validMethodFromClient(clientSocket);
                } while (chosenMethod == null);
                if (flagClosed) {
                    break;
                }
                if (chosenMethod.equalsIgnoreCase("exit")) {
                    break;
                }
                signInExit = executeChosenMethod(clientSocket, chosenMethod);
                if (signInExit) {
                    chosenMethod = "";
                }
                if (!chosenMethod.equals("signIn")) {
                    continue;
                }
            }

            do {
                if (clientSocket.isClosed()) {
                    flagClosed = true;
                    break;
                }
                chosenOption = validOptionFromClient(clientSocket);
            } while (chosenOption == null);
            if (flagClosed) {
                break;
            }
            if (chosenOption.equalsIgnoreCase("exit")) {
                JsonNode nameJson = objectMapper.readTree(getMessageFromClient(clientSocket));
                if (nameJson.has("name")) {
                    signOut(nameJson.get("name").asText());
                    chosenMethod = "";
                    continue;
                } else {
                    System.err.println("Error: Didn't sign out");
                }
            }
            roomName = executeChosenOption(clientSocket, chosenOption);
            if (chosenOption.equals("choose room") && !roomName.equals("exit") && !roomName.equals("empty")) {
                messenger(clientSocket);
                break;
            }
            if (roomName.equals("empty")) {
                chosenOption = "";
            }
            if (roomName.equals("exit")) {
                chosenOption = "";
            }
        }
    }

    private String validMethodFromClient(Socket clientSocket) throws IOException {
        String socketMessage = getMessageFromClient(clientSocket);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode message = objectMapper.readTree(socketMessage);

        if (message.has("command")) {
            String command = message.get("command").asText();
            JsonNode method;
            switch (command) {
                case "1":
                    method = objectMapper.createObjectNode().put("method", "signIn");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return "signIn";
                case "2":
                    method = objectMapper.createObjectNode().put("method", "signUp");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return "signUp";
                case "3":
                    method = objectMapper.createObjectNode().put("method", "exit");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return "exit";
                default:
                    method = objectMapper.createObjectNode().put("method", "unknown");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return null;
            }
        }
        return null;
    }

    private boolean executeChosenMethod(Socket clientSocket, String chosenMethod) throws IOException {
        if (chosenMethod.equals("signUp")) {
            trySignUp(clientSocket);
            return false;
        }
        if (chosenMethod.equals("signIn")) {
            return trySignIn(clientSocket);
        }
        return false;
    }

    synchronized private void trySignUp(Socket clientSocket) throws IOException {
        String name, password;
        while(true) {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode messageName = objectMapper.readTree(getMessageFromClient(clientSocket));
            JsonNode messagePassword = objectMapper.readTree(getMessageFromClient(clientSocket));
            JsonNode answer;

            if (messageName.has("name") && messagePassword.has("password")) {
                name = messageName.get("name").asText();
                password = messagePassword.get("password").asText();

                if (name.equalsIgnoreCase("exit") || password.equalsIgnoreCase("exit")) {
                    answer = objectMapper.createObjectNode().put("answer", "exit");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                    break;
                }

                if (usersService.signUp(name, password)) {
                    answer = objectMapper.createObjectNode().put("answer", "Signed up successfully");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                    break;
                } else {
                    answer = objectMapper.createObjectNode().put("answer", "Sign up failed");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                }
            } else {
                answer = objectMapper.createObjectNode().put("answer", "Sign up failed");
                sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
            }
        }
    }

    synchronized private boolean trySignIn(Socket clientSocket) throws IOException {
        String name, password;
        while(true) {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode messageName = objectMapper.readTree(getMessageFromClient(clientSocket));
            JsonNode messagePassword = objectMapper.readTree(getMessageFromClient(clientSocket));
            JsonNode answer;

            if (messageName.has("name") && messagePassword.has("password")) {
                name = messageName.get("name").asText();
                password = messagePassword.get("password").asText();

                if (name.equalsIgnoreCase("exit") || password.equalsIgnoreCase("exit")) {
                    answer = objectMapper.createObjectNode().put("answer", "exit");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                    return true;
                }
                String resultSigningIn = usersService.signIn(name, password);

                if (resultSigningIn.equals("Signed in successfully")) {
                    answer = objectMapper.createObjectNode().put("answer", "Signed in successfully");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                    return false;
                } else {
                    answer = objectMapper.createObjectNode().put("answer", resultSigningIn);
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                }
            } else {
                answer = objectMapper.createObjectNode().put("answer", "Sign in failed");
                sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
            }
        }
    }

    private String validOptionFromClient(Socket clientSocket) throws IOException {
        String socketMessage = getMessageFromClient(clientSocket);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode message = objectMapper.readTree(socketMessage);

        if (message.has("option")) {
            String command = message.get("option").asText();
            JsonNode method;
            switch (command) {
                case "1":
                    method = objectMapper.createObjectNode().put("option", "create room");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return "create room";
                case "2":
                    method = objectMapper.createObjectNode().put("option", "choose room");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return "choose room";
                case "3":
                    method = objectMapper.createObjectNode().put("option", "exit");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return "exit";
                default:
                    method = objectMapper.createObjectNode().put("option", "unknown");
                    sendMessageToClient(objectMapper.writeValueAsString(method), clientSocket);
                    return null;
            }
        }
        return null;
    }

    private String executeChosenOption(Socket clientSocket, String chosenOption) throws IOException {
        if (chosenOption.equals("create room")) {
            tryCreateRoom(clientSocket);
            return "";
        }
        if (chosenOption.equals("choose room")) {
            return tryChooseRoom(clientSocket);
        }
        return "";
    }

    private String tryChooseRoom(Socket clientSocket) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode answer = null;
        List<Chatroom> chatrooms = chatroomsService.findAll();
        if (chatrooms.isEmpty()) {
            answer = objectMapper.createObjectNode().put("answer", "There are no chatrooms");
            sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
            return "empty";
        }
        int i = 1;
        ObjectNode answerNode = objectMapper.createObjectNode();
        for (Chatroom chatroom : chatrooms) {
            answerNode.put("chatroom." + i, chatroom.getName());
            i++;
        }
        answer = answerNode;
        sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);

        while (true) {
            JsonNode numOfRoomMessage = objectMapper.readTree(getMessageFromClient(clientSocket));
            JsonNode answerValid;
            if (numOfRoomMessage.has("command")) {
                return "exit";
            }
            if (numOfRoomMessage.has("room number")) {
                String room = numOfRoomMessage.get("room number").asText();
                int j;
                try {
                    j = Integer.parseInt(room);
                } catch (NumberFormatException e) {
                    answerValid = objectMapper.createObjectNode().put("answer", "wrong number");
                    sendMessageToClient(objectMapper.writeValueAsString(answerValid), clientSocket);
                    continue;
                }
                if (j > chatrooms.size() || j < 1) {
                    answerValid = objectMapper.createObjectNode().put("answer", "wrong number");
                    sendMessageToClient(objectMapper.writeValueAsString(answerValid), clientSocket);
                    continue;
                }
                String name = chatrooms.get(j - 1).getName();
                answerValid = objectMapper.createObjectNode().put("answer", name);
                sendMessageToClient(objectMapper.writeValueAsString(answerValid), clientSocket);
                return name;
            }
        }
    }

    private void tryCreateRoom(Socket clientSocket) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        while(true) {
            JsonNode message = objectMapper.readTree(getMessageFromClient(clientSocket));
            JsonNode answer;
            if (message.has("room name") && message.has("owner")) {
                String roomName = message.get("room name").asText();
                String roomOwner = message.get("owner").asText();
                if (roomName.isEmpty()) {
                    answer = objectMapper.createObjectNode().put("answer", "empty name");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                    continue;
                }

                if (chatroomsService.save(roomName, roomOwner)) {
                    answer = objectMapper.createObjectNode().put("answer", "created successfully");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                    break;
                } else {
                    answer = objectMapper.createObjectNode().put("answer", "room already exists");
                    sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
                }
            } else {
                answer = objectMapper.createObjectNode().put("answer", "unknown error");
                sendMessageToClient(objectMapper.writeValueAsString(answer), clientSocket);
            }
        }
    }

    synchronized private void signOut(String name) {
        usersService.signOut(name);
    }

    private void messenger(Socket clientSocket) {
        ObjectMapper objectMapper = new ObjectMapper();
        String roomNameToWalkIn = "";
        try {
            JsonNode info = objectMapper.readTree(getMessageFromClient(clientSocket));
            if (info.has("room name")) {
                if (!test.containsKey(clientSocket)) {
                    roomNameToWalkIn = info.get("room name").asText();
                    test.put(clientSocket, roomNameToWalkIn);
                }
            }

            List<Message> messages = new ArrayList<>();
            JsonNode last30Messages;
            if (!roomNameToWalkIn.isEmpty()) {
                messages = messagesService.find30Last(roomNameToWalkIn);
            }
            if (messages.isEmpty()) {
                last30Messages = objectMapper.createObjectNode().put("answer", "There are no messages");
                sendMessageToClient(objectMapper.writeValueAsString(last30Messages), clientSocket);
            }
            int i = 1;
            ObjectNode answerNode = objectMapper.createObjectNode();
            for (Message message : messages) {
                answerNode.put("message." + i, message.getAuthor().getName() + ": " + message.getText());
                i++;
            }
            last30Messages = answerNode;
            sendMessageToClient(objectMapper.writeValueAsString(last30Messages), clientSocket);

            while (true) {
                JsonNode messageJson = objectMapper.readTree(getMessageFromClient(clientSocket));
                if (messageJson.has("command") && messageJson.has("user name")) {
                    signOut(messageJson.get("user name").asText());
                    break;
                }
                if (messageJson.has("user name") && messageJson.has("text") && messageJson.has("room name")) {
                    String roomName = messageJson.get("room name").asText();
                    String author = messageJson.get("user name").asText();
                    String message = messageJson.get("text").asText();
                    if (message == null || author == null) {
                        break;
                    }
                    saveMessage(roomName, author, message);
                    broadcastMessage(roomName, author, message, clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printStartInfo(Socket clientSocket) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("New client connected");

        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        System.out.println("Client connected from: " + clientInfo);

        JsonNode message = objectMapper.createObjectNode().put("message", "Hello from Server!");
        sendMessageToClient(objectMapper.writeValueAsString(message), clientSocket);
    }

    public void saveMessage(String roomName, String author, String message) {
        messagesService.save(new Message(roomName, new User(author), message, LocalDateTime.now()));
    }

    private void broadcastMessage(String roomName, String author, String message, Socket senderSocket) {
        ObjectMapper objectMapper = new ObjectMapper();
        test.forEach((clientSocket, room) -> {
            if (clientSocket != senderSocket && !clientSocket.isClosed() && room.equals(roomName)) {
                try {
                    JsonNode messageJson = objectMapper.createObjectNode()
                            .put("author", author)
                            .put("message", message);
                    sendMessageToClient(objectMapper.writeValueAsString(messageJson), clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getMessageFromClient(Socket clientSocket) throws IOException {
        try {
            if (clientSocket == null || clientSocket.isClosed()) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void sendMessageToClient(String message, Socket clientSocket) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        out.write(message);
        out.newLine();
        out.flush();
    }
}
