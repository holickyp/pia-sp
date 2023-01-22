package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.enums.MinDates;
import cz.zcu.kiv.pia.sp.projects.enums.Role;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import cz.zcu.kiv.pia.sp.projects.mapper.SubordinateMapper;
import cz.zcu.kiv.pia.sp.projects.mapper.UserMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.UUID;

/**
 * JDBC-based implementation of {@link UserRepository}.
 */
@Primary
@Repository
public class JdbcUserRepository implements UserRepository {
    private static final UserMapper USER_MAPPER = new UserMapper();

    private static final SubordinateMapper SUBORDINATE_MAPPER = new SubordinateMapper();
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    public JdbcUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Registers user
     *
     * @param user User to be registered
     * @return Registered user
     */
    @Override
    public Mono<User> registerUser(User user) {
        var sql = """
        INSERT INTO user (id, firstname, lastname, username, password, role, workplace, email)
        VALUES (UUID_TO_BIN(:id), :firstname, :lastname, :username, :password, :role, :workplace, :email)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", user.getId().toString());
        params.addValue("firstname", user.getFirstname());
        params.addValue("lastname", user.getLastname());
        params.addValue("username", user.getUsername());
        params.addValue("password", user.getPassword());
        params.addValue("role", user.getRole().toString());
        params.addValue("workplace", user.getWorkplace());
        params.addValue("email", user.getEmail());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? Mono.just(user) : Mono.empty();
    }

    /**
     * aktualizuje uzivatele na zadane hodnoty
     * @param id id uzivatele
     * @param firstName kresni jmeno
     * @param lastName prijmeni
     * @param username uzivatelske jmeno
     * @param password helso
     * @param role role
     * @param workplace pracoviste
     * @param email email
     * @return aktualizovany uzivatel
     */
    @Override
    public Mono<User> updateUser(UUID id, String firstName, String lastName, String username, String password, Role role, String workplace, String email) {
        var sql = """
        UPDATE user
        SET firstname = :firstname, lastname = :lastname , password = :password, role = :role, workplace = :workplace, email = :email
        WHERE username = :username
        """;

        var params = new MapSqlParameterSource();
        params.addValue("firstname", firstName);
        params.addValue("lastname", lastName);
        params.addValue("password", password);
        params.addValue("role", role.toString());
        params.addValue("workplace", workplace);
        params.addValue("email", email);
        params.addValue("username", username);

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? findById(id) : Mono.empty();
    }

    /**
     * najde uzivatele podle id
     * @param id id uzivatele
     * @return User
     */
    @Override
    public Mono<User> findById(UUID id) {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user WHERE id = UUID_TO_BIN(:id)";

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        try {
            var user = jdbcTemplate.queryForObject(sql, params, USER_MAPPER);

            return Mono.justOrEmpty(user);

        } catch (DataAccessException e) {
            return Mono.empty();
        }
    }

    /**
     * Finds user by his unique username
     *
     * @param username User's unique username
     * @return Found user
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user WHERE username = :username";

        var params = new MapSqlParameterSource();
        params.addValue("username", username);

        try {
            var user = jdbcTemplate.queryForObject(sql, params, USER_MAPPER);

            return Mono.justOrEmpty(user);

        } catch (DataAccessException e) {
            return Mono.empty();
        }
    }

    /**
     * Finds user by his unique username
     *
     * @param username User's unique username
     * @return Found user
     */
    @Override
    public Mono<User> findUserByUsername(String username) {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user WHERE username = :username";

        var params = new MapSqlParameterSource();
        params.addValue("username", username);

        try {
            var user = jdbcTemplate.queryForObject(sql, params, USER_MAPPER);

            return Mono.justOrEmpty(user);

        } catch (DataAccessException e) {
            return Mono.empty();
        }
    }

    /**
     * vrati vsechny uzivatele prirazeny v danem projektu
     * @param id project id
     * @return vschni uzivatele v danem projektu
     */
    @Override
    public Flux<User> findUsersByProjectId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE p.id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        var result = jdbcTemplate.query(sql, params, USER_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * vrati vsechny uzivatele prirazeny v projektu kde je dany uzivatel manager
     * @param id manager id
     * @return vschni uzivatele v danem projektu
     */
    @Override
    public Flux<User> findUsersByManagerId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE p.manager_id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        var result = jdbcTemplate.query(sql, params, USER_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * vrati vsechny uzivatele
     * @return All users
     */
    @Override
    public Flux<User> findAll() {
        var sql = "SELECT BIN_TO_UUID(id) AS user_id, firstname, lastname, username, password, role, workplace, email FROM user";

        var result = jdbcTemplate.query(sql, USER_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * vrati jen uzivatele kteri jsou v nejakem projektu
     * @return only assigned users
     */
    @Override
    public Flux<User> getOnlyAssignedUsers() {
        var sql = """
        SELECT DISTINCT BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM user u
        JOIN (SELECT worker_id FROM assignment) a
        WHERE u.id = a.worker_id
        """;

        var result = jdbcTemplate.query(sql, USER_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * vytvori novy zaznam pro Subordinates
     * @param subordinate new Subordinate
     * @return Subordinate
     */
    @Override
    public Mono<Subordinate> registerSubordinate(Subordinate subordinate) {
        var sql = """
        INSERT INTO subordinate (id, superior_id, subordinate_id)
        VALUES (UUID_TO_BIN(:id), UUID_TO_BIN(:superiorId), UUID_TO_BIN(:subordinateId))
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", subordinate.getId().toString());
        params.addValue("superiorId", subordinate.getSuperior_id().toString());
        params.addValue("subordinateId", subordinate.getSubordinate_id().toString());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? Mono.just(subordinate) : Mono.empty();
    }

    /**
     * vrati vsechny podrizene podle daneho nadrizeneho
     * @param id superior id
     * @return all of theirs subordinates
     */
    @Override
    public Flux<User> findSubordinatesBySuperiorId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(s.id) AS record_id, BIN_TO_UUID(s.superior_id) AS superior_id, BIN_TO_UUID(s.subordinate_id) AS subordinate_id,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM subordinate s
        JOIN user u ON s.subordinate_id = u.id
        WHERE s.superior_id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        var result = jdbcTemplate.query(sql, params, USER_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * prida uzivatele do projektu
     * @param username uzivatelske jmeno
     * @param project projekt
     * @return User
     */
    @Override
    public Mono<User> joinProject(String username, Project project) {
        var sql = """
        INSERT INTO assignment (id, worker_id, job_id, scope, time_from, time_to, note, status)
        VALUES (UUID_TO_BIN(:id), UUID_TO_BIN(:workerId), UUID_TO_BIN(:jobId), :scope, :time_from, :time_to, :note, :status)
        """;

        var params = new MapSqlParameterSource();
        var user = findUserByUsername(username).block();
        Assignment assignment = new Assignment(user.getId(), project.getId(), 0, FMT.parse(MinDates.DEFAULT_DATE.toString(), Instant::from), FMT.parse(MinDates.DEFAULT_DATE.toString(), Instant::from), "newly assigned", Status.DRAFT);
        params.addValue("id", assignment.getId().toString());
        params.addValue("workerId", assignment.getWorker_id().toString());
        params.addValue("jobId", assignment.getJob_id().toString());
        params.addValue("scope", String.valueOf(assignment.getScope()));
        params.addValue("time_from", assignment.getFrom());
        params.addValue("time_to", assignment.getTo());
        params.addValue("note", assignment.getNote());
        params.addValue("status", assignment.getStatus().toString());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? Mono.just(user) : Mono.empty();
    }
}
