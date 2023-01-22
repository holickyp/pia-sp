package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyAssignedException;
import cz.zcu.kiv.pia.sp.projects.error.WorkloadExceededException;
import cz.zcu.kiv.pia.sp.projects.repository.AssignmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * testy nad AssignmentService tridou
 */
@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {
    @Mock
    private AssignmentRepository assignmentRepository;
    @InjectMocks
    private AssignmentService assignmentService;

    private static Assignment assignment;

    @BeforeAll
    public static void init(){
        assignment = AssignmentService.DEFAULT_ASSIGNMENT;
    }

    @Test
    void createAssignment() {
        when(assignmentRepository.createAssignment(assignment)).thenReturn(Mono.just(assignment));

        var result = assignmentService.createAssignment(assignment).block();

        assertEquals(assignment, result);

        verify(assignmentRepository).createAssignment(assignment);
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void updateAssignment() {
        when(assignmentRepository.updateAssignment(assignment.getId(), assignment.getScope(), assignment.getFrom(), assignment.getTo(), assignment.getNote(), assignment.getStatus())).thenReturn(Mono.just(assignment));

        var result = assignmentService.updateAssignment(assignment.getId(), assignment.getScope(), assignment.getFrom(), assignment.getTo(), assignment.getNote(), assignment.getStatus().toString()).block();

        assertEquals(assignment, result);

        verify(assignmentRepository).updateAssignment(assignment.getId(), assignment.getScope(), assignment.getFrom(), assignment.getTo(), assignment.getNote(), assignment.getStatus());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void isWorkloadExceededFalse() {
        when(assignmentRepository.findByUserId(assignment.getWorker_id())).thenReturn(Flux.just(assignment));

        var result = assignmentService.isWorkloadExceeded(assignment.getWorker_id(), assignment.getJob_id(), 20).block();

        assertEquals(false, result);

        verify(assignmentRepository).findByUserId(assignment.getWorker_id());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void isWorkloadExceededTrue() {
        Assertions.assertThrows(WorkloadExceededException.class, () -> {
            when(assignmentRepository.findByUserId(assignment.getWorker_id())).thenReturn(Flux.just(assignment));

            assignmentService.isWorkloadExceeded(assignment.getWorker_id(), assignment.getJob_id(), 40);
        });
    }

    @Test
    void isUserAssigned() {
        Assertions.assertThrows(UserAlreadyAssignedException.class, () -> {
            when(assignmentRepository.findByUserId(assignment.getWorker_id())).thenReturn(Flux.just(assignment));

            assignmentService.isUserAssigned(assignment.getWorker_id(), assignment.getJob_id()).block();
        });
    }

    @Test
    void updateAssignmentStatuses() {
        assignmentService.updateAssignmentsStatuses();

        LocalDate localDate = LocalDate.now();
        LocalDate time_to = assignment.getTo().atZone(ZoneId.systemDefault()).toLocalDate();
        if(localDate.isAfter(time_to)) {
            assertEquals(Status.PAST, assignment.getStatus());
        } else {
            assertEquals(Status.ACTIVE, assignment.getStatus());
        }
    }

    @Test
    void findAssignmentById() {
        when(assignmentRepository.findById(assignment.getId())).thenReturn(Mono.just(assignment));

        var result = assignmentService.findById(assignment.getId()).block();

        assertEquals(assignment, result);

        verify(assignmentRepository).findById(assignment.getId());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void findAssignmentByUserId() {
        when(assignmentRepository.findByUserId(assignment.getWorker_id())).thenReturn(Flux.just(assignment));

        var result = assignmentService.findByUserId(assignment.getWorker_id()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(assignment, result.get(0));

        verify(assignmentRepository).findByUserId(assignment.getWorker_id());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void findAssignmentByProjectId() {
        when(assignmentRepository.findByProjectId(assignment.getJob_id())).thenReturn(Flux.just(assignment));

        var result = assignmentService.findByProjectId(assignment.getJob_id()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(assignment, result.get(0));

        verify(assignmentRepository).findByProjectId(assignment.getJob_id());
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void findAllAssignments() {
        when(assignmentRepository.findAll()).thenReturn(Flux.just(assignment));

        var result = assignmentService.getAllAssignments().collectList().block();

        assertEquals(1, result.size());
        assertEquals(assignment, result.get(0));

        verify(assignmentRepository).findAll();
        verifyNoMoreInteractions(assignmentRepository);
    }

    @Test
    void calculateActiveWorkload() {
        var result = assignmentService.calculateActiveWorkload(Flux.just(assignment)).block();

        assertEquals(1, result);
    }

    @Test
    void calculateOverallWorkload() {
        var result = assignmentService.calculateOverallWorkload(Flux.just(assignment)).block();

        assertEquals(1, result);
    }
}
