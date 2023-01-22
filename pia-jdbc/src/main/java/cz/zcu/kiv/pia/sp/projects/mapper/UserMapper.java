package cz.zcu.kiv.pia.sp.projects.mapper;

import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.enums.Role;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Maps {@link ResultSet} to {@link User}.
 */
public class UserMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getObject("user_id", String.class);
        String firstName = rs.getObject("firstname", String.class);
        String lastName = rs.getObject("lastname", String.class);
        String username = rs.getObject("username", String.class);
        String password = rs.getObject("password", String.class);
        String role = rs.getObject("role", String.class);
        String workplace = rs.getObject("workplace", String.class);
        String email = rs.getObject("email", String.class);
        return new User(UUID.fromString(id), firstName, lastName, username, password, Role.getRoleByString(role), workplace, email);
    }
}
