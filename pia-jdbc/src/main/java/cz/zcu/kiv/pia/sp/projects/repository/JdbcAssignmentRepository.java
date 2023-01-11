package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.mapper.AssignmentMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * implementace AssignmentRepository, databazova varianta
 */
@Primary
@Repository
public class JdbcAssignmentRepository implements AssignmentRepository {
    private static final AssignmentMapper ASSIGNMENT_MAPPER = new AssignmentMapper();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAssignmentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * vytvor novy assignment
     * @param assignment assignment
     * @return assignment
     */
    @Override
    public Mono<Assignment> createAssignment(Assignment assignment) {
        var sql = """
        INSERT INTO assignment (id, worker_id, job_id, scope, time_from, time_to, note, status)
        VALUES (UUID_TO_BIN(:id), UUID_TO_BIN(:workerId), UUID_TO_BIN(:jobId), :scope, :time_from, :time_to, :note, :status)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", assignment.getId().toString());
        params.addValue("workerId", assignment.getWorker_id().toString());
        params.addValue("jobId", assignment.getJob_id().toString());
        params.addValue("scope", String.valueOf(assignment.getScope()));
        params.addValue("time_from", assignment.getFrom());
        params.addValue("time_to", assignment.getTo());
        params.addValue("note", assignment.getNote());
        params.addValue("status", assignment.getStatus());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? Mono.just(assignment) : Mono.empty();
    }

    /**
     * aktualizuje assignment na zadane hodnoty
     * @param id id pro identifikaci
     * @param scope uvazek
     * @param from cas od
     * @param to cas do
     * @param note poznamka
     * @param status stav
     * @return aktualizovany Assignment
     */
    @Override
    public Mono<Assignment> updateAssignment(UUID id, double scope, Instant from, Instant to, String note, String status) {
        var sql = """
        UPDATE assignment
        SET scope = :scope, time_from = :time_from, time_to = :time_to , note = :note, status = :status
        WHERE id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("scope", scope);
        params.addValue("time_from", from);
        params.addValue("time_to", to);
        params.addValue("note", note);
        params.addValue("status", status);
        params.addValue("id", id.toString());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1 ? findById(id) : Mono.empty();
    }

    /**
     * projede vsechny assignments a aktualizuje jejich stav na Past pokud cas do uz probehl
     * @return void
     */
    @Override
    public Mono<Void> updateAssignmentsStatuses() {
        var assignments = findAll();

        LocalDate localDate = LocalDate.now();
        for(Assignment assignment : assignments.toIterable()) {
            LocalDate time_to = assignment.getTo().atZone(ZoneId.systemDefault()).toLocalDate();
            if(localDate.isAfter(time_to)) {
                updateAssignment(assignment.getId(), assignment.getScope(), assignment.getFrom(), assignment.getTo(), assignment.getNote(), "Past");
            }
        }
        return Mono.empty();
    }

    /**
     * najde assignment podle od
     * @param id assignment id
     * @return assignment
     */
    @Override
    public Mono<Assignment> findById(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id), p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE a.id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        try {
            var result = jdbcTemplate.queryForObject(sql, params, ASSIGNMENT_MAPPER);

            return Mono.justOrEmpty(result);

        } catch (DataAccessException e) {
            return Mono.empty();
        }
    }

    /**
     * najde assignment podle worker_id
     * @param id id uzivatele
     * @return assignment
     */
    @Override
    public Flux<Assignment> findByUserId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id), p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE a.worker_id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        try {
            var result = jdbcTemplate.query(sql, params, ASSIGNMENT_MAPPER);

            return Flux.fromIterable(result);

        } catch (DataAccessException e) {
            return Flux.empty();
        }
    }

    /**
     * najde assignment podle job_id
     * @param id id projektu
     * @return assignment
     */
    @Override
    public Flux<Assignment> findByProjectId(UUID id) {
        var sql = """
        SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
        BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id), p.time_from, p.time_to, p.description,
        BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
        FROM assignment a
        JOIN user u ON a.worker_id = u.id
        JOIN project p ON a.job_id = p.id
        WHERE a.job_id = UUID_TO_BIN(:id)
        """;

        var params = new MapSqlParameterSource();
        params.addValue("id", id.toString());

        try {
            var result = jdbcTemplate.query(sql, params, ASSIGNMENT_MAPPER);

            return Flux.fromIterable(result);

        } catch (DataAccessException e) {
            return Flux.empty();
        }
    }

    /**
     * vrati vsechny assignments
     * @return assignments
     */
    @Override
    public Flux<Assignment> findAll() {
        var sql = """
            SELECT BIN_TO_UUID(a.id) AS assignment_id, BIN_TO_UUID(a.worker_id) AS worker_id, BIN_TO_UUID(a.job_id) AS job_id, a.scope, a.time_from, a.time_to, a.note, a.status,
            BIN_TO_UUID(p.id) AS project_id, p.name, BIN_TO_UUID(p.manager_id), p.time_from, p.time_to, p.description,
            BIN_TO_UUID(u.id) AS user_id, u.firstname, u.lastname, u.username, u.password, u.role, u.workplace, u.email
            FROM assignment a
            JOIN user u ON a.worker_id = u.id
            JOIN project p ON a.job_id = p.id
            """;

        var result = jdbcTemplate.query(sql, ASSIGNMENT_MAPPER);

        return Flux.fromIterable(result);
    }
}
