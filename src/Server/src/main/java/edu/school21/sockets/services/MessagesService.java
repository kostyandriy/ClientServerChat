package edu.school21.sockets.services;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.repositories.MessagesRepository;

import java.util.List;

public interface MessagesService {
    void save(Message message);
    List<Message> find30Last(String name);
}
