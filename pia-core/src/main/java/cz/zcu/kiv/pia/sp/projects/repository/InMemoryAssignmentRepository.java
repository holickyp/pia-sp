package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import cz.zcu.kiv.pia.sp.projects.service.AssignmentService;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * implementace AssignmentRepository, pametova varianta
 */
@Repository
public class InMemoryAssignmentRepository implements AssignmentRepository {
    /** mapa uchovavajici Assignments */
    private final Map<UUID, Assignment> assignmentMap;

    /**
     * constructor
     */
    public InMemoryAssignmentRepository() {
        this.assignmentMap = new HashMap<>();
    }

    @PostConstruct
    private void postConstruct() {
        assignmentMap.put(AssignmentService.DEFAULT_ASSIGNMENT.getId(), AssignmentService.DEFAULT_ASSIGNMENT);
    }

    /**
     * vytvor novy assignment
     * @param assignment assignment
     * @return assignment
     */
    @Override
    public Mono<Assignment> createAssignment(Assignment assignment) {
        assignmentMap.put(assignment.getId(), assignment);

        return findById(assignment.getId());
    }

    /**
     * aktualizuje assignment na zadane hodnoty
     * @param id id pro identifikaci
     * @param scope uvazek
     * @param from cas od
     * @param to cas do
     * @param note poznamka
     * @param status stav
     * @return aktualizovany Assignment
     */
    @Override
    public Mono<Assignment> updateAssignment(UUID id, double scope, Instant from, Instant to, String note, Status status) {
        var updated_assignment = assignmentMap.get(id);
        updated_assignment.update(scope, from, to, note, status);
        return Mono.just(updated_assignment);
    }

    /**
     * projede vsechny assignments a aktualizuje jejich stav na Past pokud cas do uz probehl
     * @return void
     */
    @Override
    public Mono<Void> updateAssignmentsStatuses() {
        var assignments = findAll();

        LocalDate localDate = LocalDate.now();
        for(Assignment assignment : assignments.toIterable()) {
            LocalDate time_to = assignment.getTo().atZone(ZoneId.systemDefault()).toLocalDate();
            if(localDate.isAfter(time_to)) {
                assignment.update(assignment.getScope(), assignment.getFrom(), assignment.getTo(), assignment.getNote(), Status.PAST);
            }
        }
        return Mono.empty();
    }

    /**
     * najde assignment podle od
     * @param id assignment id
     * @return assignment
     */
    @Override
    public Mono<Assignment> findById(UUID id) {
        var assignment = assignmentMap.get(id);

        return Mono.just(assignment);
    }

    /**
     * najde assignment podle worker_id
     * @param id id uzivatele
     * @return assignment
     */
    @Override
    public Flux<Assignment> findByUserId(UUID id) {
        var assignments = assignmentMap.values()
                .stream()
                .filter(assignment -> assignment.getWorker_id().equals(id));

        return Flux.fromStream(assignments);
    }

    /**
     * najde assignment podle job_id
     * @param id id projektu
     * @return assignment
     */
    @Override
    public Flux<Assignment> findByProjectId(UUID id) {
        var assignments = assignmentMap.values()
                .stream()
                .filter(assignment -> assignment.getJob_id().equals(id));

        return Flux.fromStream(assignments);
    }

    /**
     * vrati vsechny assignments
     * @return assignments
     */
    @Override
    public Flux<Assignment> findAll() {
        var assignments = assignmentMap.values();

        return Flux.fromIterable(assignments);
    }
}
