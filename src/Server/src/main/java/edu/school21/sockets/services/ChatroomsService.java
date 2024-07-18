package edu.school21.sockets.services;

import edu.school21.sockets.models.Chatroom;

import java.util.List;

public interface ChatroomsService {
    boolean save(String name, String owner);
    List<Chatroom> findAll();
}
