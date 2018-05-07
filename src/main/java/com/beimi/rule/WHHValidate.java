package com.beimi.rule;


import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class WHHValidate extends AbsCheckScoreRule{


    @Override
    public boolean isSatisfy() {

        if (CollectionUtils.isEmpty(collections) || collections.size() < 2) {
            return false;
        }

        if (collections.get(0) / 4 != collections.get(1) / 4) {
            return false;
        }

        if (collections.size() == 2) {
            if(Math.abs(collections.get(0)/4 - collections.get(1)/4) != 0){
                return false;
            }
            return true;
        }

        for (int i = 2; i < collections.size()-2; i = i + 3) {
            int faway1 = Math.abs(collections.get(i)/4 - collections.get(i+1)/4);
            int faway2 = Math.abs(collections.get(i+1)/4 - collections.get(i+2)/4);
            if(faway1 != faway2){
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
}
