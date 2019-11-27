package com.thomsonreuters.atr.entity;
import com.thomsonreuters.atr.service.FixParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;


//Query bean created for every new request


@Slf4j
public class Query {
    private String query;
    private int offset;
    private String groupId;

    public String getUUid() {
        return uuId;
    }

    public void setUUid(String uuId) {
        this.uuId = uuId;
    }

    private String uuId;



    public String getGroupId() { return groupId;}
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public int getOffset() {
        return offset;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
    public String getQuery() {
        return this.query;
    }
    public void setQuery(String query) {
        this.query = query;
    }


    private final String PARSING_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    private ArrayList<ArrayList<JSONObject>> result;
    private ArrayList<JSONObject> andList;
    private ArrayList<JSONObject> orList;
    private ArrayList<JSONObject> notList;
    private ArrayList<JSONObject> orderByList;

    public ArrayList<ArrayList<JSONObject>> parseQuery(FixParser fixParser) {
        result = new ArrayList<>();
        andList = new ArrayList<>();
        orList = new ArrayList<>();
        notList = new ArrayList<>();
        orderByList = new ArrayList<>();
        if(groupId!=null && groupId.length() > 0){
            String[] groupIds = groupId.replaceAll("[\\[\\]]", "").split(",");
            Set<String> set = new HashSet<>(Arrays.asList(groupIds)); //Remove any duplicates
            log.info("GroupIds for this user are: {}", set.toString());
            andList.add(fixParser.createGroupIdJSON(set));
        }

        String[] aEach = query.split(String.format(PARSING_DELIMITER, (" (?i)and | (?i)or ")));
        for (int i = 0; i < aEach.length; i++){
            aEach[i] = isOrderBy(fixParser, aEach[i]); // Checks query to see if orderBy is used
        }
        ArrayList<String> queries = new ArrayList(Arrays.asList(aEach));

        //In the case of just one query
        if(queries.size() == 1) {
            if(queries.get(0).contains("!="))
                notList.add(fixParser.createJSON(queries.get(0)));
            else
                orList.add(fixParser.createJSON(queries.get(0)));
        }


        //Take care of the first query in a multiple query string
        else{
            if(queries.get(0).contains("!="))
                notList.add(fixParser.createJSON(queries.get(0)));
            else if(queries.get(1).toLowerCase().equals(" and ")){
                andList.add(fixParser.createJSON(queries.get(0)));
            }
            else if(queries.get(1).toLowerCase().equals(" or ")) {
                orList.add(fixParser.createJSON(queries.get(0)));
            }
            else
                return null;
        }


        //The remaining queries
        for(int i = 1; i<queries.size();i+=2){
            if(i+1>= queries.size())
                return null;
            if(queries.get(i+1).contains("!="))
                notList.add(fixParser.createJSON(queries.get(i+1)));
            else if(queries.get(i).toLowerCase().equals(" and ")){
                andList.add(fixParser.createJSON(queries.get(i+1)));
            }
            else if(queries.get(i).toLowerCase().equals(" or ")){
                orList.add(fixParser.createJSON(queries.get(i+1)));
            }
            else
                return null;
        }
        result.add(andList);
        result.add(orList);
        result.add(notList);
        result.add(orderByList);
        return result;
    }

    public String isOrderBy(FixParser fixParser, String queryToCheck) {
        String orderByQuery = queryToCheck.toLowerCase();
        int index;
        String buff;
        String defaultOrder = "desc";

        if (orderByQuery.contains("orderby")) {
            index = orderByQuery.indexOf("orderby");
            buff = "orderby";
        }
        else if (orderByQuery.contains("order by")) {
            index = orderByQuery.indexOf("order by");
            buff = "order by";
        }
        else
            return queryToCheck;

        String orderBy = orderByQuery.substring(index);
        queryToCheck = queryToCheck.substring(0,index);
        orderBy = orderBy.replaceAll(buff, "");
        orderBy = orderBy.trim();
        String [] arr = orderBy.split(" ");
        if(arr.length > 1 && arr[1].equals("asc"))
            defaultOrder = "asc";

        if(fixParser.columns.containsKey(arr[0])){
            if(fixParser.columns.get(arr[0]).equals("text"))
                arr[0] = arr[0] + ".keyword";
        }
        JSONObject orderJSON = generateOrderBy(arr[0],defaultOrder);
        orderByList.add(orderJSON);
        return queryToCheck;
    }

    public JSONObject generateOrderBy(String field, String order){
        JSONObject sortJSON = new JSONObject();
        JSONObject orderJSON = new JSONObject();
        try {
            sortJSON.put("order", order);
            orderJSON.put(field, sortJSON);
        }catch (JSONException e){
            log.error("JSON object creation failed at ORDERBY level");
        }
        return orderJSON;
    }

}

