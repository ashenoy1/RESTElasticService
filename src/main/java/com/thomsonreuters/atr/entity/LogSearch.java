package com.thomsonreuters.atr.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


//Java Bean for incoming LogSearch query

public class LogSearch {
    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> applyTimeOffset(){
        Instant fromDate = Instant.parse(this.timestamp).minusSeconds(timeOffset);
        Instant toDate = Instant.parse(this.timestamp).plusSeconds(timeOffset);
        List<String> list = new ArrayList<>();
        list.add(fromDate.toString());
        list.add(toDate.toString());
        return list;
    }

    private int timeOffset;
    private String timestamp;
}
