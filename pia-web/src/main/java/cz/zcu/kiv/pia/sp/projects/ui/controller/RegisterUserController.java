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
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

/**
 * Controller pro vytvoreni noveho uzivatele
 */
@Controller
public class RegisterUserController extends AbstractController {

    public RegisterUserController(UserService userService){
        super(userService);
    }

    /**
     * stranka pro vytvoreni noveho uzivatele
     * @param model model
     * @return template name
     */
    @GetMapping("/user/register")
    public String registerUser(Model model) {
        model.addAttribute("userVO", new UserVO("", "","","","","",""));

        return "registerUser";
    }

    /**
     * POST vytvor noveho uzivatele
     * @param userVO
     * @param errors
     * @param model
     * @return
     */
    @PostMapping("/user/register")
    public Mono<String> registerUser(@ModelAttribute UserVO userVO, BindingResult errors, Model model) {
        Mono<User> curr_user =  userService.getCurrentUser();
        Mono<User> register = curr_user.flatMap(user -> {
            try {
                Mono<User> new_user = userService.registerUser(new User(userVO.getFirstname(), userVO.getLastname(), userVO.getUsername(), new BCryptPasswordEncoder().encode(userVO.getPassword()), userVO.getRole(), userVO.getWorkplace(), userVO.getEmail()));
                model.addAttribute("message", "Success");
                model.addAttribute("alertClass", "alert-success");
                return new_user;
            } catch (UserAlreadyExistException e) {
                errors.rejectValue("username", "userData.username","An account already exists for this username.");
                model.addAttribute("registrationForm", userVO);
                model.addAttribute("message", "Failed");
                model.addAttribute("alertClass", "alert-danger");
                return userService.getCurrentUser();
            }
        });

        return register.map(index -> "registerUser");
    }
}
