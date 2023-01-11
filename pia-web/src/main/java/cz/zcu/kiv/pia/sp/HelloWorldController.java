package cz.zcu.kiv.pia.sp;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

import static org.slf4j.LoggerFactory.getLogger;

@Controller
public class HelloWorldController {

    private static final Logger LOG = getLogger(HelloWorldController.class);

    @GetMapping("/hello")
    public Mono<String> sayHello(@RequestParam String from, Model model) {
        var builder = new StringBuilder("Hello World");
        if (from != null) {
            builder.append(" from ").append(from);
        }
        builder.append("!");

        LOG.info(builder.toString());

        model.addAttribute("greeting", builder);
        return Mono.just("greeting");
    }

    @GetMapping(path = "/greet/{name}", produces = MediaType.TEXT_HTML_VALUE + "; charset=utf-8")
    public Mono<String> greet(@PathVariable String name) {
        return Mono.just("<h1>Hello " + name + "!</h1>");
    }
}
