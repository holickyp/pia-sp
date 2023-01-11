package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.repository.ProjectRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Definuje operace nad projekty
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

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
     *
     * @param project Project to create
     * @return Created project
     */
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<Project> createProject(Project project) {
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
}
