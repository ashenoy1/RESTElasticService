package com.thomsonreuters.atr.service;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
public class FixParser{


    private Map<String,String>fixTagNum;
    public Map<String,String> columns;

    public void setColumns(Map<String,String> columns){
        this.columns = columns;
    }
    public void setFixTagNum(Map<String,String> fixTagNum){
        this.fixTagNum = fixTagNum;
    }



    public FixParser(){
        columns = new HashMap<>();
        fixTagNum = new HashMap<>();
    }

    public Set<String> getColumns(){
        return columns.keySet();
    }

    public JSONObject createGroupIdJSON(Set<String> groupIds){
        String[] targetArray = groupIds.toArray(new String[groupIds.size()]);
        Arrays.parallelSetAll(targetArray, (i) -> targetArray[i].trim());
        JSONObject innerJson = new JSONObject();
        JSONObject jsonObj = new JSONObject();
        try {
            innerJson.put("groupid", new JSONArray(targetArray));
            jsonObj.put("terms", innerJson);
        }catch (JSONException e){
            log.error("JSON object creation failed at GROUPID level");
        }
        return jsonObj;
    }


    public JSONObject createJSON(String query){
        JSONObject queryObj = null;
        try {

            if (query.contains("<") || query.contains(">"))
                queryObj = JsonRangeParser(query);

            else if (query.contains("="))
                queryObj = JsonEqualsParser(query);

            else if(query.contains(" in ") || query.contains(" IN "))
                queryObj = InQuery(query);

            //Case in which query doesnt have "=, !=, ~=, <, >, <=,>=, but rather a single word to look for
            else {
                queryObj = new JSONObject();
                JSONObject queryString = new JSONObject();
                queryString.put("query", query);
                queryObj.put("query_string", queryString);

            }
        }catch(JSONException e){
            log.error("FixParser cannot parse query properly");
        }
        return queryObj;
    }

    public JSONObject JsonEqualsParser(String query) throws JSONException{
        JSONObject queryObj = new JSONObject();
        JSONObject queryString = new JSONObject();
        String array[];
        if(query.contains("=~")) {
            array= splitByDeliminator(query, "=~");
            array[1] = "*"+array[1]+"*";
            array[1] = array[1].replaceAll(" ","");
            queryString.put("default_field", array[0]);
            queryString.put("query",array[1]);
            queryObj.put("query_string",queryString);
        }
        else if (query.contains("!=")) {
            array = splitByDeliminator(query,"!=");
        }
        else {
            array = splitByDeliminator(query,"=");
        }

        if(array[1].toLowerCase().contains("present()"))
            return isFieldPresent(array);

        return matchQuery(array);
    }


    public JSONObject isFieldPresent (String[] array) throws JSONException{
        JSONObject queryObj = new JSONObject();
        JSONObject queryString = new JSONObject();
        queryString.put("field", array[0]);
        queryObj.put("exists", queryString);
        return queryObj;
    }

    public JSONObject matchQuery(String []array) throws JSONException{
        JSONObject queryObj = new JSONObject();
        JSONObject queryString = new JSONObject();
        queryString.put(array[0], array[1]);
        queryObj.put("match", queryString);
        return queryObj;
    }

    public String[] splitByDeliminator(String query, String deliminator){
        String array[] = query.split(deliminator);
        array[0] = array[0].toLowerCase();
        if(array[0].startsWith("tag")) {
            array[0] = array[0].replace("tag", "");
        }
        array[0] = array[0].replaceAll(" ","");
        array[1] = array[1].trim();
        //array[1] = array[1].replaceAll(" ","");   //Rather just do a trim()

        if(array[1].toLowerCase().contains("today()")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            array[1] = array[1].toLowerCase().replaceAll("today\\(\\)", formatter.format(date));
        }
        if (array[1].toLowerCase().contains("now()"))
            array[1] = array[1].toLowerCase().replaceAll("now\\(\\)",DateTime.now().toString());

        if(fixTagNum.containsKey(array[0]))       //HASHMAP OF OUR FIX NUM -> FIX WORDS
            array[0] = fixTagNum.get(array[0]);

        if(!columns.containsKey(array[0])){//If English value isn't part of the elastic fields retreived, set it equal to msg string (will either return data or fail)
            if(!array[0].equals("_id") && !array[0].equals("@timestamp")) {
                array[1] = array[0] + "=" + array[1];
                array[0] = "msg";
            }
        }
        return array;
    }



    public JSONObject JsonRangeParser(String query) throws JSONException{
        JSONObject queryObj = new JSONObject();
        JSONObject range = new JSONObject();
        JSONObject queryString = new JSONObject();
        String parameter;
        String deliminator;
        if(query.contains("<=")){
            deliminator = "<="; parameter = "lte";
        }
        else if(query.contains("<")){
            deliminator = "<"; parameter = "lt";
        }
        else if (query.contains(">=")){
            deliminator = ">="; parameter = "gte";
        }
        else {
            deliminator = ">"; parameter = "gt";
        }

        String[] array = splitByDeliminator(query, deliminator);
        queryString.put(parameter,array[1]);
        queryObj.put(array[0], queryString);
        range.put("range",queryObj);
        return range;
    }

    public JSONObject InQuery(String query) throws JSONException{
        JSONObject queryObj = new JSONObject();
        JSONObject queryString = new JSONObject();
        String deliminator = " IN";
        if(query.contains(" in "))
            deliminator = " in";
        String[] temp = splitByDeliminator(query,deliminator);
        query = temp[0]+":"+temp[1];
        query = query.replaceAll(","," OR ");
        queryString.put("query",query);queryObj.put("query_string",queryString);
        return queryObj;
    }


}

