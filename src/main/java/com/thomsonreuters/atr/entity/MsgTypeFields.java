package com.thomsonreuters.atr.entity;


//Bean populated through Database


public class MsgTypeFields {

    public MsgTypeFields(String msgtype, String tag) {
        this.msgtype = msgtype;
        this.tag = tag;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private String msgtype;
    private String tag;
}
