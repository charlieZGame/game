package com.beimi.rule;

import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class QYSValidate implements ICheckScoreRule {

    private List<Byte> collections;

    private List<Action> actions;

    private byte[] powerful;


    public QYSValidate() {
    }

    public QYSValidate(List<Byte> collections, List<Action> actions) {
        this.collections = collections;
        this.actions = actions;
    }

    @Override
    public boolean isSatisfy() {

        if(CollectionUtils.isEmpty(collections)  || collections.size() < 2 ){
            return false;
        }
        int flag = (collections.get(0) < 0 ? -1 : 1);
        int se = collections.get(0)/36;

        for(Byte b : collections){
            if(flag * b < 0){
                return false;
            }
            if(se != (b/36)){
                return false;
            }
        }
        if(CollectionUtils.isEmpty(actions)){
            return true;
        }
        for(Action action : actions){
            if(flag * action.getCard() < 0){
                return false;
            }

            if(se != action.getCard()/36){
                return false;
            }
        }
        return true;
    }

    @Override
    public CardType getHuName() {
        return CardType.QYS;
    }

    @Override
    public Integer getHuScore() {
        return 1;
    }


    @Override
    public void setData(List<Byte> collections, List<Action> actions, byte[] powerful) {
        this.collections = collections;
        this.actions = actions;
        this.powerful = powerful;
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
