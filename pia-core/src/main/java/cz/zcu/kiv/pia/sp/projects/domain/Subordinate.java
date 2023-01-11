package cz.zcu.kiv.pia.sp.projects.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Subordinate class
 * urcuje hierarchii mezi uzivateli
 */
public class Subordinate {
    /** id zaznamu */
    private final UUID id;
    /** id nadrizeneho uzivatele */
    private final UUID superior_id;
    /** id podrizeneho uzivatele */
    private final UUID subordinate_id;

    /**
     * constructor, vytvori zaznam s nahodnym UUID
     * @param superior_id id nadrizeneho uzivatele
     * @param subordinate_id
     */
    public Subordinate(UUID superior_id, UUID subordinate_id) {
        this(UUID.randomUUID(), superior_id, subordinate_id);
    }

    /**
     * constructor
     * @param id id zaznamu
     * @param superior_id id nadrizeneho uzivatele
     * @param subordinate_id id podrizeneho uzivatele
     */
    public Subordinate(UUID id, UUID superior_id, UUID subordinate_id) {
        this.id = id;
        this.superior_id = superior_id;
        this.subordinate_id = subordinate_id;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSuperior_id() {
        return superior_id;
    }

    public UUID getSubordinate_id() {
        return subordinate_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subordinate subordinate)) return false;
        return Objects.equals(id, subordinate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subordinate_id, superior_id);
    }

    @Override
    public String toString() {
        return "Subordinate{" +
                "id=" + id +
                ", superior_id=" + superior_id +
                ", subordinate_id=" + subordinate_id +
                '}';
    }
}
