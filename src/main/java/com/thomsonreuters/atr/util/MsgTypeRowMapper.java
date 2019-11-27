package com.thomsonreuters.atr.util;

import com.thomsonreuters.atr.entity.FixTag;
import com.thomsonreuters.atr.entity.MsgTypeFields;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MsgTypeRowMapper implements RowMapper<MsgTypeFields> {

    @Override
    public MsgTypeFields mapRow(ResultSet rs, int rowNum) throws SQLException {
        MsgTypeFields field = new MsgTypeFields(rs.getString("msgtype"),rs.getString("tag"));
        return field;
    }
}
