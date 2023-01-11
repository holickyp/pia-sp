package cz.zcu.kiv.pia.sp.projects.ui.controller;

import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.error.UserAlreadyExistException;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import cz.zcu.kiv.pia.sp.projects.ui.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

/**
 * Controller pro editaci uzivatele
 */
@Controller
public class EditUserController extends AbstractController {
    public EditUserController(UserService userService){
        super(userService);
    }

    /**
     * stranka pro upravu uzivatele
     * @param username uzivatelova prezdivka
     * @param model model
     * @return template name
     */
    @GetMapping("/user/edit-{username}")
    public String editUser(@PathVariable String username, Model model) {
        User user = userService.findUserByUsername(username).block();

        model.addAttribute("userVO", new UserVO(user.getFirstname(), user.getLastname(),user.getUsername(),user.getPassword(),user.getRole(),user.getWorkplace(),user.getEmail()));

        return "editUser";
    }

    /**
     * POST uprav uzivatele
     * @param userVO data z formulare
     * @param errors errors
     * @param model model
     * @return template name
     */
    @PostMapping("/user/edit")
    public Mono<String> editUser(@ModelAttribute UserVO userVO, BindingResult errors, Model model) {
        User updated_user = userService.findUserByUsername(userVO.getUsername()).block();
        return userService.getCurrentUser()
                .flatMap(user -> userService.updateUser(updated_user.getId(), userVO.getFirstname(), userVO.getLastname(), userVO.getUsername(), new BCryptPasswordEncoder().encode(userVO.getPassword()), userVO.getRole(), userVO.getWorkplace(), userVO.getEmail()))
                .map(project -> "redirect:/user/management/"); // Redirect to project view
    }
}
