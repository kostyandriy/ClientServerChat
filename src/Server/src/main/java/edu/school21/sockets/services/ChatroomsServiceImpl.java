package edu.school21.sockets.services;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.ChatroomsRepository;
import edu.school21.sockets.repositories.MessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ChatroomsSeervice")
public class ChatroomsServiceImpl implements ChatroomsService {
    @Autowired
    @Qualifier("ChatroomsRepository")
    private ChatroomsRepository repository;

    public boolean save(String name, String owner) {
        if (repository.findByName(name).isPresent()) {
            return false;
        }
        Chatroom chatroom = new Chatroom(name, new User(owner));
        repository.save(chatroom);
        return true;
    }

    public List<Chatroom> findAll() {
        return repository.findAll();
    }
}
