package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component("MessagesRepositoryJdbcTemplate")
public class MessagesRepositoryImpl implements MessagesRepository{

    @Autowired
    @Qualifier("HikariDataSource")
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private final RowMapper<Message> rowMapper;

    public MessagesRepositoryImpl() {
        rowMapper = (rs, rowNum) -> new Message(
                rs.getLong("id"),
                rs.getString("room"),
                new User(rs.getString("author"), null, false),
                rs.getString("text"),
                rs.getTimestamp("dateTime").toLocalDateTime());
    }

    @PostConstruct
    private void initDB() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Optional<Message> findById(Long id) {
        List<Message> message = jdbcTemplate.query("select * from \"Message\" where id = ?", rowMapper, id);
        if (message.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(message.get(0));
    }

    public void save(Message message) {
        String query = "insert into \"Message\" " +
                "(room, author, text, datetime) " +
                "values(?, ?, ?, ?)";
        jdbcTemplate.update(query, message.getRoom(), message.getAuthor().getName(), message.getText(), message.getDate());
    }

    public void update(Message message) {
        String query = "update \"Message\" " +
                "set room = ?, author = ?, text = ?, datetime = ? " +
                "where id = ?;";
        jdbcTemplate.update(query, message.getRoom(), message.getAuthor().getId(), message.getText(), message.getDate(), message.getId());
    }

    public List<Message> find30MessagesInChatroom(String room) {
        String queryMessages =
                "with messages as (\n" +
                "\tselect *\n" +
                "\tfrom \"Message\"\n" +
                "\twhere room = ? \n" +
                "\torder by id desc\n" +
                "\tlimit 30\n" +
                ")\n" +
                "select *\n" +
                "from messages\n" +
                "order by id asc";
        return jdbcTemplate.query(queryMessages, rowMapper, room);
    }
}
