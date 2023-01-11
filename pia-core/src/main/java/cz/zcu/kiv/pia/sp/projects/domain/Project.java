package cz.zcu.kiv.pia.sp.projects.domain;

import java.time.Instant;
import java.util.*;

/**
 * Project class
 */
public class Project {
    /** id projektu */
    private final UUID id;
    /** nazev projektu */
    private String name;
    /** manazer projektu */
    private final User manager;
    /** cas od */
    private Instant from;
    /** cas do */
    private Instant to;
    /** popis projektu */
    private String description;
    /** uzivatele zarazeni do projektu */
    private final List<User> users;

    /**
     * constructor, vytvori projekt s nahodnym UUID
     * @param name nazev projektu
     * @param manager anazer projektu
     * @param from cas od
     * @param to cas do
     * @param description popis projektu
     */
    public Project (String name, User manager, Instant from, Instant to, String description){
        this(UUID.randomUUID(), name, manager, from, to, description);
    }

    /**
     * constructor
     * @param id id projektu
     * @param name nazev projektu
     * @param manager anazer projektu
     * @param from cas od
     * @param to cas do
     * @param description popis projektu
     */
    public Project (UUID id, String name, User manager, Instant from, Instant to, String description){
        this.id = id;
        this.name = name;
        this.manager = manager;
        this.from = from;
        this.to = to;
        this.description = description;
        this.users = new ArrayList<>();
    }

    private Project (Builder builder) {
        id = builder.id;
        name = builder.name;
        manager = builder.manager;
        from = builder.from;
        to = builder.to;
        description = builder.description;
        users = builder.users;
    }

    /**
     * aktualizuje hodnoty projektu na zadane hodnoty
     * @param name nazev projektu
     * @param from cas od
     * @param to cas do
     * @param description popis projektu
     */
    public void update(String name, Instant from, Instant to, String description) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.description = description;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * prida uzivatele do projektu
     * @param user uzivatel
     */
    public void join(User user) {
        users.add(user);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getManager() {
        return manager;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public String getDescription() {
        return description;
    }

    public List<User> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project project)) return false;
        return Objects.equals(id, project.id) && Objects.equals(name, project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", manager=" + manager +
                ", from=" + from +
                ", to=" + to +
                ", description='" + description + '\'' +
                ", users=" + users +
                '}';
    }

    public static class Builder {

        private UUID id;
        private String name;
        private User manager;
        private Instant from;
        private Instant to;
        private String description;

        private List<User> users = Collections.emptyList();

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder manager(User manager) {
            this.manager = manager;
            return this;
        }

        public Builder from(Instant from) {
            this.from = from;
            return this;
        }

        public Builder to(Instant to) {
            this.to = to;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder users(List<User> users) {
            this.users = users;
            return this;
        }

        public Project build() {
            return new Project(this);
        }
    }
}
