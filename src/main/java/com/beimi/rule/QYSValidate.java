package com.beimi.rule;

import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class QYSValidate extends AbsCheckScoreRule {


    @Override
    public boolean isSatisfy() {

        if(CollectionUtils.isEmpty(collections)  || collections.size() < 2 ){
            return false;
        }

        List<Byte> hun = new ArrayList<Byte>();
        if(powerful != null && powerful.length > 0){
            for(Byte b : collections){
                for(byte _b : powerful){
                    if(b/4 == _b/4){
                        hun.add(b);
                    }
                }
            }
        }


        collections.removeAll(hun);
        int flag;
        int se;
        if(CollectionUtils.isNotEmpty(collections)) {
            flag = (collections.get(0) < 0 ? -1 : 1);
            se = collections.get(0) / 36;
        }else if(CollectionUtils.isNotEmpty(actions)){
            flag = (actions.get(0).getCard() < 0 ? -1 : 1);
            se = actions.get(0).getCard() / 36;
        }else{
            return false;
        }

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

        QYSValidate qxdValidate = new QYSValidate();

        Byte[] stringArray = new Byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,14};
        List<Byte> collections = Arrays.asList(stringArray);
        qxdValidate.collections = collections;
        System.out.println(qxdValidate.isSatisfy());

    }
}
