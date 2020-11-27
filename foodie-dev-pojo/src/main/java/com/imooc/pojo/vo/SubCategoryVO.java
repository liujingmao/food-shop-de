package com.imooc.pojo.vo;

/**
 * Created by liujingmao on 2019-11-23.
 */
public class SubCategoryVO {

    private Integer subId;
    private String subName;
    private String SubType;
    private Integer subFatherId;

    public Integer getSubId() {
        return subId;
    }

    public void setSubId(Integer subId) {
        this.subId = subId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getSubType() {
        return SubType;
    }

    public void setSubType(String subType) {
        SubType = subType;
    }

    public Integer getSubFatherId() {
        return subFatherId;
    }

    public void setSubFatherId(Integer subFatherId) {
        this.subFatherId = subFatherId;
    }

/* c.id as subid,
    c.`name` as subName,
    c.type as subType,
    c.father_id as subFatherId*/


}
