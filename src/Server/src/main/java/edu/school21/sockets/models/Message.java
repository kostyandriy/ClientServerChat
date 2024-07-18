package edu.school21.sockets.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {

    private Long id;
    private String room;
    private User author;
    private String text;
    private LocalDateTime dateTime;

    public Message(Long id, String room, User author, String text, LocalDateTime dateTime) {
        this.id = id;
        this.room = room;
        this.author = author;
        this.text = text;
        this.dateTime = dateTime;
    }

    public Message(String room, User author, String text, LocalDateTime dateTime) {
        this.room = room;
        this.author = author;
        this.text = text;
        this.dateTime = dateTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Long getId() {
        return id;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDate() {
        return dateTime;
    }

    public boolean equals(Message o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;

        return Objects.equals(id, message.id) &&
                Objects.equals(author, message.author) &&
                Objects.equals(room, message.room) &&
                Objects.equals(text, message.text) &&
                Objects.equals(dateTime, message.dateTime);
    }

    public String toString() {
        return "Message(" +
                "\n\tid = " + id + ",\n" +
                "\tauthor = " + room + ",\n" +
                "\tauthor = " + author.toString() + ",\n" +
                "\ttext = " + text + ",\n" +
                "\tdate = " + dateTime + "\n)";
    }

    public int hashCode() {
        return Objects.hash(id, room, author, text, dateTime);
    }
}

