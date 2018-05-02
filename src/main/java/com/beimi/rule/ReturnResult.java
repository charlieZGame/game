package com.beimi.rule;

import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class ReturnResult {

    private String userId;

    private String desc = "";

    private Integer score = 0;

    private boolean isWin;

    private List<Byte> collections;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<Byte> getCollections() {
        return collections;
    }

    public void setCollections(List<Byte> collections) {
        this.collections = collections;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    @Override
    public String toString() {
        return "ReturnResult{" +
                "userId='" + userId + '\'' +
                ", desc='" + desc + '\'' +
                ", score=" + score +
                ", isWin=" + isWin +
                ", collections=" + collections +
                '}';
    }
}

