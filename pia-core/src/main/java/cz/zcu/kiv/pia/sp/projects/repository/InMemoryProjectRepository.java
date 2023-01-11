package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.service.ProjectService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * implementace ProjectRepository rozhrani, pametova varianta
 */
@Repository
public class InMemoryProjectRepository implements ProjectRepository {
    /** mapa uchovavajici Projects */
    private final Map<UUID, Project> projectsMap;

    /**
     * constructor
     */
    public InMemoryProjectRepository() {
        this.projectsMap = new HashMap<>();
    }

    @PostConstruct
    private void postConstruct() {
        projectsMap.put(ProjectService.DEFAULT_PROJECT.getId(), ProjectService.DEFAULT_PROJECT);
    }

    /**
     * Creates a new project.
     * @param project Project to be created
     * @return Created project
     */
    @Override
    public Mono<Project> createProject(Project project) {
        projectsMap.put(project.getId(), project);

        return findById(project.getId());
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
        var updated_project = projectsMap.get(id);
        updated_project.update(name, from, to, description);
        return Mono.just(updated_project);
    }

    /**
     * Retrieves all projects.
     * @return Found projects
     */
    @Override
    public Flux<Project> findAll() {
        var projects = projectsMap.values();

        return Flux.fromIterable(projects);
    }

    /**
     * Searches projects by (partial) name.
     * @param name Searched project name
     * @return Found projects
     */
    @Override
    public Flux<Project> findByName(String name) {
        var projects = projectsMap.values()
                .stream()
                .filter(project -> project.getName().contains(name));

        return Flux.fromStream(projects);
    }

    /**
     * Searches projects by (full) name.
     * @param name Searched project name
     * @return Found project
     */
    @Override
    public Mono<Project> findProjectMatchingName(String name) {
        for(Project project : projectsMap.values()) {
            if(project.getName().equals(name)) {
                return Mono.just(project);
            }
        }

        return Mono.empty();
    }

    /**
     * Retrieves project by its unique identifier
     * @param id Unique project identifier
     * @return Found project
     */
    @Override
    public Mono<Project> findById(UUID id) {
        var project = projectsMap.get(id);

        return Mono.just(project);
    }

    /**
     * najde projekt podle id manazera
     * @param id manager id
     * @return Found project
     */
    @Override
    public Flux<Project> findByUserId(UUID id) {
        Flux<Project> projects = Flux.empty();
        for (Project project : projectsMap.values()) {
            Flux.concat(projects, Flux.just(project.getUsers().stream().filter(user -> user.getId().equals(id))));
        }
        return projects;
    }

    /**
     * prida uzivatele do zadaneho projektu
     * @param id id projektu do ktereho se uzivatel prida
     * @param user uzivatel
     * @return Project
     */
    @Override
    public Mono<Project> joinProject(UUID id, User user) {
        var project = projectsMap.get(id);
        project.join(user);
        return Mono.just(project);
    }
}
