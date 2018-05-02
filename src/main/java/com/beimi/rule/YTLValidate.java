package com.beimi.rule;


import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class YTLValidate implements ICheckScoreRule {

    private List<Byte> collections;

    private List<Action> actions;

    private byte[] powerfull;


    public YTLValidate() {
    }

    public YTLValidate(List<Byte> collections, List<Action> actions,byte[] powerfull) {
        this.collections = collections;
        this.actions = actions;
    }

    @Override
    public boolean isSatisfy() {

        if(CollectionUtils.isNotEmpty(actions)){
            return false;
        }
        if (CollectionUtils.isEmpty(collections) || (collections.size() - 2) % 3 != 0 || collections.size() != 14) {
            return false;
        }
        for(int i = 2;i < collections.size()-2 ;i = i+3){
            if(Math.abs(collections.get(i)-collections.get(i+1)) != 1 || Math.abs(collections.get(i+1)-collections.get(i+2))!= 1){
                return false;
            }
        }

        return true;
    }

    @Override
    public CardType getHuName() {
        return null;
    }

    @Override
    public Integer getHuScore() {
        return null;
    }

    @Override
    public void setData(List<Byte> collections, List<Action> actions, byte[] powerful) {
        this.collections = collections;
        this.actions = actions;
        this.powerfull = powerful;
    }

    public List<Byte> getCollections() {
        return collections;
    }

    public void setCollections(List<Byte> collections) {
        this.collections = collections;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public byte[] getPowerfull() {
        return powerfull;
    }

    public void setPowerfull(byte[] powerfull) {
        this.powerfull = powerfull;
    }
}
