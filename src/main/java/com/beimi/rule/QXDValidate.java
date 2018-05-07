package com.beimi.rule;

import com.beimi.util.rules.model.Action;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Created by zhengchenglei on 2018/4/25.
 */
public class QXDValidate  extends AbsCheckScoreRule{


    @Override
    public boolean isSatisfy() {

       /* if (CollectionUtils.isNotEmpty(actions)) {
            return false;
        }*/

        List<Byte> tempb = new ArrayList<Byte>();

        Map<Integer, Integer> hunMap = new HashMap<Integer, Integer>();
        if(powerful != null) {
            for (Byte temp : powerful) {
                hunMap.put(temp / 4, 0);
            }
        }
        int hunSize = 0;
        for (Byte b : collections) {
            if (hunMap.containsKey(b / 4)) {
                hunSize++;
            } else {
                tempb.add(b);
            }
        }

        Collections.sort(tempb);
        List<Byte> pair = new ArrayList<Byte>();
        for (int i = 0; i < tempb.size()-1; i++) {
            if (tempb.get(i) / 4 == tempb.get(i + 1) / 4) {
                pair.add(tempb.get(i));
                pair.add(tempb.get(i + 1));
                i = i + 1;
            }
        }
        if (hunSize >= (tempb.size()-pair.size())) {
            return true;
        } else {
            return false;
        }
    }




    @Override
    public CardType getHuName() {
        return null;
    }

    @Override
    public Integer getHuScore() {
        return null;
    }

}
