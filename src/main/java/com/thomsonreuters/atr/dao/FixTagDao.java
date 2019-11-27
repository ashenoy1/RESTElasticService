package com.thomsonreuters.atr.dao;

import com.thomsonreuters.atr.entity.FixTag;
import com.thomsonreuters.atr.entity.MsgTypeFields;
import com.thomsonreuters.atr.messaging.messages.FixField;

import java.util.List;
import java.util.Map;

public interface FixTagDao {
    List<FixTag> getTags();
    Map<String, String> getColumns();
    Map<String, String> getFixTagNum();
    List<MsgTypeFields> getMsgTypeToTags();
    void setFixTags();
    void setMsgTypeMappings();
    Map<String,List<FixField>> getMsgTypeMapToFix();
}
