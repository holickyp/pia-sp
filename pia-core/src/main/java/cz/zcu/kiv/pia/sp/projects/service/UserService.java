package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.enums.Role;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyExistException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * rozhrani definujici operace nad uzivateli
 */
public interface UserService extends ReactiveUserDetailsService {
    /** vychozi uzivatel */
    User DEFAULT_USER = new User("John", "Doe", "Bruh", "password", Role.SECRETARIAT, "fav", "johndoe@zcu.cz");
    /** druhy vychozi uzivatel */
    User SECOND_USER = new User("Jane", "Doe", "Bruhmoment", "password", Role.DEPARTMENT_MANAGER, "fav", "janedoe@zcu.cz");
    /** vytvoreni hierarchie mezi vychozimi uzivateli */
    Subordinate DEFAULT_SUBORDINATE = new Subordinate(DEFAULT_USER.getId(), SECOND_USER.getId());

    /**
     * Registers user
     * @param firstName firstName
     * @param lastName lastName
     * @param username username
     * @param password password
     * @param role role
     * @param workplace workplace
     * @param email email
     * @return registered user
     * @throws UserAlreadyExistException UserAlreadyExistException
     */
    Mono<User> registerUser(String firstName, String lastName, String username, String password, String role, String workplace, String email) throws UserAlreadyExistException;

    /**
     * aktualizuje uzivatele na zadane hodnoty
     * @param id id uzivatele pro identifikaci
     * @param firstName kresni jmeno
     * @param lastName prijmeni
     * @param username uzivatelske jmeno
     * @param password helso
     * @param role role
     * @param workplace pracoviste
     * @param email email
     * @return aktualizovany uzivatel
     */
    Mono<User> updateUser(UUID id, String firstName, String lastName, String username, String password, String role, String workplace, String email)  throws UserAlreadyExistException;

    /**
     * zkontroluje jestlize dany uzivatel jich existuje
     * @param username uzivatelsko jmeno
     * @return true - UserAlreadyExistsException | false - doesn't exist
     */
    Mono<Boolean> checkIfUserExist(String username);

    /**
     * @return Currently authenticated user
     */
    Mono<User> getCurrentUser();

    /**
     * @return All users
     */
    Flux<User> getAllUsers();

    /**
     * vrati jen uzivatele kteri jsou v nejakem projektu
     * @return only assigned users
     */
    Flux<User> getOnlyAssignedUsers();

    /**
     * vrati uzivatele podle role prave prihlaseneho uzivatele
     * @param user prave prihlaseny uzivatel
     * @return users
     */
    Flux<User> getUsersByCurrentUserRole(User user);

    /**
     * najde uzivatele podle id
     * @param id id uzivatele
     * @return User
     */
    Mono<User> findUserById(UUID id);

    /**
     * Finds user by his unique username
     * @param Username Entered username
     * @return found user
     */
    Mono<UserDetails> findByUsername(String Username);

    /**
     * Finds user by his unique username
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
