package com.thomsonreuters.atr.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


//Each bean getting stored in {in:[],out:[]}

public class SessionBean {

    public String getClordid() {
        return clordid;
    }

    public void setClordid(String clordid) {
        this.clordid = clordid;
    }

    public String getDirection() {
        return direction;
    }


    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setFieldsForSession(boolean isFromBuyer, String buySide, String sellSide){
        if(isFromBuyer){
            setSendingsession(buySide);
            setReceivingsession(sellSide);
        }
        else{
            setSendingsession(sellSide);
            setReceivingsession(buySide);
        }
        setId();
        setMsgType();
        setQty();
        setOrdStatusWord();
    }

    public String getId() {
        return id;
    }

    public void setId() {
        this.id = this._id;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType() {
        this.msgType = FixConstants.msgTypeMap.get(this.msgtype);
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty() {
        this.qty = this.orderqty;
    }



    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = FixConstants.sideMap.get(side);
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String direction;
    private String id;
    private String msgType;
    private String qty;

    @JsonProperty("ordtype")
    @JsonIgnore
    private String ordtype;

    public String getSendercompid() {
        return sendercompid;
    }

    public void setSendercompid(String sendercompid) {
        this.sendercompid = sendercompid;
    }

    @JsonProperty("sendercompid")
    @JsonIgnore
    private String sendercompid;

    @JsonProperty("delivertocompid")
    @JsonIgnore
    private String delivertocompid;

    @JsonProperty("msg")
    @JsonIgnore
    private String msg;

    public String getTargetcompid() {
        return targetcompid;
    }

    public void setTargetcompid(String targetcompid) {
        this.targetcompid = targetcompid;
    }

    @JsonProperty("targetcompid")
    @JsonIgnore
    private String targetcompid;

    @JsonProperty("transactTime")
    @JsonIgnore
    private String transactTime;

    @JsonProperty("sendingtime")
    @JsonIgnore
    private String sendingtime;

    @JsonProperty("clientid")
    @JsonIgnore
    private String clientid;

    @JsonProperty("onbehalfofcompid")
    @JsonIgnore
    private String onbehalfofcompid;


    @JsonProperty("clordid")
    private String clordid;


    public void setMsgdir(String msgdir) {
        this.msgdir = msgdir;
    }

    public String getMsgdir() {
        return msgdir;
    }

    @JsonIgnore
    @JsonProperty("msgdir")
    private String msgdir;

    public void set_id(String _id) {
        this._id = _id;
    }

    @JsonProperty("_id")
    @JsonIgnore
    private String _id;

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    @JsonProperty("msgtype")
    @JsonIgnore
    private String msgtype;




    @JsonProperty("price")
    private String price;




    public void setOrderqty(String orderqty) {
        this.orderqty = orderqty;
    }

    @JsonIgnore
    @JsonProperty("orderqty")
    private String orderqty;

    @JsonProperty("side")
    private String side;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("ordstatus")
    @JsonIgnore
    private String ordstatus;


    public void setordstatus(String ordstatus) {
        this.ordstatus = ordstatus;
    }

    public String getOrdStatusWord() {
        return ordStatusWord;
    }

    public void setOrdStatusWord() {
        if(this.ordstatus != null)
            this.ordStatusWord = this.ordstatus;

        else if(this.msgtype != null){
            if (this.msgtype.equals("D")){
                this.ordStatusWord = FixConstants.ordStatMap.get("A");
            }
            else if(this.msgtype.equals("8")){
                this.ordStatusWord = FixConstants.ordStatMap.get("0");
            }
            else {
                this.ordStatusWord = null;
            }
        }
        else
            this.ordStatusWord = null;
    }
    private String ordStatusWord;

    public String getReceivingsession() {
        return receivingsession;
    }

    public void setReceivingsession(String receivingsession) {
        this.receivingsession = receivingsession;
    }

    public String getSendingsession() {
        return sendingsession;
    }

    public void setSendingsession(String sendingsession) {
        this.sendingsession = sendingsession;
    }

    private String receivingsession;
    private String sendingsession;

}
