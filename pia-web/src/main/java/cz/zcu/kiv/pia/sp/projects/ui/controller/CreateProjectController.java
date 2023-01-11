package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
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

import java.time.Instant;
import java.time.LocalDate;
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

    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

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
        User manager = userService.findUserByUsername(projectVO.getManager()).block();
        // manager pro zadany username nebyl nalezen
        if(manager == null) {
            errors.rejectValue("manager", "project.manager","Manager not found.");
            model.addAttribute("registrationForm", projectVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return userService.getCurrentUser().map(index -> "createProject");
        }
        //test zda projekt jiz existuje (stejne jmeno)
        Project existing_project = projectService.getProjectByName(projectVO.getName()).block();
        if(existing_project != null) {
            errors.rejectValue("name", "project.name","Project already exists.");
            model.addAttribute("registrationForm", projectVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return userService.getCurrentUser().map(index -> "createProject");
        }
        //overeni korektniho zadani datumu
        Instant from = FMT.parse(projectVO.getFrom(), Instant::from);
        Instant to = FMT.parse(projectVO.getTo(), Instant::from);
        LocalDate time_from = from.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate time_to = to.atZone(ZoneId.systemDefault()).toLocalDate();
        if(time_from.isAfter(time_to)) {
            errors.rejectValue("to", "project.to","Invalid dates. Target date must be higher than starting date.");
            model.addAttribute("registrationForm", projectVO);
            model.addAttribute("message", "Failed");
            model.addAttribute("alertClass", "alert-danger");
            return userService.getCurrentUser().map(index -> "createProject");
        }

        //vytvori novy projekt
        return userService.getCurrentUser()
                .flatMap(user -> projectService.createProject(new Project(projectVO.getName(), manager, from, to, projectVO.getDescription())))
                .map(project -> "redirect:/project/" + project.getId()); // Redirect to project view
    }
}
