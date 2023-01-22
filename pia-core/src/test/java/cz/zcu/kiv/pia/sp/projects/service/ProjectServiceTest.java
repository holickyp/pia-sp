package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.error.InvalidDateException;
import cz.zcu.kiv.pia.sp.projects.error.ProjectAlreadyExistException;
import cz.zcu.kiv.pia.sp.projects.repository.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * testy nad ProjectService tridou
 */
@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;
    @InjectMocks
    private ProjectService projectService;

    private static Project project;

    @BeforeAll
    public static void init() {
        project = ProjectService.DEFAULT_PROJECT;
    }

    @Test
    void searchProjects_withoutName() {
        when(projectRepository.findAll()).thenReturn(Flux.just(project));

        var result = projectService.searchProjects("").collectList().block();

        assertEquals(1, result.size());
        assertEquals(project, result.get(0));

        verify(projectRepository).findAll();
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void searchProjects_withName() {
        when(projectRepository.findByName(project.getName())).thenReturn(Flux.just(project));

        var result = projectService.searchProjects(project.getName()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(project, result.get(0));

        verify(projectRepository).findByName(project.getName());
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void findProjectMatchingName() {
        when(projectRepository.findProjectMatchingName(project.getName())).thenReturn(Mono.just(project));

        var result = projectService.getProjectByName(project.getName()).block();

        assertEquals(project, result);

        verify(projectRepository).findProjectMatchingName(project.getName());
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void createProject() {
        when(projectRepository.createProject(argThat(arg -> arg.getName().equals(project.getName())))).thenReturn(Mono.just(project));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = Date.from(project.getFrom());
        String formattedFromDate = formatter.format(fromDate);
        Date toDate = Date.from(project.getTo());
        String formattedToDate = formatter.format(toDate);
        var result = projectService.createProject(project.getName(), project.getManager(), formattedFromDate, formattedToDate, project.getDescription()).block();

        assertEquals(project, result);

        verify(projectRepository).createProject(argThat(arg -> arg.getName().equals(project.getName())));
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void updateProject() {
        when(projectRepository.updateProject(project.getId(), project.getName(), project.getFrom(), project.getTo(), project.getDescription())).thenReturn(Mono.just(project));

        var result = projectService.updateProject(project.getId(), project.getName(), project.getFrom(), project.getTo(), project.getDescription()).block();

        assertEquals(project, result);

        verify(projectRepository).updateProject(project.getId(), project.getName(), project.getFrom(), project.getTo(), project.getDescription());
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void isDateValidTrue() {
        var result = projectService.isDateValid("2022-10-12", "2023-10-12").block();

        assertEquals(true, result);
    }

    @Test
    void isDateValidFalse() {
        Assertions.assertThrows(InvalidDateException.class, () -> projectService.isDateValid("2022-10-12", "2021-10-12"));
    }

    @Test
    void getProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Mono.just(project));

        var result = projectService.getProject(project.getId()).block();

        assertEquals(project, result);

        verify(projectRepository).findById(project.getId());
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void projectExists() {
        Assertions.assertThrows(ProjectAlreadyExistException.class, () -> {
            when(projectRepository.findProjectMatchingName(project.getName())).thenReturn(Mono.just(project));

            projectService.projectExists(project.getName());
        });
    }

    @Test
    void getProjectByUserId() {
        when(projectRepository.findByUserId(project.getManager().getId())).thenReturn(Flux.just(project));

        var result = projectService.getProjectByUserId(project.getManager().getId()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(project, result.get(0));

        verify(projectRepository).findByUserId(project.getManager().getId());
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void joinProject() {
        when(projectRepository.joinProject(project.getId(), UserService.SECOND_USER)).thenReturn(Mono.just(project));

        var result = projectService.joinProject(project.getId(), UserService.SECOND_USER).block();

        assertEquals(project, result);

        verify(projectRepository).joinProject(project.getId(), UserService.SECOND_USER);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void joinUserIntoProjects() {
        when(projectRepository.findByUserId(UserService.DEFAULT_USER.getId())).thenReturn(Flux.just(project));

        var result = projectService.joinUserIntoProjects(UserService.DEFAULT_USER).collectList().block();

        assertEquals(1, UserService.DEFAULT_USER.getProjects().size());
        assertEquals(project, UserService.DEFAULT_USER.getProjects().get(0));
        assertEquals(project, result.get(0));

        verify(projectRepository).findByUserId(UserService.DEFAULT_USER.getId());
        verifyNoMoreInteractions(projectRepository);
    }
}
