package com.beimi.model;

import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/3/28.
 */
public class GameResultSummary implements Serializable {


    private List<Byte> pairs;

    private List<Byte> three;

    private List<Byte> gangs;

    private List<Byte> pengs;


    public String getPairs() {
        if(CollectionUtils.isEmpty(this.pairs)){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Byte b : this.pairs) {
            sb.append(",").append(b);
        }
        return sb.substring(1);
    }


    public void setPairs(List<Byte> pairs) {
        this.pairs = pairs;
    }

    public String getThree() {
        if(CollectionUtils.isEmpty(this.three)){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Byte b : this.three) {
            sb.append(",").append(b);
        }
        return sb.substring(1);
    }

    public void setThree(List<Byte> three) {
        this.three = three;
    }

    public String getGangs() {
        if(CollectionUtils.isEmpty(this.gangs)){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Byte b : this.gangs) {
            sb.append(",").append(b);
        }
        return sb.substring(1);
    }


    public void setGangs(List<Byte> gangs) {
        this.gangs = gangs;
    }

    public String getPengs() {
        if(CollectionUtils.isEmpty(this.pengs)){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Byte b : this.pengs) {
            sb.append(",").append(b);
        }
        return sb.substring(1);
    }

    public void setPengs(List<Byte> pengs) {
        this.pengs = pengs;
    }

}
