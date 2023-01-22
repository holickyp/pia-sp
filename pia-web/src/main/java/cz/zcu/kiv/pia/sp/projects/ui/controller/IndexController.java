package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.service.ProjectService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller pro index stranku
 */
// Note that we're not using @RestController here
@Controller
public final class IndexController extends AbstractController {

    // Autowire RoomService using constructor-based dependency injection
    private final ProjectService projectService;

    public IndexController(UserService userService, ProjectService projectService) {
        super(userService);
        this.projectService = projectService;
    }

    // Accept query parameter "q" used for searching projects, submitted from navbar search form (see layout.html)
    @GetMapping
    public String index(@RequestParam(name = "q", required = false) String query, @RequestParam(name = "lang", required = false) String language, Model model) {
        model.addAttribute("query", query);

        var projects = projectService.searchProjects(query);
        // Flux will be resolved before rendering.
        model.addAttribute("projects", projects);

        // Return the template name (templates/index.html)
        return "index";
    }
}
