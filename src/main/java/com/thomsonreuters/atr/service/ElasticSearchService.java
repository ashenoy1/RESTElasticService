package com.thomsonreuters.atr.service;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thomsonreuters.atr.dao.FixTagDao;
import com.thomsonreuters.atr.entity.*;
import com.thomsonreuters.atr.messaging.config.IFixDomainConfiguration;
import com.thomsonreuters.atr.messaging.messages.FixField;
import com.thomsonreuters.atr.util.ConfigStrings;
import lombok.extern.slf4j.*;
import org.apache.http.client.utils.URIBuilder;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class ElasticSearchService {
    @Autowired
    private FixTagDao dao;
    private FixParser fixParser;
    private AdminPubSub adminPubSub; //Connection to Admin RESTService to retrieve Groupids

    private int maxResultSize;
    private String requestTimeout;

    private String awsMsgURL; //URL for messageSearch
    private String awsLogURL; //URL for logsearch

    private RestTemplate template;


    public void setTemplate(RestTemplate template) {   //Setter created for better testing purposes
        this.template = template;
    }

    public ElasticSearchService() {
        fixParser = new FixParser();
    }

    public Map<String,List<FixField>> msgTypeToTagsMapping(){
       return dao.getMsgTypeMapToFix();
   }

    public void init(IFixDomainConfiguration configuration) throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(configuration.getConfigurationString(ConfigStrings.Protocol,"https"));
        builder.setHost(configuration.getConfigurationString(ConfigStrings.HOST,"vpc-atr-dev-76u4zommjpkv7ocf5ws6bxtqye.us-east-1.es.amazonaws.com"));
        builder.setPort((configuration.getConfigurationInteger(ConfigStrings.PORT,443)));
        builder.setPath(configuration.getConfigurationString(ConfigStrings.MessageIndexName,"msglog-dev*") + "/_search");
        this.awsMsgURL = builder.build().toString();
        System.out.println(this.awsMsgURL + " is the awsMsg url");
        builder.setPath(configuration.getConfigurationString(ConfigStrings.LogIndexName,"srvlog-dev*") + "/_search");
        this.awsLogURL = builder.build().toString();
        this.maxResultSize = (configuration.getConfigurationInteger(ConfigStrings.MessageMaxResultSize,1000));
        this.requestTimeout = configuration.getConfigurationString(ConfigStrings.RequestTimeout+"ms", "120000ms");
        dao.setFixTags();
        fixParser.setColumns(dao.getColumns());
        fixParser.setFixTagNum(dao.getFixTagNum());
        adminPubSub = new AdminPubSub(configuration);
    }

    public void setAwsMsgURL(String url){
        this.awsMsgURL = url;
    }

    public void setAwsLogURL(String awsLogURL) {
        this.awsLogURL = awsLogURL;
    }


    //Querying API
    public ResponseEntity<String> getQuery(Query query, boolean isMessage){
        if(query == null)
            return null;
        try{
            log.info("Incoming Query: " + query.getQuery());
            String URL;
            if(isMessage) {
                URL = awsMsgURL;
                List<Integer> groupIds = adminPubSub.getGroupIdList(query.getUUid());
                if(groupIds == null || groupIds.size() <= 0) {
                    log.info("Result size is 0 - GroupIds doesn't exist for this User");
                    return null;
                }
                query.setGroupId(groupIds.toString());
            }
            else
                URL = awsLogURL;

            ArrayList<ArrayList<JSONObject>> list = query.parseQuery(fixParser);
            String JSONString = createJsonString(list, query.getOffset(), !isMessage);
            log.trace("JSON string being sent to ElasticSearch: {} " + JSONString);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> request = new HttpEntity(JSONString,headers);
            log.trace("Connecting to Elastic Search at " + URL);
            ResponseEntity<String> result =  template.exchange(
                   URL, HttpMethod.POST, request, String.class);
            return result;
        }catch(Exception e){
            log.error("Response template failed to create a ResponseEntity");
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<String> messageZoom(Query query){
        if(!query.getQuery().contains("_id"))
            return null;

        log.trace("ID for message zoom is: " + query.getQuery());
        query.setOffset(0);
        return getQuery(query,true);
    }

    public SessionResponse sessionZoom(SessionRequest request){ //MIGHT NEED TO DIVIDE SECONDS by 1000
        if(request.getDirection() == null)
            request.setDirection("both");

        if (request.getTimeOffset() == 0)
            request.setTimeOffset(3600);  //WHAT IF timeOFFSET WAS ORIGINALLY 0?

        int offset = request.getTimeOffset();
        Instant fromDate = Instant.parse(request.getTimestamp());
        Instant toDate = Instant.parse(request.getTimestamp());


        if (request.getDirection().equals("back")) {
            fromDate = fromDate.minusSeconds(offset);
        }
        else if (request.getDirection().equals("forward")) {
            toDate = toDate.plusSeconds(offset);
        }
        else {
            fromDate = fromDate.minusSeconds(offset);
            toDate = toDate.plusSeconds(offset);
        }
        List<String> queries = new ArrayList<>();
        List<Response> sessionListBuyers = new ArrayList<>();
        List<Response> sessionListBrokers = new ArrayList<>();


        //Sessions Incoming from Seller
        queries.add("timestamp > " + fromDate.toString() + " AND timestamp < " + toDate.toString() + " AND msgtype = D "
                + "AND sendercompid = " + request.getBuySideSession() + " AND delivertocompid =  " + request.getSellSideSession());
        queries.add("timestamp > " + fromDate.toString() + " AND timestamp < " + toDate.toString() + " AND msgtype = D "
                + "AND onbehalfofcompid = " + request.getBuySideSession() + " AND targetcompid =  " + request.getSellSideSession());

        //Sessions Incoming from Broker
        queries.add("timestamp > " + fromDate.toString() + " AND timestamp < " + toDate.toString() + " AND msgtype = D "
               + "AND sendercompid = " + request.getSellSideSession() + " AND delivertocompid =  " + request.getBuySideSession());
        queries.add("timestamp > " + fromDate.toString() + " AND timestamp < " + toDate.toString() + " AND msgtype = D "
               + "AND onbehalfofcompid = " + request.getSellSideSession() + " AND targetcompid =  " + request.getBuySideSession());


        //Generate Buyers/Brokers <Response> List
        for(int i = 0;i<queries.size();i++){
            Query sessionZoom = new Query();
            sessionZoom.setQuery(queries.get(i));
            sessionZoom.setOffset(0);
            if(i<2) {
                sessionListBuyers.add(getResponse(getQuery(sessionZoom,true), false,false));
            }
            else{
                sessionListBrokers.add(getResponse(getQuery(sessionZoom,true), false,false));
            }
        }

        SessionResponse resultBuyers = sessionZoomResponse(sessionListBuyers,request.getBuySideSession(),request.getSellSideSession(),true);
        SessionResponse resultBrokers = sessionZoomResponse(sessionListBrokers,request.getBuySideSession(),request.getSellSideSession(),false);
        resultBuyers.addToHits(resultBrokers.getHits());
        return resultBuyers;
    }

    public SessionResponse sessionZoomResponse(List<Response> resultList,String buySession, String sellSession, boolean isFromBuyer){
        Map<String,JsonNode> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        for(int i = 0; i<resultList.size();i++) {  //Iterates 4 times
            List<JsonNode> currHitList = resultList.get(i).getHits();
            for (JsonNode temp:currHitList) {
                SessionBean newBean = null;
                try {
                    newBean = mapper.treeToValue(temp, SessionBean.class);
                }catch(JsonProcessingException e){
                    log.error("Failed to create session bean");
                }
                newBean.setFieldsForSession(isFromBuyer,buySession,sellSession);
                String nodeString = temp.get("clordid").toString();
                map.putIfAbsent(nodeString,mapper.createObjectNode());
                JsonNode nodeForMap = setDirection(mapper, map.get(nodeString), newBean, buySession, sellSession);
                map.put(nodeString, nodeForMap);
            }
        }
        int hitCount = map.size();
        int timeTook = Math.max(Integer.parseInt(resultList.get(0).getTimeTook()),Integer.parseInt(resultList.get(1).getTimeTook()));
        List<JsonNode> result = new ArrayList<>(map.values());
        SessionResponse response = new SessionResponse(Integer.toString(timeTook),Integer.toString(hitCount),result);
        return response;
    }


    //I,O indicating LEFT or RIGHT side. i,o indicating pointing left or right
    public JsonNode setDirection(ObjectMapper mapper, JsonNode mapNode, SessionBean bean,String buySession, String sellSession){
        if (bean.getMsgdir().equals("I")){
            if(bean.getSendercompid().equals(buySession))
                bean.setDirection("i");
            else if(bean.getSendercompid().equals(sellSession))
                bean.setDirection("o");

            JsonNode newNode = mapper.valueToTree(bean);
            ((ObjectNode) mapNode).set("in",newNode);
        }
        else if (bean.getMsgdir().equals("O")){
            if(bean.getTargetcompid().equals(sellSession)) {
                bean.setDirection("i");
            }
            else if(bean.getTargetcompid().equals(buySession)) {
                bean.setDirection("o");
            }
            JsonNode newNode = mapper.valueToTree(bean);
            ((ObjectNode) mapNode).set("out",newNode);  //NEED TO CHANGE direction to "i" inside bean (setter?)

        }
        return mapNode;
    }



    public static String CleanInvalidXmlChars(String text) {
        String test1 = "\\\\u0001";
        return text.replaceAll(test1, "|");
        //return text;
    }


    //Custom Response
    public Response getResponse(ResponseEntity<String> response, boolean isMsgZoom, boolean isLogSearch) {
        if(response == null)
            return null;
        log.trace("Generating custom Response from Elastic Search results");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode timeTook = root.path("took");
            JsonNode hits = root.path("hits");
            JsonNode hits2 = hits.get("hits");
            log.trace("Time in milliseconds for Elasticsearch to execute the search is " + timeTook.toString());
            log.info("Result size is " + hits.get("total"));
            List<JsonNode> result = new ArrayList<>();
            for(JsonNode temp : hits2){
                JsonNode buffer = temp.get("_source");
                if(isLogSearch){
                    JsonNode MSG = buffer.get("message");
                    if(!MSG.asText().equals(""))  //To stop empty logs from returning
                        result.add(MSG);
                }
                else {
                    if (isMsgZoom) {
                        log.trace("MessageZoom is grabbing the msg string");
                        JsonNode node2 = mapper.createObjectNode();
                        String msgStringAfter = CleanInvalidXmlChars(buffer.get("msg").textValue());
                        ((ObjectNode) node2).put("msg", msgStringAfter);
                        ((ObjectNode) node2).put("_id", temp.get("_id").textValue());
                        result.add(node2);
                        return new Response(timeTook.toString(), hits.get("total").toString(), result);
                    }
                    ObjectNode node = (ObjectNode) buffer;
                    node.put("_id", temp.get("_id").textValue());
                    result.add(mapper.readTree(CleanInvalidXmlChars(node.toString())));
                }
            }
            return new Response(timeTook.toString(), hits.get("total").toString(),result);
        }catch (IOException e){
            log.error("ResponseEntity<String> was not able to convert to Response");
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<String> getLog(LogSearch search){
        log.trace("Logs are being returned");
        List<String> list = search.applyTimeOffset();
        String queryString = "@timestamp > " + list.get(0) + " AND @timestamp < " + list.get(1);
        Query query = new Query();
        query.setQuery(queryString);
        query.setOffset(0);
        return getQuery(query,false);
    }


    //Generates the JSON String needed for queries
    public String createJsonString(ArrayList<ArrayList<JSONObject>> list, int offset, boolean isLogSearch){
        JSONObject query = new JSONObject();
        JSONObject bool = new JSONObject();
        JSONObject queries = new JSONObject();
        List<String> columns = new ArrayList<>(fixParser.getColumns());
        try{
            if(list.get(3).size() == 0){
                JSONObject sortJSON = new JSONObject();
                JSONObject orderJSON = new JSONObject();
                String timeStamp = "timestamp";
                if(isLogSearch)
                    timeStamp = "@timestamp";
                try {
                    sortJSON.put("order", "desc");
                    orderJSON.put(timeStamp, sortJSON);
                }catch (JSONException e){
                    log.error("JSON object creation failed at ORDERBY level");
                }
                list.get(3).add(orderJSON);
            }
            queries.put("must_not", new JSONArray(list.get(2)));
            queries.put("should",new JSONArray(list.get(1)));
            queries.put("must",new JSONArray(list.get(0)));
            bool.put("bool",queries);
            query.put("query",bool);
            query.put("sort",new JSONArray(list.get(3)));
            query.put("timeout",requestTimeout);
            if(!isLogSearch)
                query.put("_source", new JSONArray(columns));

            query.put("size",maxResultSize);
            query.put("from", offset);
        }catch(JSONException e){
            log.error("Failed to create JSON Object");
        }
        return query.toString();
    }

}




