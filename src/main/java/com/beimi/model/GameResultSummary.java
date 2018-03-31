package com.beimi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/3/28.
 */
public class GameResultSummary implements Serializable {


    private List<Byte> pairs;

    private List<Byte> three;

    private List<Byte> gangs;

    private List<Byte> pengs;


    public List<Byte> getPairs() {
        if(this.pairs == null){
            this.pairs = new ArrayList<Byte>();
        }
        return pairs;
    }

    public void setPairs(List<Byte> pairs) {
        this.pairs = pairs;
    }

    public List<Byte> getThree() {
        if(this.three == null){
            this.three = new ArrayList<Byte>();
        }
        return three;
    }

    public void setThree(List<Byte> three) {
        this.three = three;
    }

    public List<Byte> getGangs() {
        if(this.gangs == null){
            this.gangs = new ArrayList<Byte>();
        }
        return gangs;
    }

    public void setGangs(List<Byte> gangs) {
        this.gangs = gangs;
    }

    public List<Byte> getPengs() {
        if(this.pengs == null){
            this.pengs = new ArrayList<Byte>();
        }
        return pengs;
    }

    public void setPengs(List<Byte> pengs) {
        this.pengs = pengs;
    }

}
