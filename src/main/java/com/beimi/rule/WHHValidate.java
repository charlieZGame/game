package com.beimi.rule;


import com.beimi.core.BMDataContext;
import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
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
            /*if(Math.abs(collections.get(0)/4 - collections.get(1)/4) != 0){
                return false;
            }*/
            return true;
        }

        boolean isHF = true;
        //校验三个连载一起的
        for (int i = 2; i < collections.size()-2; i = i + 3) {
            int faway1 = Math.abs(collections.get(i)/4 - collections.get(i+1)/4);
            int faway2 = Math.abs(collections.get(i+1)/4 - collections.get(i+2)/4);
            if(faway1 != faway2){
                isHF = false;
            }
        }
        if(isHF){
            return true;
        }
        //校验七小对
        if(CollectionUtils.isNotEmpty(actions)) {
            for (Action action : actions){
                if(BMDataContext.PlayerAction.GANG.toString().equals(action.getAction())){
                    continue;
                }
                collections.add((byte)(action.getCard()/ 4 * 4));
                if(action.getCard() < 0){
                    collections.add((byte)(action.getCard()/ 4 * 4 - 1));
                    collections.add((byte) (action.getCard() / 4 * 4 - 2));
                }else {
                    collections.add((byte) (action.getCard() / 4 * 4 + 1));
                    collections.add((byte) (action.getCard() / 4 * 4 + 2));
                }
            }
        }
        if(collections.size() % 2 !=0){
            return false;
        }
        Collections.sort(collections);
        for (int i = 2; i < collections.size()-1; i = i + 2) {
            if(Math.abs(collections.get(i)/4 - collections.get(i+1)/4) != 0){
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


    public static void main(String[] args) {

        WHHValidate qxdValidate = new WHHValidate();

        Byte[] stringArray = new Byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,14};
        List<Byte> collections = Arrays.asList(stringArray);
        qxdValidate.collections = collections;
        System.out.println(qxdValidate.isSatisfy());

    }
}
