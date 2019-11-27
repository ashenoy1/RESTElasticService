package com.thomsonreuters.atr.entity;
import lombok.Data;

@Data
public class FixTag {

    //Java Bean that gets populated from PostGres

    private String tag;
    private String tagname;
    private String datatype;

    public FixTag(String tag, String tagname, String datatype) {
        this.tag = tag;
        this.tagname = tagname;
        this.datatype = datatype;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTagname() {
        return tagname;
    }

    public void setTagname(String tagname) {
        this.tagname = tagname;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
