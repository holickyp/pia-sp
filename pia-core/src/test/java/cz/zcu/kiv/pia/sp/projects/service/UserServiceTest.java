package cz.zcu.kiv.pia.sp.projects.service;

import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyExistException;
import cz.zcu.kiv.pia.sp.projects.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

/**
 * testy nad UserService tridou
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private DefaultUserService userService;
    @InjectMocks
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static User user;

    @BeforeAll
    public static void init(){
        user = UserService.DEFAULT_USER;
    }

    @Test
    void registerUser() throws UserAlreadyExistException {
        DefaultUserService underTest = new DefaultUserService(userRepository, passwordEncoder) {
            @Override
            public Mono<Boolean> checkIfUserExist(String username) {
                return Mono.just(false);
            }
        };
        when(userRepository.registerUser(argThat(arg -> arg.getUsername().equals(user.getUsername())))).thenReturn(Mono.just(user));

        var result = underTest.registerUser(user.getFirstname(), user.getLastname(), user.getUsername(),user.getPassword(), user.getRole().toString(), user.getWorkplace(), user.getEmail()).block();

        assertEquals(user, result);

        verify(userRepository).registerUser(argThat(arg -> arg.getUsername().equals(user.getUsername())));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void registerUserAlreadyExists() throws UserAlreadyExistException {
        Assertions.assertThrows(UserAlreadyExistException.class, () -> {
            DefaultUserService underTest = new DefaultUserService(userRepository, passwordEncoder) {
                @Override
                public Mono<Boolean> checkIfUserExist(String username) {
                    return Mono.just(true);
                }
            };

            underTest.registerUser(user.getFirstname(), user.getLastname(), user.getUsername(), user.getPassword(), user.getRole().toString(), user.getWorkplace(), user.getEmail()).block();
        });
    }

    @Test
    void updateUser() {
        DefaultUserService underTest = new DefaultUserService(userRepository, passwordEncoder) {
            @Override
            public Mono<Boolean> checkIfUserExist(String username) {
                return Mono.just(true);
            }
        };

        when(userRepository.updateUser(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(user));

        var result = underTest.updateUser(user.getId(), user.getFirstname(), user.getLastname(), user.getUsername(), user.getPassword(), user.getRole().toString(), user.getWorkplace(), user.getEmail()).block();

        assertEquals(user, result);

        verify(userRepository).updateUser(any(), any(), any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserDoesNotExists() throws UserAlreadyExistException {
        DefaultUserService underTest = new DefaultUserService(userRepository, passwordEncoder) {
            @Override
            public Mono<Boolean> checkIfUserExist(String username) {
                return Mono.just(false);
            }
        };

        var result = underTest.updateUser(user.getId(), user.getFirstname(), user.getLastname(), user.getUsername(), user.getPassword(), user.getRole().toString(), user.getWorkplace(), user.getEmail());

        assertEquals(Mono.empty(), result);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void checkIfUserExist() {
        Assertions.assertThrows(UserAlreadyExistException.class, () -> {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Mono.just(user));

            userService.checkIfUserExist(user.getUsername());
        });
    }

    @Test
    void getCurrentUser() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken(user, null);

        TestSecurityContextHolder.setAuthentication(authentication);
        TestExecutionListener reactorContextTestExecutionListener = new ReactorContextTestExecutionListener();
        reactorContextTestExecutionListener.beforeTestMethod(null);

        var result = userService.getCurrentUser();

        assertEquals(user, result.block());

        reactorContextTestExecutionListener.afterTestMethod(null);
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(Flux.just(user));

        var result = userService.getAllUsers().collectList().block();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));

        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getOnlyAssignedUsers() {
        when(userRepository.getOnlyAssignedUsers()).thenReturn(Flux.just(user));

        var result = userService.getOnlyAssignedUsers().collectList().block();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));

        verify(userRepository).getOnlyAssignedUsers();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUsersByCurrentUserRoleSecretariat() {
        when(userRepository.findAll()).thenReturn(Flux.just(user));

        var result = userService.getUsersByCurrentUserRole(user).collectList().block();

        assertFalse(result.isEmpty());

        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUserById() {
        when(userRepository.findById(user.getId())).thenReturn(Mono.just(user));

        var result = userService.findUserById(user.getId()).block();

        assertEquals(user, result);

        verify(userRepository).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findByUsername() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Mono.just(user));

        var result = userService.findByUsername(user.getUsername()).block();

        assertEquals(user, result);

        verify(userRepository).findByUsername(user.getUsername());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUserByUsername() {
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Mono.just(user));

        var result = userService.findUserByUsername(user.getUsername()).block();

        assertEquals(user, result);

        verify(userRepository).findUserByUsername(user.getUsername());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUsersByProjectId() {
        when(userRepository.findUsersByProjectId(ProjectService.DEFAULT_PROJECT.getId())).thenReturn(Flux.just(user));

        var result = userService.findUsersByProjectId(ProjectService.DEFAULT_PROJECT.getId()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));

        verify(userRepository).findUsersByProjectId(ProjectService.DEFAULT_PROJECT.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUsersByManagerId() {
        when(userRepository.findUsersByManagerId(ProjectService.DEFAULT_PROJECT.getManager().getId())).thenReturn(Flux.just(user));

        var result = userService.findUsersByManagerId(ProjectService.DEFAULT_PROJECT.getManager().getId()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));

        verify(userRepository).findUsersByManagerId(ProjectService.DEFAULT_PROJECT.getManager().getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void joinProject() {
        when(userRepository.joinProject(user.getUsername(), ProjectService.DEFAULT_PROJECT)).thenReturn(Mono.just(user));

        var result = userService.joinProject(user.getUsername(), ProjectService.DEFAULT_PROJECT).block();

        assertEquals(user, result);

        verify(userRepository).joinProject(user.getUsername(), ProjectService.DEFAULT_PROJECT);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void registerSubordinate() {
        Subordinate subordinate = new Subordinate(user.getId(), UserService.SECOND_USER.getId());
        when(userRepository.registerSubordinate(subordinate)).thenReturn(Mono.just(subordinate));

        var result = userService.registerSubordinate(subordinate).block();

        assertEquals(subordinate, result);

        verify(userRepository).registerSubordinate(subordinate);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findSubordinatesBySuperiorId() {
        when(userRepository.findSubordinatesBySuperiorId(user.getId())).thenReturn(Flux.just(user));

        var result = userService.findSubordinatesBySuperiorId(user.getId()).collectList().block();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));

        verify(userRepository).findSubordinatesBySuperiorId(user.getId());
        verifyNoMoreInteractions(userRepository);
    }
}
