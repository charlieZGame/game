package com.beimi.backManager;

/**
 * Created by zhengchenglei on 2018/4/7.
 */
public class Session {

    private String openId;

    private long timestemp = System.currentTimeMillis();


    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public long getTimestemp() {
        return timestemp;
    }

    public Session setTimestemp(long timestemp) {
        this.timestemp = timestemp;
        return this;
    }
}
