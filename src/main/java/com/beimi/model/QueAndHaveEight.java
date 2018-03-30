package com.beimi.model;

import java.io.Serializable;

/**
 * Created by zhengchenglei on 2018/3/28.
 */
public class QueAndHaveEight implements Serializable{

    private boolean que;

    private boolean isHaveEight;


    public boolean isQue() {
        return que;
    }

    public void setQue(boolean que) {
        this.que = que;
    }

    public boolean isHaveEight() {
        return isHaveEight;
    }

    public void setHaveEight(boolean haveEight) {
        isHaveEight = haveEight;
    }

    public boolean isQueHaveEight(){
        return que && isHaveEight;
    }

}
