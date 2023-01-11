package cz.zcu.kiv.pia.sp.projects.ui.vo;

/**
 * Value object used in the form for registering a new user
 *
 */
public class UserVO {
    private final String firstname;
    private final String lastname;
    private final String username;
    private final String password;
    private final String role;
    private final String workplace;
    private final String email;

    public UserVO(String firstname, String lastname, String username, String password, String role, String workplace, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.workplace = workplace;
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUsername() {
        return username;
    }

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


}
