package cz.zcu.kiv.pia.sp.projects.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

@Configuration
@ComponentScan(basePackages = "cz.zcu.kiv.pia.sp.projects.repository", includeFilters = @ComponentScan.Filter(classes = Repository.class))
public class ITConfiguration {
}
