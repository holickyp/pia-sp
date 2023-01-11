package cz.zcu.kiv.pia.sp.projects.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

/**
 * User class
 */
public class User implements UserDetails {
    /** id uzivatele */
    private final UUID id;
    /** krestni jmeno */
    private String firstname;
    /** prijmeni */
    private String lastname;
    /** uzivatelske jmeno */
    private String username;
    /** heslo */
    private String password;
    /** role uzivatele */
    private String role;
    /** misto pracoviste */
    private String workplace;
    /** email */
    private String email;
    /** seznam v jakych projektech je uzivatel prirazen */
    private final List<Project> projects;

    /**
     * constructor, vytvori uzivatele s nahodnym UUID
     * @param firstname krestni jmeno
     * @param lastname prijmeni
     * @param username uzivatelske jmeno
     * @param password heslo
     * @param role role uzivatele
     * @param workplace misto pracoviste
     * @param email email
     */
    public User(String firstname, String lastname, String username, String password, String role, String workplace, String email) {
        this(UUID.randomUUID(), firstname, lastname, username, password, role, workplace, email);
    }

    /**
     * constructor
     * @param id id uzivatele
     * @param firstname krestni jmeno
     * @param lastname prijmeni
     * @param username uzivatelske jmeno
     * @param password heslo
     * @param role role uzivatele
     * @param workplace misto pracoviste
     * @param email email
     */
    public User(UUID id, String firstname, String lastname, String username, String password, String role, String workplace, String email) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.workplace = workplace;
        this.email = email;
        this.projects = new ArrayList<>();
    }

    /**
     * aktualizuje hodnoty uzivatele na zadane hodnoty
     * @param firstname krestni jmeno
     * @param lastname prijmeni
     * @param username uzivatelske jmeno
     * @param password heslo
     * @param role role uzivatele
     * @param workplace misto pracoviste
     * @param email email
     */
    public void update(String firstname, String lastname, String username, String password, String role, String workplace, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.workplace = workplace;
        this.email = email;
    }

    /**
     * prida uzivatele do projektu
     * @param project projekt
     */
    public void join(Project project) {
        projects.add(project);
    }

    public UUID getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getWorkplace() {
        return workplace;
    }

    public String getEmail() {
        return email;
    }

    public List<Project> getProjects() {
        return projects;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(role));
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstname + '\'' +
                ", lastName='" + lastname + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", workplace='" + workplace + '\'' +
                ", email='" + email + '\'' +
                ", projects=" + projects +
                '}';
    }
}
