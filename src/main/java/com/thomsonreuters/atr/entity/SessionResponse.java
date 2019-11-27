package com.thomsonreuters.atr.entity;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;

import java.util.List;


//Response Object for SessionZoom

public class SessionResponse {
    private String timeTook;

    public SessionResponse(String timeTook, String hitsCount, List<JsonNode> hits) {
        this.timeTook = timeTook;
        this.hitsCount = hitsCount;
        this.hits = hits;
    }

    public String getTimeTook() {
        return timeTook;
    }

    public void setTimeTook(String timeTook) {
        this.timeTook = timeTook;
    }

    public String getHitsCount() {
        return hitsCount;
    }

    public void setHitsCount(String hitsCount) {
        this.hitsCount = hitsCount;
    }

    public List<JsonNode> getHits() {
        return hits;
    }

    public void setHits(List<JsonNode> hits) {
        this.hits = hits;
    }

    private String hitsCount;
    private List<JsonNode> hits;

    public void addToHits(List<JsonNode> toAdd){
        this.hits.addAll(toAdd);
    }

}
