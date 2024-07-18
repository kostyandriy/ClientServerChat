package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.Message;

import java.util.List;
import java.util.Optional;

public interface ChatroomsRepository {
    void save(Chatroom chatroom);
    List<Chatroom> findAll();
    Optional<Chatroom> findByName(String name);

}
