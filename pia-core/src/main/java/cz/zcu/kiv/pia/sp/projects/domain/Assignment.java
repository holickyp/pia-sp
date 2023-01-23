package cz.zcu.kiv.pia.sp.projects.domain;

import cz.zcu.kiv.pia.sp.projects.enums.Status;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Assignment class
 * udavá jaký uživatel je přiřazen ke kterému projeku
 */
public class Assignment {
    /** assignment id */
    private final UUID id;
    /** id uzivatele */
    private final UUID worker_id;
    /** id projektu */
    private final UUID job_id;
    /** uvazek 0-40 hodin */
    private double scope;
    /** cas platnosti prirazeni od */
    private Instant from;
    /** cas platnosti prirazeni do */
    private Instant to;
    /** poznamka */
    private String note;
    /** stav */
    private Status status;

    /**
     * constructor, vytvori assignment s nahodnym UUID
     * @param worker_id id uzivatele
     * @param job_id id projektu
     * @param scope uvazek 0-40 hodin
     * @param from cas od
     * @param to cas do
     * @param note poznamka
     * @param status stav
     */
    public Assignment(UUID worker_id, UUID job_id, double scope, Instant from, Instant to, String note, Status status) {
        this(UUID.randomUUID(), worker_id, job_id, scope, from, to, note, status);
    }

    /**
     * constructor
     * @param id assignment id
     * @param worker_id id uzivatele
     * @param job_id id projektu
     * @param scope uvazek 0-40 hodin
     * @param from cas od
     * @param to cas do
     * @param note poznamka
     * @param status stav
     */
    public Assignment(UUID id, UUID worker_id, UUID job_id, double scope, Instant from, Instant to, String note, Status status) {
        this.id = id;
        this.worker_id = worker_id;
        this.job_id = job_id;
        this.scope = scope;
        this.from = from;
        this.to = to;
        this.note = note;
        this.status = status;
    }

    /**
     * aktualizuje hodnoty assigmentu na zadane hodnoty
     * @param scope uvazek 0-40 hodin
     * @param from cas od
     * @param to cas do
     * @param note poznamka
     * @param status stav
     */
    public void update(double scope, Instant from, Instant to, String note, Status status) {
        this.scope = scope;
        this.from = from;
        this.to = to;
        this.note = note;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorker_id() {
        return worker_id;
    }

    public UUID getJob_id() {
        return job_id;
    }

    public double getScope() {
        return scope;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public String getNote() {
        return note;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment assignment)) return false;
        return Objects.equals(worker_id, assignment.worker_id) && Objects.equals(job_id, assignment.job_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worker_id, job_id);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", worker_id=" + worker_id +
                ", job_id=" + job_id +
                ", scope=" + scope +
                ", from=" + from +
                ", to=" + to +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
