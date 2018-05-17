package com.beimi.rule;

import com.beimi.core.BMDataContext;
import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class HYSValidate extends AbsCheckScoreRule {

    @Override
    public boolean isSatisfy() {

        if(CollectionUtils.isEmpty(collections) || collections.size() < 2){
            return false;
        }

        Map<Integer,Integer> hunSe = new HashMap<Integer,Integer>();
        for(Byte b : collections) {
            if (b < 0) {
                hunSe.put(-1, null);
            } else {
                hunSe.put(b / 36, null);
            }
        }
        if(CollectionUtils.isNotEmpty(actions)){
            for(Action action : actions){
                if (action.getCard() < 0) {
                    hunSe.put(-1, null);
                } else {
                    hunSe.put(action.getCard() / 36, null);
                }
            }
        }

        if(hunSe.size() != 2){
            return false;
        }

        for(Map.Entry<Integer,Integer> entry : hunSe.entrySet()){
            if(entry.getKey() < 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public CardType getHuName() {
        return CardType.QYS;
    }

    @Override
    public Integer getHuScore() {
        return 1;
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


    public static void main(String[] args) {

        Byte[] bytes = new Byte[]{52,53,39,43,46,50,54,58,61,66,71};
        List<Byte> list = new ArrayList<Byte>();
        for(Byte b : bytes){
            list.add(b);
        }

        List<Action> actions = new ArrayList<Action>();
        Action action = new Action("",BMDataContext.PlayerAction.PENG.toString(),(byte)-26);
        actions.add(action);
        HYSValidate hysValidate = new HYSValidate();
        hysValidate.collections = list;
        hysValidate.actions = actions;

        System.out.println(hysValidate.isSatisfy());

    }

}
