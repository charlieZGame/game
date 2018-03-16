package com.beimi.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
public class OutRoom {

    private String applyUserId;

    private Map<String,Boolean> volate = new HashMap<String,Boolean>();

    public String getApplyUserId() {
        return applyUserId;
    }

    public OutRoom setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
        return this;
    }

    public Map<String, Boolean> getVolate() {
        return volate;
    }

    public void setVolate(Map<String, Boolean> volate) {
        this.volate = volate;
    }

}
