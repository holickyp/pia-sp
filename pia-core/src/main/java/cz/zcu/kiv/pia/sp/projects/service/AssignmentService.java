package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyAssignedException;
import cz.zcu.kiv.pia.sp.projects.error.WorkloadExceededException;
import cz.zcu.kiv.pia.sp.projects.repository.AssignmentRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            Status.ACTIVE);

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
        return assignmentRepository.updateAssignment(id, scope, from, to, note, Status.getStatusByString(status));
    }

    /**
     * tests if extra workload would exceed user's allowed workload maximum (40h)
     * @param userId user id
     * @param assignmentId assignment id
     * @param AddWorkload extra workload
     * @return false -> not exceeded | true -> WorkloadExceededException is exceeded
     */
    public Mono<Boolean> isWorkloadExceeded(UUID userId, UUID assignmentId, double AddWorkload) {
        var assignments = assignmentRepository.findByUserId(userId);
        //spocte uvazek
        double workload = 0;
        for(Assignment assignment : assignments.toIterable()) {
            if(assignment.getId().equals(assignmentId)) {
                continue;
            }
            workload += assignment.getScope();
        }
        workload += AddWorkload;
        //test zda uvazek neprekroci maximum
        if(workload > 40) {
            throw new WorkloadExceededException("workload exceeds 40 hours");
        }
        return Mono.just(false);
    }

    /**
     * tests if user is already assigned to target project
     * @param userId user's id
     * @param projectId target project's id
     * @return false -> not assigned | true -> UserAlreadyAssignedException already assigned
     */
    public Mono<Boolean> isUserAssigned(UUID userId, UUID projectId){
        var assignments = assignmentRepository.findByUserId(userId);
        for(Assignment assignment : assignments.toIterable()) {
            if(assignment.getJob_id().equals(projectId)) {
                throw new UserAlreadyAssignedException("user already assigned");
            }
        }
        return Mono.just(false);
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

    /**
     * calculates active workload from given assignments
     * @param assignments assignments
     * @return active workload
     */
    public Mono<Double> calculateActiveWorkload(Flux<Assignment> assignments) {
        double activeWorkload = 0;
        for(Assignment assignment : assignments.toIterable()) {
            if(assignment.getStatus().equals(Status.ACTIVE)) {
                activeWorkload += assignment.getScope();
            }
        }
        return Mono.just(activeWorkload);
    }

    /**
     * calculates overall workload from given assignments
     * @param assignments assignments
     * @return overall workload
     */
    public Mono<Double> calculateOverallWorkload(Flux<Assignment> assignments) {
        double overallWorkload = 0;
        double activeWorkload = 0;
        for(Assignment assignment : assignments.toIterable()) {
            if(assignment.getStatus().equals(Status.ACTIVE)) {
                activeWorkload += assignment.getScope();
            } else {
                if(!assignment.getStatus().equals(Status.PAST)) {
                    overallWorkload += assignment.getScope();
                }
            }
        }
        overallWorkload += activeWorkload;
        return Mono.just(overallWorkload);
    }
}
