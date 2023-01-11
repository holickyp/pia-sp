package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * rozhrani, projekty v pameti a oprace nad nima
 */
public interface ProjectRepository {

    /**
     * Creates a new project.
     * @param project Project to be created
     * @return Created project
     */
    Mono<Project> createProject(Project project);

    /**
     * aktualizuje projekt na zadane hodnoty
     * @param id id projektu
     * @param name nazev projektu
     * @param from cas od
     * @param to cas do
     * @param description popis projektu
     * @return aktualizovany Projekt
     */
    Mono<Project> updateProject(UUID id, String name, Instant from, Instant to, String description);

    /**
     * Retrieves all projects.
     * @return Found projects
     */
    Flux<Project> findAll();

    /**
     * Searches projects by (partial) name.
     * @param name Searched project name
     * @return Found projects
     */
    Flux<Project> findByName(String name);

    /**
     * Searches projects by (full) name.
     * @param name Searched project name
     * @return Found project
     */
    Mono<Project> findProjectMatchingName(String name);

    /**
     * Retrieves project by its unique identifier
     *
     * @param id Unique project identifier
     * @return Found project
     */
    Mono<Project> findById(UUID id);

    /**
     * najde projekt podle id manazera
     * @param id manager id
     * @return Found project
     */
    Flux<Project> findByUserId(UUID id);

    /**
     * prida uzivatele do zadaneho projektu
     * @param id id projektu do ktereho se uzivatel prida
     * @param user uzivatel
     * @return Project
     */
    Mono<Project> joinProject(UUID id, User user);
}
