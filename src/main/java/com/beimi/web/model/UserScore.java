package com.beimi.web.model;

import java.io.Serializable;

/**
 * Created by zhengchenglei on 2018/5/16.
 */
public class UserScore implements Serializable {

    private String userId;

    private int score;

    private int peng;

    private int gang;

    private int dianpao;

    private int hu;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPeng() {
        return peng;
    }

    public void setPeng(int peng) {
        this.peng = peng;
    }

    public int getGang() {
        return gang;
    }

    public void setGang(int gang) {
        this.gang = gang;
    }

    public int getDianpao() {
        return dianpao;
    }

    public void setDianpao(int dianpao) {
        this.dianpao = dianpao;
    }

    public int getHu() {
        return hu;
    }

    public void setHu(int hu) {
        this.hu = hu;
    }
}
