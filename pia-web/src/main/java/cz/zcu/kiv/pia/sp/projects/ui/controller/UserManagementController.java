package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
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
import reactor.core.publisher.Flux;
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
     * @param principal
     * @param model
     * @return
     */
    @GetMapping("/user/management")
    public String userManagement(@AuthenticationPrincipal Principal principal, Model model) {
        User curr_user = userService.findUserByUsername(principal.getName()).block();

        var users = userService.getAllUsers();
        String curr_user_role = curr_user.getRole();
        //zobrazi jine uzivatele podle role
        switch (curr_user_role) {
            case "REGULAR-USER": users = Flux.just(curr_user); model.addAttribute("users", curr_user);break;
            case "SUPERIOR": users = userService.findSubordinatesBySuperiorId(curr_user.getId());model.addAttribute("users", users);break;
            case "PROJECT-MANAGER":users = userService.findUsersByManagerId(curr_user.getId());model.addAttribute("users", users);break;
            case "DEPARTMENT-MANAGER":users = userService.getOnlyAssignedUsers();model.addAttribute("users", users);break;
            case "SECRETARIAT":
            default: model.addAttribute("users", users);break;
        }

        //get projects of displayed users
        for(User user : users.toIterable()) {
            var projects = projectService.getProjectByUserId(user.getId());
            for(Project project : projects.toIterable()) {
                user.join(project);
            }
            var assignments = assignmentService.findByUserId(user.getId());
            model.addAttribute("assignment"+user.getUsername(), assignments);

            //spocteni vytizeni pro managery
            if(curr_user_role.equals("PROJECT-MANAGER") || curr_user_role.equals("DEPARTMENT-MANAGER")) {
                double active_workload = 0;
                double overall_workload = 0;
                for(Assignment assignment : assignments.toIterable()) {
                    if(assignment.getStatus().equals("Active")) {
                        active_workload += assignment.getScope();
                    } else {
                        if(!assignment.getStatus().equals("Past")) {
                            overall_workload += assignment.getScope();
                        }
                    }
                }
                overall_workload += active_workload;
                model.addAttribute("activeAssignmentWorkload"+user.getUsername(), active_workload);
                model.addAttribute("overallAssignmentWorkload"+user.getUsername(), overall_workload);
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
        User superior = userService.findUserByUsername(userVO.getUsername()).block();
        //superior nebyl nalezen
        if(superior == null) {
            errors.rejectValue("username", "user.username","Superior not found.");
            model.addAttribute("registrationForm", userVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(userManagement((Principal) userService.getCurrentUser(), model));
        }

        //identifikuje uzivatele ktereho chceme priradit jako subordinate (jeho username ulozeny v userVO.lastname)
        User subordinate = userService.findUserByUsername(userVO.getLastname()).block();
        //subordinate nebyl nalezen
        if(subordinate == null) {
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
