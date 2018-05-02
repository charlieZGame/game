package com.beimi.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/4/17.
 */
public class UserCardsUtil {

    private static Map<String,List<Byte>> cards = new HashMap<String,List<Byte>>();



    public static void clearHistoryCards(){
        cards.clear();
    }


    public static Map<String,List<Byte>> getCardsInfo(){
        if(cards.isEmpty()){
            return cards;
        }
        Map<String,List<Byte>> tmpCards = new HashMap<String,List<Byte>>();
        for(Map.Entry<String,List<Byte>> entry : cards.entrySet()){
            tmpCards.put(entry.getKey(),entry.getValue());
        }
        return tmpCards;
    }

    public static void putData(String userId,String datastr){

        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(datastr)){
            return;
        }
        String[] data = datastr.split(",");
        if(StringUtils.isEmpty(userId) || data == null || data.length == 0){
            return;
        }
        List<Byte> cardArray = new ArrayList<Byte>();
        for(String str : data){
            cardArray.add(Byte.parseByte(str));
        }
        cards.put(userId,cardArray);
    }

    public static String getCards(){
        return JSONObject.toJSONString(cards);
    }
    public static  Map<String,List<Byte>> getCardsData(){
        return cards;
    }

}
