package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.JdbcConfig;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import org.junit.jupiter.api.Assertions;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * testy nad ProjectRepository tridou
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ITConfiguration.class,
        JdbcConfig.class
})
@Testcontainers
@Transactional
@Sql(scripts={"classpath:JdbcProjectRepositoryIT.sql"})
public class JdbcProjectRepositoryIT {
    // will be shared between test methods
    @Container
    private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8")
            .withDatabaseName("pia_sp");

    @Autowired
    private ProjectRepository projectRepository;

    @DynamicPropertySource
    static void registerJdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jdbc.driver-class-name", mySQLContainer::getDriverClassName);
        registry.add("spring.jdbc.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.jdbc.username", mySQLContainer::getUsername);
        registry.add("spring.jdbc.password", mySQLContainer::getPassword);
    }

    @Test
    void createProject() {
        //stejny user jako testujici user ve sql skriptu
        User project_manager = new User(UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029"), "John", "Doe", "testuser", new BCryptPasswordEncoder().encode("password"), "SECRETARIAT", "fav", "johndoe@zcu.cz");
        Project project = new Project("test", project_manager, Instant.parse("2023-01-05T23:00:00Z"), Instant.parse("2023-01-05T23:00:00Z"), "test");


        projectRepository.createProject(project).block();
        var result = projectRepository.findById(project.getId()).block();

        assertEquals(project.getId(), result.getId());
        assertEquals(project.getName(), result.getName());
        assertEquals(project_manager, result.getManager());
        assertEquals(project.getFrom(), result.getFrom());
        assertEquals(project.getTo(), result.getTo());
        assertEquals(project.getDescription(), result.getDescription());
    }

    @Test
    void updateProject() {
        var project_id = UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var project_name = "test update";
        var project_from = Instant.parse("2023-01-05T23:00:00Z");
        var project_to = Instant.parse("2023-01-05T23:00:00Z");
        var project_desc = "test update desc";
        var result = projectRepository.updateProject(project_id, project_name, project_from, project_to, project_desc).block();

        assertEquals(project_id, result.getId());
        assertEquals(project_name, result.getName());
        assertEquals(project_from, result.getFrom());
        assertEquals(project_to, result.getTo());
        assertEquals(project_desc, result.getDescription());
    }

    @Test
    void findAll() {
        var result = projectRepository.findAll().collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void findByName() {
        var result = projectRepository.findByName("Default project").collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void findProjectMatchingName() {
        var project_id = UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var result = projectRepository.findProjectMatchingName("Test project").block();

        assertEquals(project_id, result.getId());
    }

    @Test
    void findById() {
        var project_id = UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var result = projectRepository.findById(project_id).block();

        assertEquals(project_id, result.getId());
    }

    @Test
    void findByUserId() {
        var user_id = UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029");
        var result = projectRepository.findByUserId(user_id).collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void joinProject() {
        //stejny jako v sql skriptu
        User project_manager = new User(UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029"), "John", "Doe", "testuser", new BCryptPasswordEncoder().encode("password"), "SECRETARIAT", "fav", "johndoe@zcu.cz");
        Project project = new Project(UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f"), "Test project", project_manager, Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2023-01-01T00:00:00Z"), "Test project description");

        //Assignment assignment = new Assignment(UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029"), UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f"), 20, Instant.parse("2023-01-05T23:00:00Z"), Instant.parse("2023-01-05T23:00:00Z"), "test", "Past");

        //spravne by melo spadnot jelikoz assigment mezi timto user a project uz existuje (vytvoren v sql skriptu)
        //testovat vytvoreni prirazeni nelze bud kvuli:
        // 1) uniqui key constraints
        // 2) nelze ziskat nove vytvoreny assignment pro porovnani
        Assertions.assertThrows(org.springframework.dao.DuplicateKeyException.class, () -> {
            projectRepository.joinProject(project.getId(), project_manager);
        });
    }
}
