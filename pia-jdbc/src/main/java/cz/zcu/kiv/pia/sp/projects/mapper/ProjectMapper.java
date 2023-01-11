package cz.zcu.kiv.pia.sp.projects.mapper;

import cz.zcu.kiv.pia.sp.projects.domain.Project;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import cz.zcu.kiv.pia.sp.projects.repository.InMemoryUserRepository;
import cz.zcu.kiv.pia.sp.projects.repository.UserRepository;
import cz.zcu.kiv.pia.sp.projects.service.DefaultUserService;
import cz.zcu.kiv.pia.sp.projects.service.UserService;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.UUID;

/**
 * Maps {@link ResultSet} to {@link Project}.
 *
 * @see ProjectMapper
 */
public class ProjectMapper implements RowMapper<Project> {
    private static final UserMapper USER_MAPPER = new UserMapper();

    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getObject("project_id", String.class);
        String name = rs.getObject("name", String.class);
        User manager = USER_MAPPER.mapRow(rs, rowNum);
        String from = rs.getObject("time_from", String.class);
        String to = rs.getObject("time_to", String.class);
        String description = rs.getObject("description", String.class);

        return new Project(UUID.fromString(id), name, manager, FMT.parse(from, Instant::from), FMT.parse(to, Instant::from), description);
    }
}
