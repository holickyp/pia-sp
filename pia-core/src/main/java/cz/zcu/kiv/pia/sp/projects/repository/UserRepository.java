package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * rozhrani, uzivatele v pameti a oprace nad nima
 */
public interface UserRepository {

    /**
     * Registers user
     *
     * @param user User to be registered
     * @return Registered user
     */
    Mono<User> registerUser(User user);

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
    Mono<User> updateUser(UUID id, String firstName, String lastName, String username, String password, String role, String workplace, String email);

    /**
     * najde uzivatele podle id
     * @param id id uzivatele
     * @return User
     */
    Mono<User> findById(UUID id);

    /**
     * Finds user by his unique username
     *
     * @param username User's unique username
     * @return Found user
     */
    Mono<UserDetails> findByUsername(String username);

    /**
     * Finds user by his unique username
     *
     * @param username User's unique username
     * @return Found user
     */
    Mono<User> findUserByUsername(String username);

    /**
     * vrati vsechny uzivatele prirazeny v danem projektu
     * @param id project id
     * @return vschni uzivatele v danem projektu
     */
    Flux<User> findUsersByProjectId(UUID id);

    /**
     * vrati vsechny uzivatele prirazeny v projektu kde je dany uzivatel manager
     * @param id manager id
     * @return vschni uzivatele v danem projektu
     */
    Flux<User> findUsersByManagerId(UUID id);

    /**
     * vrati vsechny uzivatele
     * @return All users
     */
    Flux<User> findAll();

    /**
     * vrati jen uzivatele kteri jsou v nejakem projektu
     * @return only assigned users
     */
    Flux<User> getOnlyAssignedUsers();

    /**
     * prida uzivatele do projektu
     * @param username uzivatelske jmeno
     * @param project projekt
     * @return User
     */
    Mono<User> joinProject(String username, Project project);

    /**
     * vytvori novy zaznam pro Subordinates
     * @param subordinate new Subordinate
     * @return Subordinate
     */
    Mono<Subordinate> registerSubordinate(Subordinate subordinate);

    /**
     * vrati vsechny podrizene podle daneho nadrizeneho
     * @param id superior id
     * @return all of theirs subordinates
     */
    Flux<User> findSubordinatesBySuperiorId(UUID id);
}
