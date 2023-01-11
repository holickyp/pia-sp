package cz.zcu.kiv.pia.sp.projects.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.repository.AssignmentRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Definice operaci nad assignments
 */
@Service
@EnableScheduling
@Transactional(readOnly = true)
public class AssignmentService {
    /** vychozi assignment */
    public static final Assignment DEFAULT_ASSIGNMENT = new Assignment(
            ProjectService.DEFAULT_PROJECT.getId(),
            UserService.DEFAULT_USER.getId(),
            1,
            Instant.now(),
            Instant.now(),
            "default assignment",
            "Active");

    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * vytvor novy assignment
     * @param assignment assignment
     * @return assignment
     */
    @Transactional
    @Secured("ROLE_SECRETARIAT")
    public Mono<Assignment> createAssignment(Assignment assignment) {
        return Mono.just(assignment)
                .flatMap(assignmentRepository::createAssignment);
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
    @Transactional
    @Secured({"ROLE_DEPARTMENT-MANAGER", "ROLE_PROJECT-MANAGER"})
    public Mono<Assignment> updateAssignment(UUID id, double scope, Instant from, Instant to, String note, String status) {
        return assignmentRepository.updateAssignment(id, scope, from, to, note, status);
    }

    /**
     * projede vsechny assignments a aktualizuje jejich stav na Past pokud cas do uz probehl
     * pousti se automaticky kazdy den o pulnoci
     * @return void
     */
    @Transactional
    @Scheduled(cron = "@daily")
    public Mono<Void> updateAssignmentsStatuses() {
        return assignmentRepository.updateAssignmentsStatuses();
    }

    /**
     * najde assignment podle od
     * @param id assignment id
     * @return assignment
     */
    public Mono<Assignment> findById(UUID id) {
        return assignmentRepository.findById(id);
    }

    /**
     * najde assignment podle worker_id
     * @param id id uzivatele
     * @return assignment
     */
    public Flux<Assignment> findByUserId(UUID id) {
        return assignmentRepository.findByUserId(id);
    }

    /**
     * najde assignment podle job_id
     * @param id id projektu
     * @return assignment
     */
    public Flux<Assignment> findByProjectId(UUID id) {
        return assignmentRepository.findByProjectId(id);
    }

    /**
     * vrati vsechny assignments
     * @return assignments
     */
    public Flux<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }
}
