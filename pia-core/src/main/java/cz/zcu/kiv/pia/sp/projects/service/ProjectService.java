package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.error.InvalidDateException;
import cz.zcu.kiv.pia.sp.projects.error.ProjectAlreadyExistException;
import cz.zcu.kiv.pia.sp.projects.repository.ProjectRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.UUID;

/**
 * Definuje operace nad projekty
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    /** vychozi projekt */
    public static final Project DEFAULT_PROJECT = new Project(
            "default project",
            UserService.DEFAULT_USER,
            Instant.now(),
            Instant.now(),
            "default in memory project description");

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Searches projects by (partial) name or returns all when name is blank.
     *
     * @param name Searched project name
     * @return Found projects
     */
    public Flux<Project> searchProjects(String name) {
        if (StringUtils.hasLength(name)) {
            return projectRepository.findByName(name);
        } else {
            return projectRepository.findAll();
        }
    }

    /**
     * Creates a new project.
     * @param name name of project
     * @param manager project's manager
     * @param from starting date
     * @param to ending date
     * @param description project description
     * @return created project
     */
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<Project> createProject(String name, User manager, String from, String to, String description) {
        Instant timeFrom = FMT.parse(from, Instant::from);
        Instant timeTo = FMT.parse(to, Instant::from);
        Project project = new Project(name, manager, timeFrom, timeTo, description);
        return Mono.just(project).flatMap(projectRepository::createProject);
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
    @Transactional
    @Secured({"ROLE_SECRETARIAT", "ROLE_DEPARTMENT-MANAGER", "ROLE_PROJECT-MANAGER"})
    public Mono<Project> updateProject(UUID id, String name, Instant from, Instant to, String description) {
        return projectRepository.updateProject(id, name, from, to, description);
    }

    /**
     * tet zda je zadany datum validni (konecny datum neni pred zacatkem)
     * @param start startovni datum
     * @param end koncovy datum
     * @return true -> je validni | false -> throws InvalidDateException pokud neni validni
     */
    public Mono<Boolean> isDateValid(String start, String end) {
        Instant from = FMT.parse(start, Instant::from);
        Instant to = FMT.parse(end, Instant::from);
        LocalDate time_from = from.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate time_to = to.atZone(ZoneId.systemDefault()).toLocalDate();
        if(time_from.isAfter(time_to)) {
            throw new InvalidDateException("Invalid dates. Target date must be higher than starting date");
        }
        return Mono.just(true);
    }

    /**
     * Retrieves project by its identifier
     *
     * @param id Unique project identifier
     * @return Found project
     */
    public Mono<Project> getProject(UUID id) {
        return projectRepository.findById(id);
    }

    /**
     * Searches projects by (full) name.
     * @param name Searched project name
     * @return Found projects
     */
    public Mono<Project> getProjectByName(String name) {
        return projectRepository.findProjectMatchingName(name);
    }

    /**
     * test zd projekt jiz existuje
     * @param name jmeno projektu
     * @return false -> neexistuje | true -> throws ProjectAlreadyExistException pokud existuje
     */
    public Mono<Boolean> projectExists(String name) {
        Project project = projectRepository.findProjectMatchingName(name).block();
        if(project == null) {
            return Mono.just(false);
        } else {
            throw new ProjectAlreadyExistException("project already exists");
        }
    }

    /**
     * najde projekt podle id manazera
     * @param id manager id
     * @return Found project
     */
    public Flux<Project> getProjectByUserId(UUID id) {
        return projectRepository.findByUserId(id);
    }

    /**
     * Joins user to project with given identifier
     *
     * @param id Unique project identifier
     * @param user User joining project
     * @return Project
     */
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<Project> joinProject(UUID id, User user) {
        return projectRepository.joinProject(id, user);
    }

    /**
     * joins giben user into projects
     * @param user user
     * @return user's projects
     */
    public Flux<Project> joinUserIntoProjects(User user) {
        var projects = projectRepository.findByUserId(user.getId());
        for(Project project : projects.toIterable()) {
            user.join(project);
        }
        return projects;
    }
}
