package cz.zcu.kiv.pia.sp.projects.repository;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * rozhrani, assignments v pameti a oprace nad nima
 */
public interface AssignmentRepository {

    /**
     * vytvor novy assignment
     * @param assignment assignment
     * @return assignment
     */
    Mono<Assignment> createAssignment(Assignment assignment);

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
    Mono<Assignment> updateAssignment(UUID id, double scope, Instant from, Instant to, String note, Status status);

    /**
     * projede vsechny assignments a aktualizuje jejich stav na Past pokud cas do uz probehl
     * @return void
     */
    Mono<Void> updateAssignmentsStatuses();

    /**
     * najde assignment podle od
     * @param id assignment id
     * @return assignment
     */
    Mono<Assignment> findById(UUID id);

    /**
     * najde assignment podle worker_id
     * @param id id uzivatele
     * @return assignment
     */
    Flux<Assignment> findByUserId(UUID id);

    /**
     * najde assignment podle job_id
     * @param id id projektu
     * @return assignment
     */
    Flux<Assignment> findByProjectId(UUID id);

    /**
     * vrati vsechny assignments
     * @return assignments
     */
    Flux<Assignment> findAll();
}
