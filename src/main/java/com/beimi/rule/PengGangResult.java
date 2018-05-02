package com.beimi.rule;

import java.io.Serializable;

/**
 * Created by zhengchenglei on 2018/4/17.
 */
public class PengGangResult implements Serializable {

    private int score;

    private String desc;


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
