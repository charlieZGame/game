package com.beimi.backManager;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhengchenglei on 2018/4/6.
 */
public class StandardResponse<T> {

    private Integer returnCode;

    private String msg;

    private T data;

    public StandardResponse(Integer returnCode, String msg, T data) {
        this.returnCode = returnCode;
        this.msg = msg;
        this.data = data;
    }

    public StandardResponse() {
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toJSON(){
        return JSONObject.toJSONString(this);
    }

}
