package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        when(projectRepository.createProject(project)).thenReturn(Mono.just(project));

        var result = projectService.createProject(project).block();

        assertEquals(project, result);

        verify(projectRepository).createProject(project);
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
    void getProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Mono.just(project));

        var result = projectService.getProject(project.getId()).block();

        assertEquals(project, result);

        verify(projectRepository).findById(project.getId());
        verifyNoMoreInteractions(projectRepository);
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
}
