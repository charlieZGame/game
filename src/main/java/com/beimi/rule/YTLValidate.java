package com.beimi.rule;


import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class YTLValidate  extends AbsCheckScoreRule{


    @Override
    public boolean isSatisfy() {

        if(CollectionUtils.isNotEmpty(actions)){
            return false;
        }
        if (CollectionUtils.isEmpty(collections) || (collections.size() - 2) % 3 != 0 || collections.size() != 14) {
            return false;
        }


        List<Byte> hun = new ArrayList<Byte>();
        for(Byte b : collections){
            for(byte _b : powerful){
                if(b == _b){
                    hun.add(b);
                }
            }
        }

        collections.remove(0);
        collections.remove(1);
        collections.removeAll(hun);

        for(int i = 0;i < collections.size()-1 ;i ++){
            if(Math.abs(collections.get(i)-collections.get(i+1)) == 0){
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

    public byte[] getPowerful() {
        return powerful;
    }

    public void setPowerful(byte[] powerful) {
        this.powerful = powerful;
    }
}
