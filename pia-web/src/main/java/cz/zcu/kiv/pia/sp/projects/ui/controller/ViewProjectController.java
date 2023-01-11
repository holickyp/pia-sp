package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.service.AssignmentService;
import cz.zcu.kiv.pia.sp.projects.service.ProjectService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import cz.zcu.kiv.pia.sp.projects.ui.vo.AssignmentVO;
import cz.zcu.kiv.pia.sp.projects.ui.vo.ProjectVO;
import cz.zcu.kiv.pia.sp.projects.ui.vo.UserVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.UUID;

/**
 * Controller pro zobrazeni projektu
 */
@Controller
public class ViewProjectController extends AbstractController {
    // Autowire ProjectService using constructor-based dependency injection
    private final ProjectService projectService;
    private final AssignmentService assignmentService;
    private UUID currentProject_id;

    private UUID currentAssignment_id;
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    public ViewProjectController(ProjectService projectService, UserService userService, AssignmentService assignmentService) {
        super(userService);
        this.projectService = projectService;
        this.assignmentService = assignmentService;
    }

    /**
     * @return id aktualne prohlizeneho projektu
     */
    private UUID getCurrentProjectID() {
        return currentProject_id;
    }

    /**
     * nastavi id aktualne prohlizeneho projektu
     * @param project_id id aktualne prohlizeneho projektu
     */
    private void setCurrentProject(UUID project_id) {
        this.currentProject_id = project_id;
    }

    /**
     * @return id aktualne prohlizeneho assignmentu
     */
    private UUID getCurrentAssignmentID() {
        return currentAssignment_id;
    }

    /**
     * nastavi id aktualne prohlizeneho assignmentu
     * @param assignment_id id aktualne prohlizeneho assignmentu
     */
    private void setCurrentAssignment(UUID assignment_id) {
        this.currentAssignment_id = assignment_id;
    }

    /**
     * stranka pro prohlizeni projektu
     * @param projectId id projektu
     * @param model model
     * @return template name
     */
    @GetMapping("/project/{projectId}")
    public String viewProject(@PathVariable UUID projectId, Model model) {
        var project = projectService.getProject(projectId);
        setCurrentProject(projectId);
        // Mono will be resolved before rendering.
        model.addAttribute("project", project);

        var users = userService.findUsersByProjectId(projectId);
        model.addAttribute("users", users);

        var assignments = assignmentService.findByProjectId(projectId);
        model.addAttribute("assignments", assignments);

        model.addAttribute("userVO", new UserVO("", "","","","","",""));

        // Return the template name (templates/viewProject.html)
        return "viewProject";
    }

    /**
     * POST prirad uzivatele do projektu
     * @param userVO data z formulare
     * @param errors errors
     * @param model model
     * @return template name
     */
    @PostMapping("/project/assign")
    public Mono<String> assignUser(@ModelAttribute UserVO userVO, BindingResult errors, Model model) {
        //indetifikuje uzivatele ktereho chceme priradit
        User join_user = userService.findUserByUsername(userVO.getUsername()).block();
        if(join_user == null) {
            errors.rejectValue("username", "user.username","User not found.");
            model.addAttribute("registrationForm", userVO);
            model.addAttribute("message", "Failed-notFound");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(viewProject(getCurrentProjectID(), model));
        }

        var assignments = assignmentService.findByUserId(join_user.getId());
        for(Assignment assignment : assignments.toIterable()) {
            if(assignment.getJob_id().equals(getCurrentProjectID())) {
                errors.rejectValue("username", "user.username","User already assigned.");
                model.addAttribute("registrationForm", userVO);
                model.addAttribute("message", "Failed-exists");
                model.addAttribute("alertClass", "alert-danger");
                return Mono.just(viewProject(getCurrentProjectID(), model));
            }
        }

        return userService.getCurrentUser()
                .flatMap(user -> assignmentService.createAssignment(new Assignment(join_user.getId(), getCurrentProjectID(), 0, FMT.parse("1000-01-01", Instant::from), FMT.parse("1000-01-01", Instant::from), "newly assigned", "Draft")))
                .map(project -> "redirect:/project/" + project.getJob_id()); // Redirect to project view
    }

    /**
     * stranka pro upravu projektu
     * @param projectId id projektu
     * @param model model
     * @return template name
     */
    @GetMapping("/project/edit-{projectId}")
    public String editProject(@PathVariable UUID projectId, Model model) {
        Project project = projectService.getProject(projectId).block();
        setCurrentProject(projectId);
        // Mono will be resolved before rendering.
        model.addAttribute("project", project);


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = Date.from(project.getFrom());
        String formattedFromDate = formatter.format(fromDate);
        Date toDate = Date.from(project.getTo());
        String formattedToDate = formatter.format(toDate);

        //predvyplni formular
        model.addAttribute("projectVO", new ProjectVO(project.getName(), project.getManager().getUsername(),  formattedFromDate, formattedToDate, project.getDescription()));

        // Return the template name (templates/editProject.html)
        return "editProject";
    }

    /**
     * POST uprav projekt
     * @param projectVO data z formulare
     * @param errors errors
     * @param model model
     * @return template name
     */
    @PostMapping("/project/edit")
    public Mono<String> editProject(@ModelAttribute ProjectVO projectVO, BindingResult errors, Model model) {
        return userService.getCurrentUser()
                .flatMap(user -> projectService.updateProject(getCurrentProjectID(), projectVO.getName(), FMT.parse(projectVO.getFrom(), Instant::from), FMT.parse(projectVO.getTo(), Instant::from), projectVO.getDescription()))
                .map(project -> "redirect:/project/" + project.getId()); // Redirect to project view
    }

    /**
     * stranka pro upravu assignmentu
     * @param assignmentId id assignmentu
     * @param model model
     * @return template name
     */
    @GetMapping("/assignment/edit-{assignmentId}")
    public String editAssignment(@PathVariable UUID assignmentId, Model model) {
        Assignment assignment = assignmentService.findById(assignmentId).block();
        model.addAttribute("assignment", assignment);
        setCurrentAssignment(assignmentId);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = Date.from(assignment.getFrom());
        String formattedFromDate = formatter.format(fromDate);
        Date toDate = Date.from(assignment.getTo());
        String formattedToDate = formatter.format(toDate);

        //predvyplni formular
        if(formattedFromDate.equals("0999-12-27") || formattedToDate.equals("0999-12-27")) {
            model.addAttribute("assignmentVO", new AssignmentVO(assignment.getWorker_id(), assignment.getJob_id(), assignment.getScope(), "", "", assignment.getNote(), assignment.getStatus()));
        } else {
            model.addAttribute("assignmentVO", new AssignmentVO(assignment.getWorker_id(), assignment.getJob_id(), assignment.getScope(), formattedFromDate, formattedToDate, assignment.getNote(), assignment.getStatus()));
        }

        return "editAssignment";
    }

    /**
     * POST uprav assignment
     * @param assignmentVO data z formulare
     * @param errors errors
     * @param model model
     * @return template name
     */
    @PostMapping("/assignment/edit")
    public Mono<String> editAssignment(@ModelAttribute AssignmentVO assignmentVO, BindingResult errors, Model model) {
        UUID target_user_id = assignmentService.findById(getCurrentAssignmentID()).block().getWorker_id();
        var assignments = assignmentService.findByUserId(target_user_id);
        //spocte uvazek
        double workload = 0;
        for(Assignment assignment : assignments.toIterable()) {
            if(assignment.getId().equals(getCurrentAssignmentID())) {
                continue;
            }
            workload += assignment.getScope();
        }
        workload += assignmentVO.getScope();
        //test zda uvazek neprekroci maximum
        if(workload > 40) {
            errors.rejectValue("scope", "assignment.scope","Workload exceeds 40 hours.");
            model.addAttribute("editAssignmentForm", assignmentVO);
            model.addAttribute("message", "Failed-workload");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(editAssignment(getCurrentAssignmentID(), model));
        }
        //test zda byl datum spravne zadany
        Instant from = FMT.parse(assignmentVO.getTime_from(), Instant::from);
        Instant to = FMT.parse(assignmentVO.getTime_to(), Instant::from);
        LocalDate time_from = from.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate time_to = to.atZone(ZoneId.systemDefault()).toLocalDate();
        if(time_from.isAfter(time_to)) {
            errors.rejectValue("time_to", "assignment.time_to","Invalid dates. Target date must be higher than starting date.");
            model.addAttribute("editAssignmentForm", assignmentVO);
            model.addAttribute("message", "Failed-date");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(editAssignment(getCurrentAssignmentID(), model));
        }

        //upravi assignment
        return userService.getCurrentUser()
                .flatMap(user -> assignmentService.updateAssignment(getCurrentAssignmentID(), assignmentVO.getScope(), from, to, assignmentVO.getNote(), assignmentVO.getStatus()))
                .map(project -> "redirect:/project/" + getCurrentProjectID()); // Redirect to project view
    }
}
