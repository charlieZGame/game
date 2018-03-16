package com.beimi.util.rules.model;

import java.io.Serializable;

/**
 * Created by zcl on 2018/3/14.
 */
public class PlayerChartMsg<T> implements Serializable {


    private String srcUserId;

    private String srcUserName;

    private String desUserId;

    private String desUserName;

    private String type;

    private T msg;


    public PlayerChartMsg() {
    }

    public PlayerChartMsg(String srcUserId, String srcUserName, String desUserId, String desUserName,String returnCode, T msg) {

        this.srcUserId = srcUserId;
        this.srcUserName = srcUserName;
        this.desUserId = desUserId;
        this.desUserName = desUserName;
        this.type = type;
        this.msg = msg;
    }


    public String getSrcUserId() {
        return srcUserId;
    }

    public void setSrcUserId(String srcUserId) {
        this.srcUserId = srcUserId;
    }

    public String getSrcUserName() {
        return srcUserName;
    }

    public void setSrcUserName(String srcUserName) {
        this.srcUserName = srcUserName;
    }

    public String getDesUserId() {
        return desUserId;
    }

    public void setDesUserId(String desUserId) {
        this.desUserId = desUserId;
    }

    public String getDesUserName() {
        return desUserName;
    }

    public void setDesUserName(String desUserName) {
        this.desUserName = desUserName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getMsg() {
        return msg;
    }

    public void setMsg(T msg) {
        this.msg = msg;
    }
}
