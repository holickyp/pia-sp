package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.JdbcConfig;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
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

import static org.junit.jupiter.api.Assertions.*;


/**
 * testy nad UserRepository tridou
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ITConfiguration.class,
        JdbcConfig.class
})
@Testcontainers
@Transactional
@Sql(scripts={"classpath:JdbcUserRepositoryIT.sql"})
public class JdbcUserRepositoryIT {
    // will be shared between test methods
    @Container
    private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8")
            .withDatabaseName("pia_sp");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void registerJdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jdbc.driver-class-name", mySQLContainer::getDriverClassName);
        registry.add("spring.jdbc.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.jdbc.username", mySQLContainer::getUsername);
        registry.add("spring.jdbc.password", mySQLContainer::getPassword);
    }

    @Test
    void registerUser() {
        User user = new User("John", "Doe", "new", new BCryptPasswordEncoder().encode("password"), "SECRETARIAT", "fav", "johndoe@zcu.cz");

        userRepository.registerUser(user).block();
        var result = userRepository.findById(user.getId()).block();

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getFirstname(), result.getFirstname());
        assertEquals(user.getLastname(), result.getLastname());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getRole(), result.getRole());
        assertEquals(user.getWorkplace(), result.getWorkplace());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void updateUser() {
        var user_id = UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029");
        var user_firstname = "test";
        var user_lastname = "test";
        var user_username = "testuser";
        var user_password = new BCryptPasswordEncoder().encode("test");
        var user_role= "test";
        var user_workplace= "test";
        var user_email= "test@test";
        var result = userRepository.updateUser(user_id, user_firstname, user_lastname, user_username, user_password, user_role, user_workplace, user_email).block();

        assertEquals(user_id, result.getId());
        assertEquals(user_firstname, result.getFirstname());
        assertEquals(user_lastname, result.getLastname());
        assertEquals(user_username, result.getUsername());
        assertTrue(new BCryptPasswordEncoder().matches("test", result.getPassword()));
        assertEquals(user_role, result.getRole());
        assertEquals(user_workplace, result.getWorkplace());
        assertEquals(user_email, result.getEmail());
    }

    @Test
    void findById() {
        var user_id = UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029");
        var result = userRepository.findById(user_id).block();

        assertEquals(user_id, result.getId());
    }

    @Test
    void findByUsername() {
        var user_username = "testuser";
        var result = userRepository.findByUsername(user_username).block();

        assertEquals(user_username, result.getUsername());
    }

    @Test
    void findUserByUsername() {
        var user_username = "testuser";
        var result = userRepository.findUserByUsername(user_username).block();

        assertEquals(user_username, result.getUsername());
    }

    @Test
    void findUsersByProjectId() {
        var project_id = UUID.fromString("75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f");
        var result = userRepository.findUsersByProjectId(project_id).collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void findUsersByManagerId() {
        var project_id = UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029");
        var result = userRepository.findUsersByManagerId(project_id).collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void findAll() {
        var result = userRepository.findAll().collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void getOnlyAssignedUsers() {
        var result = userRepository.getOnlyAssignedUsers().collectList().block();

        assertFalse(result.isEmpty());
    }

    @Test
    void registerSubordinate() {
        Subordinate subordinate = new Subordinate(UUID.fromString("227219c1-525a-44b1-93d3-383008a5a029"), UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029"));
        userRepository.registerSubordinate(subordinate).block();

        var result = userRepository.findSubordinatesBySuperiorId(UUID.fromString("227219c1-525a-44b1-93d3-383008a5a029")).collectList().block();

        assertFalse(result.isEmpty());
        assertEquals(subordinate.getSubordinate_id(), result.get(0).getId());
    }

    @Test
    void findSubordinatesBySuperiorId() {
        var superior_id = UUID.fromString("567219c1-525a-44b1-93d3-383008a5a029");
        var result = userRepository.findSubordinatesBySuperiorId(superior_id).collectList().block();

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
            userRepository.joinProject(project_manager.getUsername(), project);
        });
    }
}
