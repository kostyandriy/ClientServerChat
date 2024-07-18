package edu.school21.sockets.services;

public interface UsersService {
    boolean signUp(String name, String password);
    String signIn(String name, String password);
    void signOut(String name);
}
