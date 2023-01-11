package cz.zcu.kiv.pia.sp;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableWebFlux
@ComponentScan(basePackages = "cz.zcu.kiv.pia.sp")
public class WebAppConfig implements ApplicationContextAware, WebFluxConfigurer  {
    private static final Logger LOG = getLogger(WebAppConfig.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    @EventListener
    public void onContextStarted(ContextStartedEvent cse) {
        LOG.info("*************** APPLICATION CONTEXT STARTED ***************");

        for (String beanName : cse.getApplicationContext().getBeanDefinitionNames()) {
            LOG.info(beanName);
        }
    }

    @Bean
    public ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        // Load HTML templates from src/main/resources/templates/ directory
        templateResolver.setPrefix("classpath:/templates/");
        // Append .html suffix to view names returned from controller methods before template lookup
        templateResolver.setSuffix(".html");
        return templateResolver;
    }

    @Bean
    public ISpringWebFluxTemplateEngine templateEngine() {
        SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine();
        // WARNING: Use SpringTemplateEngine instead of SpringWebFluxTemplateEngine with Spring MVC:
        //SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setTemplateEngineMessageSource(messageSource());
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.addDialect(new LayoutDialect()); // add the ability to decorate templates
        templateEngine.addDialect(new Java8TimeDialect()); // enable access to the #temporals utility object in your templates
        templateEngine.addDialect(new SpringSecurityDialect()); // enable security features
        return templateEngine;
    }

    @Bean
    public ThymeleafReactiveViewResolver viewResolver() {
        ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
        // WARNING: Use ThymeleafViewResolver instead of ThymeleafReactiveViewResolver with Spring MVC:
        //ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        // Set common "appName" variable usable in all templates
        viewResolver.setStaticVariables(Map.of("appName", "KIV/PIA SP"));
        return viewResolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("languages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(viewResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map static resources loaded under root path to src/main/resources/static/ directory
        registry
                .addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
