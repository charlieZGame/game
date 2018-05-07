package com.beimi.rule;

import com.beimi.util.GameUtils;
import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/5/6.
 */
public class KDJValidate  {


    private List<Byte> collections;

    private List<Action> actions;

    private List<Byte> coverCards;

    private byte[] powerful;


    public KDJValidate() {
    }

    public KDJValidate(List<Byte> collections, List<Action> actions) {
        this.collections = collections;
        this.actions = actions;
    }


    public int isSatisfy() {

        int innerSize = 0;
        if(CollectionUtils.isEmpty(coverCards)){
            return 0;
        }

        Map<Integer,Integer> kou = new HashMap<Integer,Integer>();

        // 中发白算不算大将
        for(Byte b : coverCards){
            if(b >= 0){
                continue;
            }
            if(kou.containsKey(b/4)){
                kou.put(b/4,kou.get(b/4)+1);
            }else{
                kou.put(b/4,1);
            }
        }

        if(kou.isEmpty()||kou.size() == 0){
            return 0;
        }

        for(Map.Entry<Integer,Integer> entry : kou.entrySet()){
            if(entry.getValue() > 1){
                continue;
            }
            innerSize ++ ;
        }

        return innerSize;
    }

    public void setData(List<Byte> collections, List<Action> actions, byte[] powerful,List<Byte> coverCards) {
        this.collections = GameUtils.cloneList(collections);
        this.actions = actions;
        this.powerful = powerful;
        this.coverCards = GameUtils.cloneList(coverCards);

    }


}
