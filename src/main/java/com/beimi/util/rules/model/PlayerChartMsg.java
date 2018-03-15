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

    private T msgType;

    private String message;


    public PlayerChartMsg() {
    }

    public PlayerChartMsg(String srcUserId, String srcUserName, String desUserId, String desUserName, T msg,String message) {
        this.srcUserId = srcUserId;
        this.srcUserName = srcUserName;
        this.desUserId = desUserId;
        this.desUserName = desUserName;
        this.msgType = msgType;
        this.message = message;
    }



    public String getSrcUserId() {
        return srcUserId;
    }

    public void setSrcUserId(String srcUserId) {
        this.srcUserId = srcUserId;
    }

    public String getDesUserId() {
        return desUserId;
    }

    public void setDesUserId(String desUserId) {
        this.desUserId = desUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getMsgType() {
        return msgType;
    }

    public void setMsgType(T msgType) {
        this.msgType = msgType;
    }

    public String getSrcUserName() {
        return srcUserName;
    }

    public void setSrcUserName(String srcUserName) {
        this.srcUserName = srcUserName;
    }

    public String getDesUserName() {
        return desUserName;
    }

    public void setDesUserName(String desUserName) {
        this.desUserName = desUserName;
    }
}
