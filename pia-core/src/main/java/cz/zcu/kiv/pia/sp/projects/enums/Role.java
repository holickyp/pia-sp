package cz.zcu.kiv.pia.sp.projects.enums;

public enum Role {
    SECRETARIAT("SECRETARIAT"),
    DEPARTMENT_MANAGER("DEPARTMENT-MANAGER"),
    PROJECT_MANAGER("PROJECT-MANAGER"),
    SUPERIOR("SUPERIOR"),
    REGULAR_USER("REGULAR-USER");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public static Role getRoleByString(String role) {
        for(Role r : Role.values()) {
            if(r.toString().equals(role)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return role;
    }

    public String getRole() {
        return role;
    }
}
