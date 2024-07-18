package edu.school21.sockets.services;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.repositories.MessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("MessagesService")
public class MessagesServiceImpl implements MessagesService {

    @Autowired
    @Qualifier("MessagesRepositoryJdbcTemplate")
    private MessagesRepository repository;

    public void save(Message message) {
        repository.save(message);
    }
    public List<Message> find30Last(String name) {
        return repository.find30MessagesInChatroom(name);
    }

}
