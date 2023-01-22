package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.enums.Role;
import cz.zcu.kiv.pia.sp.projects.error.UserNotFoundException;
import cz.zcu.kiv.pia.sp.projects.service.AssignmentService;
import cz.zcu.kiv.pia.sp.projects.service.ProjectService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import cz.zcu.kiv.pia.sp.projects.ui.vo.UserVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * Controller pro spravu uzivatelu
 */
@Controller
public class UserManagementController extends AbstractController {
    private final ProjectService projectService;
    private final AssignmentService assignmentService;

    public UserManagementController(UserService userService, ProjectService projectService, AssignmentService assignmentService) {
        super(userService);
        this.projectService = projectService;
        this.assignmentService = assignmentService;
    }

    /**
     * stranka pro spravu uzivatelu
     * jineho hodnoty podle role prohlaseneho uzivatele
     * @param principal principal
     * @param model model
     * @return template name
     */
    @GetMapping("/user/management")
    public String userManagement(@AuthenticationPrincipal Principal principal, Model model) {
        User curr_user = userService.findUserByUsername(principal.getName()).block();

        var users = userService.getUsersByCurrentUserRole(curr_user);
        model.addAttribute("users", users);

        //get projects of displayed users
        for(User user : users.toIterable()) {
            projectService.joinUserIntoProjects(user);
            var assignments = assignmentService.findByUserId(user.getId());
            model.addAttribute("assignment"+user.getUsername(), assignments);

            //spocteni vytizeni pro managery
            if(curr_user.getRole().equals(Role.PROJECT_MANAGER) || curr_user.getRole().equals(Role.DEPARTMENT_MANAGER)) {
                model.addAttribute("activeAssignmentWorkload"+user.getUsername(), assignmentService.calculateActiveWorkload(assignments).block());
                model.addAttribute("overallAssignmentWorkload"+user.getUsername(), assignmentService.calculateOverallWorkload(assignments).block());
            }
        }
        model.addAttribute("userVO", new UserVO("", "","","","","",""));

        return "userManagement";
    }

    /**
     * POST prirad podrizeneho
     * @param userVO data z formulare
     * @param errors errors
     * @param model model
     * @return template name
     */
    @PostMapping("/user/management/assign")
    public Mono<String> assignSubordinate(@ModelAttribute UserVO userVO, BindingResult errors, Model model) {
        //identifikuje uzivatele ke kteremu bude subordinate pridany (jeho username ulozeny v userVO.username)
        User superior;
        try {
            superior = userService.findUserByUsername(userVO.getUsername()).block();
        } catch(UserNotFoundException e) {
            //superior nebyl nalezen
            errors.rejectValue("username", "user.username","Superior not found.");
            model.addAttribute("registrationForm", userVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(userManagement((Principal) userService.getCurrentUser(), model));
        }

        //identifikuje uzivatele ktereho chceme priradit jako subordinate (jeho username ulozeny v userVO.lastname)
        User subordinate;
        try {
            subordinate = userService.findUserByUsername(userVO.getLastname()).block();
        } catch(UserNotFoundException e) {
            //subordinate nebyl nalezen
            errors.rejectValue("username", "user.username","Subordinate not found.");
            model.addAttribute("registrationForm", userVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(userManagement((Principal) userService.getCurrentUser(), model));
        }

        //vytvori noveho subordinate
        return userService.getCurrentUser()
                .flatMap(user -> userService.registerSubordinate(new Subordinate(superior.getId(), subordinate.getId())))
                .map(project -> "redirect:/user/management"); // Redirect to project view
    }
}
