package com.beimi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/3/28.
 */
public class GameResultCheck implements Serializable {


    private List<Byte> pairs;

    private List<Byte> kezis;

    private List<Byte> pengs;

    private List<Byte> gangs;

    private List<Byte> straight;

    private List<Byte> others;

    private List<Byte> huns;



    public List<Byte> getPairs() {
        return pairs;
    }

    public void setPairs(List<Byte> pairs) {
        this.pairs = pairs;
    }

    public List<Byte> getKezis() {
        return kezis;
    }

    public void setKezis(List<Byte> kezis) {
        this.kezis = kezis;
    }

    public List<Byte> getStraight() {
        return straight;
    }

    public void setStraight(List<Byte> straight) {
        this.straight = straight;
    }

    public List<Byte> getOthers() {
        return others;
    }

    public void setOthers(List<Byte> others) {
        this.others = others;
    }

    public List<Byte> getHuns() {
        return huns;
    }

    public void setHuns(List<Byte> huns) {
        this.huns = huns;
    }

    public synchronized List<Byte> getPengs() {
        if(this.pengs == null){
            pengs = new ArrayList<Byte>();
        }
        return pengs;
    }

    public void setPengs(List<Byte> pengs) {
        this.pengs = pengs;
    }

    public List<Byte> getGangs() {
        return gangs;
    }

    public void setGangs(List<Byte> gangs) {
        this.gangs = gangs;
    }
}
