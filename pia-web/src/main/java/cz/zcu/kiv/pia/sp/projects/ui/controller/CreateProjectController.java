package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.error.InvalidDateException;
import cz.zcu.kiv.pia.sp.projects.error.ProjectAlreadyExistException;
import cz.zcu.kiv.pia.sp.projects.error.UserNotFoundException;
import cz.zcu.kiv.pia.sp.projects.service.ProjectService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import cz.zcu.kiv.pia.sp.projects.ui.vo.ProjectVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Controller pro vytvoreni noveho projektu
 */
@Controller
public class CreateProjectController extends AbstractController {
    // Autowire ProjectService using constructor-based dependency injection
    private final ProjectService projectService;

    public CreateProjectController(ProjectService projectService, UserService userService) {
        super(userService);
        this.projectService = projectService;
    }

    /**
     * stranka pro vytvoreni projektu
     * @param model model
     * @return template name
     */
    @GetMapping("/project/create")
    public String createProject(Model model) {
        model.addAttribute("projectVO", new ProjectVO("", null,  null, null, ""));

        // Return the template name (templates/createProject.html)
        return "createProject";
    }

    /**
     * POST vytvoreni noveho projektu
     * @param projectVO data z formulare
     * @param errors errors
     * @param model model
     * @return template name
     */
    @PostMapping("/project/create")
    public Mono<String> createProject(@ModelAttribute ProjectVO projectVO, BindingResult errors, Model model) {
        User manager;
        try {
            manager = userService.findUserByUsername(projectVO.getManager()).block();
        } catch(UserNotFoundException e) {
            // manager pro zadany username nebyl nalezen
            errors.rejectValue("manager", "project.manager","Manager not found.");
            model.addAttribute("registrationForm", projectVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return userService.getCurrentUser().map(index -> "createProject");
        }

        //test zda projekt jiz existuje (stejne jmeno)
        try {
            projectService.projectExists(projectVO.getName()).block();
        } catch(ProjectAlreadyExistException e) {
            errors.rejectValue("name", "project.name","Project already exists.");
            model.addAttribute("registrationForm", projectVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return userService.getCurrentUser().map(index -> "createProject");
        }

        //overeni korektniho zadani datumu
        try {
            projectService.isDateValid(projectVO.getFrom(), projectVO.getTo());
        } catch(InvalidDateException e) {
            errors.rejectValue("to", "project.to","Invalid dates. Target date must be higher than starting date.");
            model.addAttribute("registrationForm", projectVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return userService.getCurrentUser().map(index -> "createProject");
        }

        //vytvori novy projekt
        return userService.getCurrentUser()
                .flatMap(user -> projectService.createProject(projectVO.getName(), manager, projectVO.getFrom(), projectVO.getTo(), projectVO.getDescription()))
                .map(project -> "redirect:/project/" + project.getId()); // Redirect to project view
    }
}
