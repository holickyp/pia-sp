package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.enums.MinDates;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import cz.zcu.kiv.pia.sp.projects.error.InvalidDateException;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyAssignedException;
import cz.zcu.kiv.pia.sp.projects.error.UserNotFoundException;
import cz.zcu.kiv.pia.sp.projects.error.WorkloadExceededException;
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
     * stranka pro prohlizeni projektu
     * @param projectId id projektu
     * @param model model
     * @return template name
     */
    @GetMapping("/project/{projectId}")
    public String viewProject(@PathVariable UUID projectId, Model model) {
        var project = projectService.getProject(projectId);
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
    @PostMapping("/project/{projectId}-assign")
    public Mono<String> assignUser(@PathVariable UUID projectId, @ModelAttribute UserVO userVO, BindingResult errors, Model model) {
        //indetifikuje uzivatele ktereho chceme priradit
        User join_user;
        try {
            join_user = userService.findUserByUsername(userVO.getUsername()).block();
            assignmentService.isUserAssigned(join_user.getId(), projectId);
        }
        catch(UserNotFoundException e) {
            errors.rejectValue("username", "user.username","User not found.");
            model.addAttribute("registrationForm", userVO);
            model.addAttribute("message", "Failed-notFound");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(viewProject(projectId, model));
        }
        catch(UserAlreadyAssignedException e) {
            errors.rejectValue("username", "user.username","User already assigned.");
            model.addAttribute("registrationForm", userVO);
            model.addAttribute("message", "Failed-exists");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(viewProject(projectId, model));
        }

        return userService.getCurrentUser()
                .flatMap(user -> assignmentService.createAssignment(new Assignment(join_user.getId(), projectId, 0, FMT.parse(MinDates.DEFAULT_DATE.toString(), Instant::from), FMT.parse(MinDates.DEFAULT_DATE.toString(), Instant::from), "newly assigned", Status.DRAFT)))
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
    @PostMapping("/project/edit-{projectId}")
    public Mono<String> editProject(@PathVariable UUID projectId, @ModelAttribute ProjectVO projectVO, BindingResult errors, Model model) {
        return userService.getCurrentUser()
                .flatMap(user -> projectService.updateProject(projectId, projectVO.getName(), FMT.parse(projectVO.getFrom(), Instant::from), FMT.parse(projectVO.getTo(), Instant::from), projectVO.getDescription()))
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

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = Date.from(assignment.getFrom());
        String formattedFromDate = formatter.format(fromDate);
        Date toDate = Date.from(assignment.getTo());
        String formattedToDate = formatter.format(toDate);

        //predvyplni formular
        if(formattedFromDate.equals(MinDates.MIN_DATE.toString()) || formattedToDate.equals(MinDates.MIN_DATE.toString())) {
            model.addAttribute("assignmentVO", new AssignmentVO(assignment.getWorker_id(), assignment.getJob_id(), assignment.getScope(), "", "", assignment.getNote(), assignment.getStatus().toString()));
        } else {
            model.addAttribute("assignmentVO", new AssignmentVO(assignment.getWorker_id(), assignment.getJob_id(), assignment.getScope(), formattedFromDate, formattedToDate, assignment.getNote(), assignment.getStatus().toString()));
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
    @PostMapping("/assignment/edit-{assignmentId}")
    public Mono<String> editAssignment(@PathVariable UUID assignmentId, @ModelAttribute AssignmentVO assignmentVO, BindingResult errors, Model model) {
        Assignment curr_assignment = assignmentService.findById(assignmentId).block();
        try {
            assignmentService.isWorkloadExceeded(curr_assignment.getWorker_id(), assignmentId, assignmentVO.getScope());
        } catch(WorkloadExceededException e) {
            errors.rejectValue("scope", "assignment.scope","Workload exceeds 40 hours.");
            model.addAttribute("editAssignmentForm", assignmentVO);
            model.addAttribute("message", "Failed-workload");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(editAssignment(assignmentId, model));
        }

        //test zda byl datum spravne zadany
        try {
            projectService.isDateValid(assignmentVO.getTime_from(), assignmentVO.getTime_to());
        } catch(InvalidDateException e) {
            errors.rejectValue("time_to", "assignment.time_to","Invalid dates. Target date must be higher than starting date.");
            model.addAttribute("editAssignmentForm", assignmentVO);
            model.addAttribute("message", "Failed-date");
            model.addAttribute("alertClass", "alert-danger");
            return Mono.just(editAssignment(assignmentId, model));
        }

        //upravi assignment
        return userService.getCurrentUser()
                .flatMap(user -> assignmentService.updateAssignment(assignmentId, assignmentVO.getScope(), FMT.parse(assignmentVO.getTime_from(), Instant::from), FMT.parse(assignmentVO.getTime_to(), Instant::from), assignmentVO.getNote(), assignmentVO.getStatus()))
                .map(project -> "redirect:/project/" + curr_assignment.getJob_id()); // Redirect to project view
    }
}
