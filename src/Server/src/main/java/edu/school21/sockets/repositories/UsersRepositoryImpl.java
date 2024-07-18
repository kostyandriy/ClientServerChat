package edu.school21.sockets.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import edu.school21.sockets.models.User;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component("UsersRepositoryJdbcTemplate")
public class UsersRepositoryImpl implements UsersRepository{

    @Autowired
    @Qualifier("HikariDataSource")
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper;

    public UsersRepositoryImpl() {
        rowMapper = (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("name"), rs.getString("password"), rs.getBoolean("authorised"));
    }

    @PostConstruct
    private void initDB() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public Optional<User> findById(Long id) {
        List<User> user = jdbcTemplate.query("select * from \"User\" where id = ?", rowMapper, id);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(user.get(0));
    }

    public List<User> findAll() {
        return jdbcTemplate.query("select * from \"User\"", rowMapper);
    }

    public void save(User entity) {
        String query = "insert into \"User\" " +
                "(name, password, authorised) " +
                "values(?, ?, ?)";
        jdbcTemplate.update(query, entity.getName(), entity.getPassword(), false);
    }

    public void update(User entity) {
        String query = "update \"User\" " +
                "set name = ?, password = ?, authorised = ? " +
                "where id = ?;";
        jdbcTemplate.update(query, entity.getName(), entity.getPassword(), entity.isAuthorised(), entity.getId());
    }

    public void delete(Long id) {
        String query = "delete from \"User\" " +
                "where id = ?";
        jdbcTemplate.update(query, id);
    }

    public Optional<User> findByName(String name) {
        List<User> user = jdbcTemplate.query("select * from \"User\" where name = ?", rowMapper, name);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(user.get(0));
    }
}
