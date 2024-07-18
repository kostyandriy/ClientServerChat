package edu.school21.sockets.models;

import java.util.List;
import java.util.Objects;

public class Chatroom {
    private Long id;
    private String name;
    private User owner;
    private List<Message> messages;

    public Chatroom(Long id, String name, User owner, List<Message> messages) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.messages = messages;
    }

    public Chatroom(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public boolean equals(Chatroom o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chatroom chatroom = (Chatroom) o;
        return Objects.equals(id, chatroom.id) &&
                Objects.equals(name, chatroom.name) &&
                owner.equals(chatroom.owner) &&
                Objects.equals(messages, chatroom.messages);
    }

    public String toString() {
        return "User{" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "owner = " + (owner != null ? owner.getName() : "null")  + ", " +
                "messages = " + (messages != null ? messages.size() : "null") + "}";
    }

    public int hashCode() {
        return Objects.hash(id, name, owner, messages);
    }

}
