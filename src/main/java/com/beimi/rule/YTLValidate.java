package com.beimi.rule;


import com.beimi.util.GameUtils;
import com.beimi.util.GameWinCheck;
import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

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
        if(powerful != null && powerful.length > 0) {
            for (Byte b : collections) {
                for (byte _b : powerful) {
                    if (b == _b) {
                        hun.add(b);
                    }
                }
            }
        }

        collections.remove(0);
        collections.remove(0);
        collections.removeAll(hun);


       // Collections.sort(collections);
        List<Byte> bytes = GameWinCheck.cloneList(collections);
        Map<Integer, List<Byte>> pairMap = GameWinCheck.findPair(bytes);
        if(pairMap.size() >= 1){
            return false;
        }/*else{
            for(Map.Entry<Integer,List<Byte>> entry : pairMap.entrySet()){
                if(entry.getValue().size() > 2){
                    return false;
                }else{
                    collections.removeAll(entry.getValue());
                }
            }
        }*/

        // 对子在上边处理了，先把不可能在有对子了
        //Collections.sort(collections);

        Map<Integer,List<Byte>> map = new HashMap<Integer,List<Byte>>();
        for(Byte b : collections){
            int key = 0;
            if(b < 0){
                key = -1;
            }else{
                key = b/36;
            }
            if(map.containsKey(key)){
                map.get(key).add(b);
            }else{
                List<Byte> list = new ArrayList<Byte>();
                list.add(b);
                map.put(key,list);
            }
        }

        if((map.get(-1) != null && map.get(-1).size() > 2) || map.size() > 2){
            return false;
        }
        List<Byte> masterCards = null;
        List<Byte> slaveKeyCards = null;
        Integer size = 0;
        for(Map.Entry<Integer,List<Byte>> entry : map.entrySet()){
            if(size < entry.getValue().size()){
                size = entry.getValue().size();
                masterCards = entry.getValue();
            }else{
                slaveKeyCards = entry.getValue();
            }
        }

   /*     if(size + hun.size() != 9){
            return false;
        }*/

        if(CollectionUtils.isEmpty(masterCards)){
            return false;
        }


        int hunSize = hun.size();
        Collections.sort(masterCards);
        for(int i = 0;i < masterCards.size() - 1 ;i ++) {
            if (Math.abs(masterCards.get(i) / 4 - masterCards.get(i + 1) / 4) == 2) {
                hunSize = hunSize - 1;
            } else if (Math.abs(masterCards.get(i) / 4 - masterCards.get(i + 1) / 4) > 2) {
                hunSize = hunSize - 2;
            }
            if(hunSize < 0) {
                return false;
            }
        }


        if(CollectionUtils.isNotEmpty(slaveKeyCards)) {
            Collections.sort(slaveKeyCards);
            for (int i = 0; i < slaveKeyCards.size() - 1; i++) {
                if (Math.abs(slaveKeyCards.get(i) / 4 - slaveKeyCards.get(i + 1) / 4) == 2) {
                    hunSize = hunSize - 1;
                } else if (Math.abs(slaveKeyCards.get(i) / 4 - slaveKeyCards.get(i + 1) / 4) > 2) {
                    hunSize = hunSize - 2;
                }
                if(hunSize < 0) {
                    return false;
                }
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


    public static void main(String[] args) {

        YTLValidate ytlValidate = new YTLValidate();

        Byte[] stringArray = new Byte[]{16,17,1,2,3,4,5,6,8,9,10,12,13,14};
        /*ytlValidate.powerful = new byte[3];
        ytlValidate.powerful[0] = 68;
        ytlValidate.powerful[1] = 36;
        ytlValidate.powerful[2] = 40;*/

        List<Byte> collections = new ArrayList<Byte>();
        for(Byte b : stringArray){
            collections.add(b);
        }
        ytlValidate.collections = collections;
        System.out.println(ytlValidate.isSatisfy());
        Object obj = ytlValidate.collections;

    }
}
