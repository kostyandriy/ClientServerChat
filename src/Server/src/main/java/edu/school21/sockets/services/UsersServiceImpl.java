package edu.school21.sockets.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.UsersRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("UsersService")
public class UsersServiceImpl implements UsersService {

    @Autowired
    @Qualifier("UsersRepositoryJdbcTemplate")
    private UsersRepository repository;

    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder passwordEncoder;

    public boolean signUp(String name, String password) {
        if (repository.findByName(name).isPresent()) {
            return false;
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(name, encodedPassword, false);
        repository.save(user);
        return true;
    }

    @Transactional
    public synchronized String signIn(String name, String password) {
        Optional<User> user = repository.findByName(name);
        if (!user.isPresent()) {
            return "User not found";
        }
        if (user.get().isAuthorised()) {
            return "User already authorised";
        }
        if (passwordEncoder.matches(password, user.get().getPassword()) ||
        password.equals(user.get().getPassword())) {
            User userToSignIn = user.get();
            userToSignIn.setAuthorised(true);
            repository.update(userToSignIn);
            return "Signed in successfully";
        } else {
            return "Incorrect password";
        }
    }

    public void signOut(String name) {
        Optional<User> user = repository.findByName(name);
        if (!user.isPresent()) {
            return;
        }

        User userToSignOut = user.get();
        userToSignOut.setAuthorised(false);
        repository.update(userToSignOut);
    }
}
