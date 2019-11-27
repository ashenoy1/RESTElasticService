package com.thomsonreuters.atr.util;
import com.thomsonreuters.atr.entity.FixTag;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FixTagRowMapper implements RowMapper<FixTag> {

    @Override
    public FixTag mapRow(ResultSet rs, int rowNum) throws SQLException {
        FixTag tag = new FixTag(rs.getString("tag"),rs.getString("tagname"),rs.getString("datatype"));
        return tag;
    }
}
