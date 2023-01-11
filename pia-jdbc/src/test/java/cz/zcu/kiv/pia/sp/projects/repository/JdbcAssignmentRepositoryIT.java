package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.JdbcConfig;
import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * testy nad AssignmentRepository tridou
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ITConfiguration.class,
        JdbcConfig.class
})
@Testcontainers
@Transactional
@Sql(scripts={"classpath:JdbcAssignmentRepositoryIT.sql"})
public class JdbcAssignmentRepositoryIT {
    @Container
    private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8")
            .withDatabaseName("pia_sp");

    @Autowired
    private AssignmentRepository assignmentRepository;

    @DynamicPropertySource
    static void registerJdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jdbc.driver-class-name", mySQLContainer::getDriverClassName);
        registry.add("spring.jdbc.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.jdbc.username", mySQLContainer::getUsername);
        registry.add("spring.jdbc.password", mySQLContainer::getPassword);
    }

    @Test
    void createAssignment() {
        Assignment assignment = new Assignment(UUID.fromString("117219c1-525a-44b1-93d3-383008a5a029"), UUID.fromString("22ae2ef7-8cf5-48d3-b03f-d137a5d43b1f"), 20, Instant.parse("2023-01-05T23:00:00Z"), Instant.parse("2023-01-05T23:00:00Z"), "test", "Past");

        assignmentRepository.createAssignment(assignment).block();
        var result = assignmentRepository.findById(assignment.getId()).block();

        assertEquals(assignment.getId(), result.getId());
        assertEquals(assignment.getWorker_id(), result.getWorker_id());
        assertEquals(assignment.getJob_id(), result.getJob_id());
        assertEquals(assignment.getScope(), result.getScope());
        assertEquals(assignment.getFrom(), result.getFrom());
        assertEquals(assignment.getTo(), result.getTo());
        assertEquals(assignment.getNote(), result.getNote());
        assertEquals(assignment.getStatus(), result.getStatus());
    }

    @Test
    void updateAssignment() {
        var assignment_id = UUID.fromString("99ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var assignment_scope = 10;
        var assignment_from = Instant.parse("2023-01-05T23:00:00Z");
        var assignment_to = Instant.parse("2023-01-05T23:00:00Z");
        var assignment_note = "test update note";
        var assignment_status= "Canceled";
        var result = assignmentRepository.updateAssignment(assignment_id, assignment_scope, assignment_from, assignment_to, assignment_note, assignment_status).block();

        assertEquals(assignment_id, result.getId());
        assertEquals(assignment_scope, result.getScope());
        assertEquals(assignment_from, result.getFrom());
        assertEquals(assignment_to, result.getTo());
        assertEquals(assignment_note, result.getNote());
        assertEquals(assignment_status, result.getStatus());
    }

    @Test
    void updateAssignmentsStatuses() {
         assignmentRepository.updateAssignmentsStatuses();
        var result = assignmentRepository.findAll().collectList().block();

        assertFalse(result.isEmpty());
        //LocalDate localDate = LocalDate.now();
        //LocalDate time_to = result.get(0).getTo().atZone(ZoneId.systemDefault()).toLocalDate();
        //System.out.println(localDate.isAfter(time_to));
        assertEquals("Past", result.get(0).getStatus());
    }

    @Test
    void findById() {
        var assignment_id = UUID.fromString("99ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var result = assignmentRepository.findById(assignment_id).block();

        assertEquals(assignment_id, result.getId());
    }

    @Test
    void findByUserId() {
        var user_id = UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029");
        var result = assignmentRepository.findByUserId(user_id).collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void findByProjectId() {
        var project_id = UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var result = assignmentRepository.findByUserId(project_id).collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void findAll() {
        var result = assignmentRepository.findAll().collectList().block();

        assertFalse(result.isEmpty());
    }
}
