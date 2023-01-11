package cz.zcu.kiv.pia.sp.projects.ui.vo;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.UUID;

/**
 * Value object used in the form for creating a new project
 *
 */
public class ProjectVO {
    private final String name;

    private final String manager;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private final String from;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private final String to;
    private final String description;

    /**
     * @param name Entered project name
     * @param from Entered project beginning date
     * @param to Entered project ending date
     * @param description Entered project description
     */
    public ProjectVO(String name, String manager, String from, String to, String description) {
        this.name = name;
        this.manager = manager;
        this.from = from;
        this.to = to;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getManager() {
        return manager;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDescription() {
        return description;
    }
}
