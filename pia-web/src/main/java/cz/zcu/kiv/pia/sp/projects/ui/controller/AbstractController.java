package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.service.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

// No annotation here
public abstract class AbstractController {
    protected final UserService userService;

    public AbstractController(UserService userService) {
        this.userService = userService;
    }

    // Add common model attributes to all views
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
    }
}
