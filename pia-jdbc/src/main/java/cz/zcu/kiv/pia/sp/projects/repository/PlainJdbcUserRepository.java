package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.UUID;

/**
 * Plain JDBC-based implementation of {@link UserRepository}.
 */
//@Primary
//@Repository
public class PlainJdbcUserRepository implements UserRepository {
    private static final UserMapper USER_MAPPER = new UserMapper();
    private final DataSource dataSource;

    public PlainJdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    @Override
    public Mono<User> registerUser(User user) {
        var sql = "INSERT INTO user (id, firstname, lastname, username, password, role, workplace, email) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?, ?)";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, user.getId().toString());
            statement.setString(2, user.getFirstname());
            statement.setString(3, user.getLastname());
            statement.setString(4, user.getUsername());
            statement.setString(5, user.getPassword());
            statement.setString(6, user.getRole());
            statement.setString(7, user.getWorkplace());
            statement.setString(8, user.getEmail());
            var rowsUpdated = statement.executeUpdate();

            return rowsUpdated == 1 ? Mono.just(user) : Mono.empty();

        } catch (SQLException e) {
            return Mono.empty();
        }
    }

    @Override
    public Mono<User> updateUser(UUID id, String firstName, String lastName, String username, String password, String role, String workplace, String email) {
        var sql = "UPDATE user SET firstname = ?, lastname = ? , password = ?, role = ?, workplace = ?, email = ? WHERE username = ?";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, password);
            statement.setString(4, role);
            statement.setString(5, workplace);
            statement.setString(6, email);
            statement.setString(7, username);
            var rowsUpdated = statement.executeUpdate();

            return rowsUpdated == 1 ?findUserByUsername(username) : Mono.empty();

        } catch (SQLException e) {
            return Mono.empty();
        }
    }

    @Override
    public Mono<User> findById(UUID id) {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user WHERE id = UUID_TO_BIN(?)";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, id.toString());
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            return Mono.justOrEmpty(user);

        } catch (SQLException e) {
            return Mono.empty();
        }
    }


    @Override
    public Mono<UserDetails> findByUsername(String username) {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user WHERE username = ?";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, username);
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            return Mono.justOrEmpty(user);

        } catch (SQLException e) {
            return Mono.empty();
        }
    }

    @Override
    public Mono<User> findUserByUsername(String username) {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user WHERE username = ?";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, username);
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            return Mono.just(user);

        } catch (SQLException e) {
            return Mono.empty();
        }
    }

    @Override
    public Flux<User> findUsersByProjectId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id), p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE p.id = UUID_TO_BIN(?)
        """;

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, id.toString());
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            assert user != null;
            return Flux.just(user);

        } catch (SQLException e) {
            return Flux.empty();
        }
    }

    @Override
    public Flux<User> findUsersByManagerId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id), p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE p.manager_id = UUID_TO_BIN(?)
        """;

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, id.toString());
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            assert user != null;
            return Flux.just(user);

        } catch (SQLException e) {
            return Flux.empty();
        }
    }

    @Override
    public Flux<User> findAll() {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            assert user != null;
            return Flux.just(user);

        } catch (SQLException e) {
            return Flux.empty();
        }
    }

    @Override
    public Flux<User> getOnlyAssignedUsers() {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        """;

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            assert user != null;
            return Flux.just(user);

        } catch (SQLException e) {
            return Flux.empty();
        }
    }

    @Override
    public Mono<Subordinate> registerSubordinate(Subordinate subordinate) {
        var sql = "INSERT INTO subordinate (id, superior_id, subordinate_id) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?))";

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, subordinate.getId().toString());
            statement.setString(2, subordinate.getSuperior_id().toString());
            statement.setString(3, subordinate.getSubordinate_id().toString());
            var rowsUpdated = statement.executeUpdate();

            return rowsUpdated == 1 ? Mono.just(subordinate) : Mono.empty();

        } catch (SQLException e) {
            return Mono.empty();
        }
    }

    @Override
    public Flux<User> findSubordinatesBySuperiorId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(s.id) AS record_id, BIN_TO_UUID(s.superior_id) AS superior_id, BIN_TO_UUID(s.subordinate_id) AS subordinate_id,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM subordinate s
        JOIN user u ON s.subordinate_id = u.id
        WHERE s.superior_id = UUID_TO_BIN(?)
        """;

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            statement.setString(1, id.toString());
            var resultSet = statement.executeQuery();

            var user = USER_MAPPER.mapRow(resultSet, resultSet.getRow());

            assert user != null;
            return Flux.just(user);

        } catch (SQLException e) {
            return Flux.empty();
        }
    }

    @Override
    public Mono<User> joinProject(String username, Project project) {
        var sql = """
        INSERT INTO assignment (id, worker_id, job_id, scope, time_from, time_to, note, status)
        VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, ?, ?, ?)
        """;

        try (var statement = dataSource.getConnection().prepareStatement(sql)) {
            var user = findUserByUsername(username).block();
            Assignment assignment = new Assignment(user.getId(), project.getId(), 0, FMT.parse("1000-01-01", Instant::from), FMT.parse("1000-01-01", Instant::from), "newly assigned", "Draft");
            statement.setString(1, assignment.getId().toString());
            statement.setString(2, assignment.getWorker_id().toString());
            statement.setString(3, assignment.getJob_id().toString());
            statement.setString(4, String.valueOf(assignment.getScope()));
            statement.setString(5, assignment.getFrom().toString());
            statement.setString(6, assignment.getTo().toString());
            statement.setString(7, assignment.getNote());
            statement.setString(8, assignment.getStatus());
            var rowsUpdated = statement.executeUpdate();

            return rowsUpdated == 1 ? Mono.just(user) : Mono.empty();

        } catch (SQLException e) {
            return Mono.empty();
        }
    }
}
