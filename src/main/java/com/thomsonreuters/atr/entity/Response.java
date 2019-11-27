package com.thomsonreuters.atr.entity;

import com.fasterxml.jackson.databind.JsonNode;



import java.util.List;

public class Response {

    private String timeTook;
    private String hitsCount;
    private List<JsonNode> hits;

    public Response(String took, String hitsCount, List<JsonNode> hits){
        this.timeTook = took;
        this.hitsCount = hitsCount;
        this.hits = hits;
    }

    public String getTimeTook() {
        return timeTook;
    }

    public String getHitsCount() {
        return hitsCount;
    }

    public void setHitsCount(String hitCount) {
        this.hitsCount = hitCount;
    }

    public void setTimeTook(String timeTook) {
        this.timeTook = timeTook;
    }

    public List<JsonNode> getHits() {
        return hits;
    }

    public void setHits(List<JsonNode> hits) {
        this.hits = hits;
    }
}
