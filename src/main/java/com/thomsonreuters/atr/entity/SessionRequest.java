package com.thomsonreuters.atr.entity;
import com.fasterxml.jackson.annotation.JsonProperty;



//Request Payload for the API
public class SessionRequest {

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("msgType")
    private String msgtype;

    @JsonProperty("offset")
    private int offset;

    @JsonProperty("timeOffset")
    private int timeOffset;

    private String buySideSession;

    public String getBuySideSession() {
        return buySideSession;
    }

    public void setBuySideSession(String buySideSession) {
        this.buySideSession = buySideSession;
    }

    public String getSellSideSession() {
        return sellSideSession;
    }

    public void setSellSideSession(String sellSideSession) {
        this.sellSideSession = sellSideSession;
    }

    private String sellSideSession;

    @JsonProperty("timestamp")
    private String timestamp;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

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

}
