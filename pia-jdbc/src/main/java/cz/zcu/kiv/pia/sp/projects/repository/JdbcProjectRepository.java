package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.enums.MinDates;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import cz.zcu.kiv.pia.sp.projects.mapper.ProjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
 * JDBC-based implementation of {@link ProjectRepository}.
 */
@Primary
@Repository
public class JdbcProjectRepository implements ProjectRepository {
    private static final ProjectMapper PROJECT_MAPPER = new ProjectMapper();
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    public JdbcProjectRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a new project.
     * @param project Project to be created
     * @return Created project
     */
    @Override
    public Mono<Project> createProject(Project project) {
        var sql = """
        INSERT INTO project (id, name, manager_id, time_from, time_to, description)
        VALUES (UUID_TO_BIN(:id), :name, UUID_TO_BIN(:managerId), :time_from, :time_to, :description)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", project.getId().toString());
        params.addValue("name", project.getName());
        params.addValue("managerId", project.getManager().getId().toString());
        params.addValue("time_from", project.getFrom());
        params.addValue("time_to", project.getTo());
        params.addValue("description", project.getDescription());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? Mono.just(project) : Mono.empty();
    }

    /**
     * aktualizuje projekt na zadane hodnoty
     * @param id id projektu
     * @param name nazev projektu
     * @param from cas od
     * @param to cas do
     * @param description popis projektu
     * @return aktualizovany Projekt
     */
    @Override
    public Mono<Project> updateProject(UUID id, String name, Instant from, Instant to, String description) {
        var sql = """
        UPDATE project
        SET name = :name, time_from = :time_from , time_to = :time_to, description = :description
        WHERE id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("name", name);
        params.addValue("time_from", from);
        params.addValue("time_to", to);
        params.addValue("description", description);
        params.addValue("id", id.toString());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? findById(id) : Mono.empty();
    }

    /**
     * Retrieves all projects.
     * @return Found projects
     */
    @Override
    public Flux<Project> findAll() {
        var sql = """
        SELECT BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM project p
        JOIN user u ON p.manager_id = u.id
        """;

        var result = jdbcTemplate.query(sql, PROJECT_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * Searches projects by (partial) name.
     * @param name Searched project name
     * @return Found projects
     */
    @Override
    public Flux<Project> findByName(String name) {
        var sql = """
        SELECT BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM project p
        JOIN user u ON p.manager_id = u.id
        WHERE p.name LIKE :name
        """;

        var params = new MapSqlParameterSource();
        params.addValue("name", "%" + name + "%");

        var result = jdbcTemplate.query(sql, params, PROJECT_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * Searches projects by (full) name.
     * @param name Searched project name
     * @return Found project
     */
    @Override
    public Mono<Project> findProjectMatchingName(String name) {
        var sql = """
        SELECT BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM project p
        JOIN user u ON p.manager_id = u.id
        WHERE p.name = :name
        """;

        var params = new MapSqlParameterSource();
        params.addValue("name", name);

        try {
            var result = jdbcTemplate.queryForObject(sql, params, PROJECT_MAPPER);

            return Mono.justOrEmpty(result);

        } catch (DataAccessException e) {
            return Mono.empty();
        }
    }

    /**
     * Retrieves project by its unique identifier
     * @param id Unique project identifier
     * @return Found project
     */
    @Override
    public Mono<Project> findById(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM project p
        JOIN user u ON p.manager_id = u.id
        WHERE p.id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        try {
            var result = jdbcTemplate.queryForObject(sql, params, PROJECT_MAPPER);

            return Mono.justOrEmpty(result);

        } catch (DataAccessException e) {
            return Mono.empty();
        }
    }

    /**
     * najde projekt podle id manazera
     * @param id manager id
     * @return Found project
     */
    @Override
    public Flux<Project> findByUserId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id) AS manager_id, p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE u.id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        var result = jdbcTemplate.query(sql, params, PROJECT_MAPPER);

        return Flux.fromIterable(result);
    }

    /**
     * prida uzivatele do zadaneho projektu
     * @param id id projektu do ktereho se uzivatel prida
     * @param user uzivatel
     * @return Project
     */
    @Override
    public Mono<Project> joinProject(UUID id, User user) {
        var sql = """
        INSERT INTO assignment (id, worker_id, job_id, scope, time_from, time_to, note, status)
        VALUES (UUID_TO_BIN(:id), UUID_TO_BIN(:workerId), UUID_TO_BIN(:jobId), :scope, :time_from, :time_to, :note, :status)
        """;

        var params = new MapSqlParameterSource();
        Assignment assignment = new Assignment(user.getId(), id, 0, FMT.parse(MinDates.DEFAULT_DATE.toString(), Instant::from), FMT.parse(MinDates.DEFAULT_DATE.toString(), Instant::from), "newly assigned", Status.DRAFT);
        params.addValue("id", assignment.getId().toString());
        params.addValue("workerId", assignment.getWorker_id().toString());
        params.addValue("jobId", assignment.getJob_id().toString());
        params.addValue("scope", String.valueOf(assignment.getScope()));
        params.addValue("time_from", assignment.getFrom());
        params.addValue("time_to", assignment.getTo());
        params.addValue("note", assignment.getNote());
        params.addValue("status", assignment.getStatus().toString());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? findById(id) : Mono.empty();
    }
}
