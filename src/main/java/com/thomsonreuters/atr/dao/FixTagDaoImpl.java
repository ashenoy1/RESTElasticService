package com.thomsonreuters.atr.dao;
import com.thomsonreuters.atr.entity.FixTag;
import com.thomsonreuters.atr.entity.MsgTypeFields;
import com.thomsonreuters.atr.messaging.messages.FixField;
import com.thomsonreuters.atr.util.FixTagRowMapper;
import com.thomsonreuters.atr.util.MsgTypeRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Repository
public class FixTagDaoImpl implements FixTagDao{

    private final DataSource dataSource;

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    private JdbcTemplate jdbcTemplate;

    private List<FixTag> tags;
    private List<MsgTypeFields> msgTypeToTags;
    private Map<String,String>fixTagNum;
    public Map<String,String> columns;

    public List<FixTag> getTags() {
        return tags;
    }

    public List<MsgTypeFields> getMsgTypeToTags() {
        return msgTypeToTags;
    }
    public Map<String, String> getFixTagNum() {
        return fixTagNum;
    }
    public Map<String, String> getColumns() {
        return columns;
    }


    @Autowired
    public FixTagDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

    }

    public void setFixTags(){
        fixTagNum = new HashMap<>();
        columns = new HashMap<>();
        log.info("Postgres - Retrieving elastic fields");
        String sql = "select * from config.elasticfields";
        this.tags = jdbcTemplate.query(sql,new FixTagRowMapper());
        for(FixTag tag: tags){
            columns.put(tag.getTagname(),tag.getDatatype());
            fixTagNum.put(tag.getTag(), tag.getTagname());
        }
    }

    public void setMsgTypeMappings(){
        this.msgTypeToTags = new ArrayList<>();
        log.info("Postgres - Retrieving message type mapping for solace");
        String sql = "select * from config.msgtypefields";
        this.msgTypeToTags = jdbcTemplate.query(
                sql,new MsgTypeRowMapper());
    }

    public Map<String,List<FixField>> getMsgTypeMapToFix(){  //Function used only for pub/sub service
        setFixTags();
        setMsgTypeMappings();
        Map<String,List<FixField>> resultMap = new HashMap();
        for(MsgTypeFields temp: msgTypeToTags){
            FixField fixField = new FixField();
            fixField.setTagNumber(temp.getTag());
            fixField.setTagName(fixTagNum.get(temp.getTag()));
            resultMap.computeIfAbsent(temp.getMsgtype(), k-> new ArrayList<>()).add(fixField);
        }
        return resultMap;
    }


    public int addFixTag(FixTag tag){
        return 0;
    }
    public int deleteFixTag(int id){
        return 0;
    }
    public int updateFixTag(FixTag tag){
        return 0;
    }

}
