package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyExistException;
import cz.zcu.kiv.pia.sp.projects.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * implementace UserService rozhrani
 */
@Service
@Transactional(readOnly = true)
public class DefaultUserService implements UserService {
    private final UserRepository userRepository;

    public DefaultUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers user
     *
     * @param user User to register
     * @return Registered user
     */
    @Override
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<User> registerUser(User user)  throws UserAlreadyExistException {
        if(checkIfUserExist(user.getUsername())){
            throw new UserAlreadyExistException("User already exists");
        }

        return userRepository.registerUser(user);
    }

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
    @Override
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<User> updateUser(UUID id, String firstName, String lastName, String username, String password, String role, String workplace, String email)  throws UserAlreadyExistException {
        if(checkIfUserExist(username)){
            return userRepository.updateUser(id, firstName, lastName, username, password, role, workplace, email);
        }

        return Mono.empty();
    }

    /**
     * Finds user by his unique username
     * @param username User's unique username
     * @return Found user
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds user by his unique username
     * @param username User's unique username
     * @return Found user
     */
    @Override
    public Mono<User> findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    /**
     * vrati vsechny uzivatele prirazeny v danem projektu
     * @param id project id
     * @return vschni uzivatele v danem projektu
     */
    @Override
    public Flux<User> findUsersByProjectId(UUID id) {
        return userRepository.findUsersByProjectId(id);
    }

    /**
     * vrati vsechny uzivatele prirazeny v projektu kde je dany uzivatel manager
     * @param id manager id
     * @return vschni uzivatele v danem projektu
     */
    @Override
    public Flux<User> findUsersByManagerId(UUID id) {
        return userRepository.findUsersByManagerId(id);
    }


    /**
     * zkontroluje jestlize dany uzivatel jich existuje
     * @param username uzivatelsko jmeno
     * @return true - exists | false - doesn't exist
     */
    public boolean checkIfUserExist(String username) {
        Mono<UserDetails> userDetailsMono = userRepository.findByUsername(username);
        return Boolean.TRUE.equals(userDetailsMono.hasElement().block());

    }

    /**
     * @return Currently authenticated user
     */
    @Override
    public Mono<User> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (User) securityContext.getAuthentication().getPrincipal());
    }

    /**
     * @return All users
     */
    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * vrati jen uzivatele kteri jsou v nejakem projektu
     * @return only assigned users
     */
    @Override
    public Flux<User> getOnlyAssignedUsers() {
        return userRepository.getOnlyAssignedUsers();
    }

    /**
     * najde uzivatele podle id
     * @param id id uzivatele
     * @return User
     */
    @Override
    public Mono<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * prida uzivatele do projektu
     * @param username uzivatelske jmeno
     * @param project projekt
     * @return User
     */
    @Override
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<User> joinProject(String username, Project project) {
        return userRepository.joinProject(username, project);
    }

    /**
     * vytvori novy zaznam pro Subordinates
     * @param subordinate new Subordinate
     * @return Subordinate
     */
    @Override
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<Subordinate> registerSubordinate(Subordinate subordinate) {
        return Mono.just(subordinate)
                .flatMap(userRepository::registerSubordinate);
    }

    /**
     * vrati vsechny podrizene podle daneho nadrizeneho
     * @param id superior id
     * @return all of theirs subordinates
     */
    @Override
    public Flux<User> findSubordinatesBySuperiorId(UUID id) {
        return userRepository.findSubordinatesBySuperiorId(id);
    }
}
