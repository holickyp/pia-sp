package cz.zcu.kiv.pia.sp.projects.mapper;

import cz.zcu.kiv.pia.sp.projects.domain.Subordinate;
import cz.zcu.kiv.pia.sp.projects.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Maps {@link ResultSet} to {@link Subordinate}.
 */
public class SubordinateMapper implements RowMapper<Subordinate> {
    @Override
    public Subordinate mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getObject("record_id", String.class);
        String superior_id = rs.getObject("superior_id", String.class);
        String subordinate_id = rs.getObject("subordinate_id", String.class);
        return new Subordinate(UUID.fromString(id), UUID.fromString(superior_id), UUID.fromString(subordinate_id));
    }
}
