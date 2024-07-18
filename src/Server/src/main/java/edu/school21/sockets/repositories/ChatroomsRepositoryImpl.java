package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.repositories.MessagesRepositoryImpl;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component("ChatroomsRepository")
public class ChatroomsRepositoryImpl implements ChatroomsRepository {

    @Autowired
    @Qualifier("HikariDataSource")
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private final RowMapper<Chatroom> rowMapper;

    public ChatroomsRepositoryImpl() {
        rowMapper = (rs, rowNum) -> new Chatroom(
                rs.getLong("id"),
                rs.getString("name"),
                new User(rs.getString("name"), null, false),
                null);
    }

    @PostConstruct
    private void initDB() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }



    public void save(Chatroom chatroom) {
        String query = "insert into \"Chatroom\" " +
                "(name, owner) " +
                "values(?, ?)";
        jdbcTemplate.update(query, chatroom.getName(), chatroom.getOwner().getName());
    }

    public List<Chatroom> findAll() {
        return jdbcTemplate.query("select * from \"Chatroom\"", rowMapper);
    }

    public Optional<Chatroom> findByName(String name) {
        List<Chatroom> chatroom = jdbcTemplate.query("select * from \"Chatroom\" where name = ?", rowMapper, name);
        if (chatroom.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(chatroom.get(0));
    }
}
