package com.beimi.rule;

import java.io.Serializable;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class CheckResult implements Serializable {

    private HuType huType;

    private boolean isWin;

    private String userId;

    private String targetUserId;

    private Integer peng;

    private Integer anGang;

    private Integer mGang;

    private Integer score;

    public HuType getHuType() {
        return huType;
    }

    public void setHuType(HuType huType) {
        this.huType = huType;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getPeng() {
        return peng;
    }

    public void setPeng(Integer peng) {
        this.peng = peng;
    }

    public Integer getAnGang() {
        return anGang;
    }

    public void setAnGang(Integer anGang) {
        this.anGang = anGang;
    }

    public Integer getmGang() {
        return mGang;
    }

    public void setmGang(Integer mGang) {
        this.mGang = mGang;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}
