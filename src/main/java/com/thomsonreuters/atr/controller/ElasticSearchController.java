package com.thomsonreuters.atr.controller;
import com.thomsonreuters.atr.entity.*;
import com.thomsonreuters.atr.messaging.messages.FixField;
import com.thomsonreuters.atr.service.ElasticSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@Slf4j
@Api(value = "RESTElastic", description = "API to retrieve ElasticSearch data")
public class ElasticSearchController {

    private final ElasticSearchService elasticSearchService;

    @Autowired
    public ElasticSearchController(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }


    @RequestMapping(value="api/messagesearch/1.0/getMappings",method = RequestMethod.GET)
    public Map<String,List<FixField>> getMappings(){
        return elasticSearchService.msgTypeToTagsMapping();
    }


    @ApiOperation(value = "Retrieve data from ElasticSearch based on query", response = Response.class)
    @RequestMapping(value="api/messagesearch/1.0/search",method = RequestMethod.POST)
    @SuppressWarnings("unused")
    public Response queryElastic(@RequestHeader("requestuuid") String uuId, @RequestBody Query query){
        log.info("Message Search - API call");
        log.info("Incoming UUid is " + uuId);
        query.setUUid(uuId);
        ResponseEntity<String> entity = elasticSearchService.getQuery(query,true);
        Response response = elasticSearchService.getResponse(entity,false,false);
        return response;
    }

    @ApiOperation(value = "Zoom in on message based on id", response = Response.class)
    @RequestMapping(value="api/messagesearch/1.0/details",method = RequestMethod.POST)
    @SuppressWarnings("unused")
    public Response messageZoom(@RequestBody Query query){
        log.info("Message Zoom - API call");
        ResponseEntity<String> entity = elasticSearchService.messageZoom(query);
        return elasticSearchService.getResponse(entity,true,false);
    }

    @ApiOperation(value = "Zoom in on sessions based on time offset", response = SessionResponse.class)
    @RequestMapping(value="api/messagesearch/1.0/sessionzoom",method = RequestMethod.POST)
    @SuppressWarnings("unused")
    public SessionResponse sessionZoom(@RequestBody SessionRequest request){
        log.info("Session Zoom - API call");
        SessionResponse response = elasticSearchService.sessionZoom(request);
        return response;
    }

    @ApiOperation(value = "Retrieve logs from ElasticSearch", response = Response.class)
    @RequestMapping(value="api/messagesearch/1.0/logs",method = RequestMethod.POST)
    @SuppressWarnings("unused")
    public Response queryLogs(@RequestBody LogSearch search){
        log.info("Retrieving Logs - API call");
        ResponseEntity<String> entity = elasticSearchService.getLog(search);
        return elasticSearchService.getResponse(entity,false,true);
    }

    @ApiOperation(value = "Check status of application", response = ResponseEntity.class)
    @SuppressWarnings("unused")
    @RequestMapping(value="api/messagesearch/1.0/status",method = RequestMethod.GET)
    public ResponseEntity<String> statusCheck() {

        return new ResponseEntity<>("", HttpStatus.OK);
    }

}

