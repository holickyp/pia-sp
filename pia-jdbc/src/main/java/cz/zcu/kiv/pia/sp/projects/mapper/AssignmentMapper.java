package cz.zcu.kiv.pia.sp.projects.mapper;

import cz.zcu.kiv.pia.sp.projects.domain.Assignment;
import cz.zcu.kiv.pia.sp.projects.enums.Status;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.UUID;

/**
 * Maps {@link ResultSet} to {@link Assignment}.
 */
public class AssignmentMapper implements RowMapper<Assignment> {

    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("Europe/Prague"));

    @Override
    public Assignment mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getObject("assignment_id", String.class);
        String worker_id = rs.getObject("worker_id", String.class);
        String job_id = rs.getObject("job_id", String.class);
        String scope = rs.getObject("scope", String.class);
        String from = rs.getObject("time_from", String.class);
        String to = rs.getObject("time_to", String.class);
        String note = rs.getObject("note", String.class);
        String status = rs.getObject("status", String.class);

        return new Assignment(UUID.fromString(id), UUID.fromString(worker_id), UUID.fromString(job_id), Double.parseDouble(scope), FMT.parse(from, Instant::from), FMT.parse(to, Instant::from), note, Status.getStatusByString(status));
    }
}
