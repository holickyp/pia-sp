package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.service.ProjectService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * implementace UserRepository rozhrani, pametova varianta
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    /** mapa uchovavajici Users */
    private final Map<UUID, User> usersMap;
    /** mapa uchovavajici Subordinates */
    private final Map<UUID, Subordinate> subordinateMap;

    /**
     * constructor
     */
    public InMemoryUserRepository() {
        this.usersMap = new HashMap<>();
        this.subordinateMap = new HashMap<>();
    }

    @PostConstruct
    private void postConstruct() {
        usersMap.put(UserService.DEFAULT_USER.getId(), UserService.DEFAULT_USER);
        usersMap.put(UserService.SECOND_USER.getId(), UserService.SECOND_USER);
        subordinateMap.put(UserService.DEFAULT_SUBORDINATE.getId(), UserService.DEFAULT_SUBORDINATE);
    }

    /**
     * Registers user
     *
     * @param user User to be registered
     * @return Registered user
     */
    @Override
    public Mono<User> registerUser(User user) {
        usersMap.put(user.getId(), user);

        return Mono.just(usersMap.get(user.getId()));
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
    public Mono<User> updateUser(UUID id, String firstName, String lastName, String username, String password, String role, String workplace, String email) {
        var updated_user = usersMap.get(id);
        updated_user.update(firstName, lastName, username, password, role, workplace, email);
        return Mono.just(updated_user);
    }

    /**
     * najde uzivatele podle id
     * @param id id uzivatele
     * @return User
     */
    @Override
    public Mono<User> findById(UUID id) {
        var user = usersMap.get(id);

        return Mono.just(user);
    }

    /**
     * Finds user by his unique username
     *
     * @param username User's unique username
     * @return Found user
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        for (User user : usersMap.values()) {
            if (user.getUsername().equals(username)) {
                return Mono.just(user);
            }
        }

        return Mono.empty();
    }

    /**
     * vrati vsechny uzivatele prirazeny v danem projektu
     * @param id project id
     * @return vschni uzivatele v danem projektu
     */
    @Override
    public Flux<User> findUsersByProjectId(UUID id) {
        Flux<User> users = Flux.empty();
        for (User user : usersMap.values()) {
            Flux.concat(users, Flux.just(user.getProjects().stream().filter(project -> project.getId().equals(id))));
        }
        return users;
    }

    /**
     * vrati vsechny uzivatele prirazeny v projektu kde je dany uzivatel manager
     * @param id manager id
     * @return vschni uzivatele v danem projektu
     */
    @Override
    public Flux<User> findUsersByManagerId(UUID id) {
        Flux<User> users = Flux.empty();
        for (User user : usersMap.values()) {
            Flux.concat(users, Flux.just(user.getProjects().stream().filter(project -> project.getManager().getId().equals(id))));
        }
        return users;
    }

    /**
     * Finds user by his unique username
     *
     * @param username User's unique username
     * @return Found user
     */
    @Override
    public Mono<User> findUserByUsername(String username) {
        for (User user : usersMap.values()) {
            if (user.getUsername().equals(username)) {
                return Mono.just(user);
            }
        }

        return Mono.empty();
    }

    /**
     * vrati vsechny uzivatele
     * @return All users
     */
    @Override
    public Flux<User> findAll() {
        var users = usersMap.values();

        return Flux.fromIterable(users);
    }

    /**
     * vrati jen uzivatele kteri jsou v nejakem projektu
     * @return only assigned users
     */
    @Override
    public Flux<User> getOnlyAssignedUsers() {
        var users = usersMap.values().stream().filter(user -> user.getProjects().isEmpty());

        return Flux.fromStream(users);
    }

    /**
     * prida uzivatele do projektu
     * @param username uzivatelske jmeno
     * @param project projekt
     * @return User
     */
    @Override
    public Mono<User> joinProject(String username, Project project) {
        var user = findUserByUsername(username).block();
        user.join(project);
        return Mono.just(user);
    }

    /**
     * vytvori novy zaznam pro Subordinates
     * @param subordinate new Subordinate
     * @return Subordinate
     */
    @Override
    public Mono<Subordinate> registerSubordinate(Subordinate subordinate) {
        subordinateMap.put(subordinate.getId(), subordinate);

        return Mono.just(subordinateMap.get(subordinate.getId()));
    }

    /**
     * vrati vsechny podrizene podle daneho nadrizeneho
     * @param id superior id
     * @return all of theirs subordinates
     */
    @Override
    public Flux<User> findSubordinatesBySuperiorId(UUID id) {
        var subordinates_id = Flux.fromStream(subordinateMap.values().stream().filter(subordinate -> subordinate.getSuperior_id().equals(id)));

        Flux<User> subordinates = Flux.empty();
        for (Subordinate subordinate : subordinates_id.toIterable()) {
            Flux.concat(subordinates, findById(subordinate.getSubordinate_id()));
        }

        return subordinates;
    }
}
