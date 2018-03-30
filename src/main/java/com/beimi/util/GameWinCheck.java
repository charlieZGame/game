package com.beimi.util;


import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Created by zhengchenglei on 2018/3/29.
 */
public class GameWinCheck {


    /**
     *
     * @param cards
     * @param huns
     * @return
     */
    public static boolean pairWinAlgorithm(Map<Integer,List<Byte>> cards,List<Byte>huns,List<Byte> collectionCards) {

        // 先从混子里边找对子，应为这个一个共同的过程
        if (huns.size() >= 2) {
            List<Byte> hunsTemp = cloneList(huns);
            collectionCards.add(hunsTemp.remove(0));
            collectionCards.add(hunsTemp.remove(1));
            boolean isHu = true;
            for (Map.Entry<Integer, List<Byte>> entry : cards.entrySet()) {
                if (!sameCardValidateHu(cloneList(entry.getValue()), collectionCards, hunsTemp)) {
                    isHu = false;
                    break;
                }
            }
            if(isHu) {
                return true;
            }
        }
        for (Map.Entry<Integer, List<Byte>> entry : cards.entrySet()) {
            if (sameSeValidateHu(entry.getValue(),entry.getKey(), cards, huns, collectionCards)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 校验一盘，按色校验
     * @param cards
     * @param cardsMap
     * @param huns
     * @param collectionCards
     * @return
     */
    private static boolean sameSeValidateHu(List<Byte>cards,int se,Map<Integer,List<Byte>> cardsMap,List<Byte>huns,List<Byte>collectionCards) {

        List<Byte> tempCard = null;
        if(huns.size() > 0) {
            for (int i = 0; i < cards.size(); i++) {
                collectionCards.clear();
                tempCard = cloneList(cards);
                List<Byte> hunsTemp = cloneList(huns);
                collectionCards.add(hunsTemp.remove(0));
                collectionCards.add(tempCard.remove(i));
                Collections.sort(tempCard);
                boolean isHu = true;
                for (Map.Entry<Integer, List<Byte>> entry : cardsMap.entrySet()) {
                    if (!sameCardValidateHu(entry.getKey() == -1 ? tempCard : cloneList(entry.getValue()), collectionCards, hunsTemp)) {
                        isHu = false;
                        break;
                    }
                }
                if (isHu) {
                    return true;
                }
            }
        }
        tempCard = cloneList(cards);
        List<Byte> pairs = findPair(tempCard);
        if (CollectionUtils.isEmpty(pairs)) {
            return false;
        } else {
            for (int i = 0; i < pairs.size() - 1; i = i + 2) {
                collectionCards.clear();
                collectionCards.add(pairs.get(i));
                collectionCards.add(pairs.get(i + 1));
                tempCard.remove(pairs.get(i));
                tempCard.remove(pairs.get(i + 1));
                Collections.sort(tempCard);
                List<Byte> hunsTemp = cloneList(huns);
                boolean isHu = true;
                for (Map.Entry<Integer, List<Byte>> entry : cardsMap.entrySet()) {
                    if (entry.getKey() == -1) {
                        List<Byte> windQian = new ArrayList<Byte>();
                        List<Byte> windHou = new ArrayList<Byte>();
                        for (Byte b : se == entry.getKey() ? tempCard : entry.getValue()) {
                            if (b >= -15) {
                                windQian.add(b);
                            } else {
                                windHou.add(b);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(windQian)) {
                            if (!sameCardValidateHu(windQian, collectionCards, hunsTemp)) {
                                isHu = false;
                                break;
                            }
                        }
                        if (CollectionUtils.isNotEmpty(windHou)) {
                            if (!sameCardValidateHu(windQian, collectionCards, hunsTemp)) {
                                isHu = false;
                                break;
                            }
                        }
                    } else if(!sameCardValidateHu(se == entry.getKey() ? tempCard : cloneList(entry.getValue()), collectionCards, hunsTemp)) {
                        isHu = false;
                        break;
                    }
                }
                if (!isHu) {
                    tempCard = cloneList(cards);
                    continue;
                }
                return true;
            }
            return false;
        }
    }


    /**
     * 校验一门
     * @param tempCards
     * @param collectionCards
     * @param hunTemp
     * @return
     */
    private static boolean sameCardValidateHu(List<Byte>tempCards,List<Byte> collectionCards,List<Byte>hunTemp){

        int i = 0;
        int hunSize = hunTemp.size();
        List<Byte> rabbishCards = new ArrayList<Byte>();
        Collections.sort(tempCards);
        while(true){

            if(tempCards.size() > i && tempCards.size() > (i+1)){
                int faraway = Math.abs((tempCards.get(i)/4 - tempCards.get(i + 1)/4));
                if (faraway > 2) {
                    hunSize = hunSize -2;
                    GameWinCheckUtil.cardCollection(hunSize,collectionCards,i,hunTemp,tempCards,rabbishCards,2);
                    i = i + 1;
                }else if(faraway == 2){
                    hunSize = hunSize - 1;
                    GameWinCheckUtil.cardCollection(hunSize,collectionCards,i,hunTemp,tempCards,rabbishCards,1);
                    i = i + 2;
                } else if (faraway == 0) {
                    if(tempCards.size() == (i + 2)){
                        i = i + 2;
                        continue;
                    }
                    faraway = Math.abs((tempCards.get(i + 1)/4 - tempCards.get(i + 2)/4));
                    if (faraway == 0) {
                        GameWinCheckUtil.cardCollection(hunSize,collectionCards,i,hunTemp,tempCards,rabbishCards,0);
                        i = i + 3;
                    }else{
                        hunSize = hunSize - 1;
                        GameWinCheckUtil.cardCollection(hunSize,collectionCards,i,hunTemp,tempCards,rabbishCards,1);
                        i = i + 2;
                    }
                } else if (faraway == 1) {
                    if(tempCards.size() == (i + 2)){
                        i = i + 2;
                        continue;
                    }
                    faraway = Math.abs((tempCards.get(i + 1)/4 - tempCards.get(i + 2)/4));
                    if(faraway == 1){
                        GameWinCheckUtil.cardCollection(hunSize,collectionCards,i,hunTemp,tempCards,rabbishCards,0);
                        i = i +3;
                    }else{
                        hunSize = hunSize - 1;
                        GameWinCheckUtil.cardCollection(hunSize,collectionCards,i,hunTemp,tempCards,rabbishCards,1);
                        i = i + 2;
                    }
                }
            }else{
                tempCards.removeAll(rabbishCards);
                if(tempCards.size() == 0){
                    return true;
                }else if(tempCards.size() == 1){
                    hunSize = hunSize - 2;
                    GameWinCheckUtil.cardCollection(hunSize,collectionCards,0,hunTemp,tempCards,rabbishCards,2);
                }else if(Math.abs((tempCards.get(1)/4 - tempCards.get(0)/4)) > 2 ){
                    hunSize = hunSize - 4;
                    GameWinCheckUtil.cardCollection(hunSize,collectionCards,0,hunTemp,tempCards,rabbishCards,4);
                }else if(Math.abs((tempCards.get(1)/4 - tempCards.get(0)/4)) <=2){
                    hunSize = hunSize- 1;
                    GameWinCheckUtil.cardCollection(hunSize,collectionCards,0,hunTemp,tempCards,rabbishCards,1);
                }
                if(hunSize < 0){
                    return false;
                }else{
                    return true;
                }
            }

        }
    }


    private static List<Byte> cloneList(List<Byte>src){
        if(CollectionUtils.isEmpty(src)){
            return src;
        }
        List<Byte> des = new ArrayList<Byte>();
        for(Byte b : src){
            des.add(b);
        }
        return des;
    }

    private static List<Byte> findPair(List<Byte> cards){
        List<Byte> pairs = new ArrayList<Byte>();
        for(int i = 0;i < (cards.size() -1);i++){
            if(cards.get(i)/4 == cards.get(i+1)/4){
                pairs.add(cards.get(i));
                pairs.add(cards.get(i+1));
                i = i + 1;
            }
        }
        return pairs;
    }


    public static void main(String[] args) {

        long tid = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            Map<Integer, List<Byte>> map = new HashMap<Integer, List<Byte>>();
            List<Byte> temp = new ArrayList<Byte>();
            List<Byte> temp1 = new ArrayList<Byte>();
            List<Byte> temp2 = new ArrayList<Byte>();
       /*     temp.add((byte) 0);
            temp.add((byte) 1);
            temp.add((byte) 2);
            temp.add((byte) 4);
            temp.add((byte) 5);
            temp.add((byte) 6);
            temp.add((byte) 16);
            temp.add((byte) 17);

            temp1.add((byte) 36);
            temp1.add((byte) 37);
            temp1.add((byte) 38);
            temp1.add((byte) 40);
            temp1.add((byte) 41);
            temp1.add((byte) 42);*/





            temp.add((byte)12);
            temp.add((byte)13);
            temp.add((byte)14);
            temp.add((byte)20);
            temp.add((byte)24);
            temp.add((byte)32);

            temp1.add((byte)101);
            temp1.add((byte)102);

            temp2.add((byte)-24);
            temp2.add((byte)-25);
            temp2.add((byte)-26);


            map.put(0, temp);
            map.put(1, temp1);
            map.put(2, temp2);

            List<Byte> hun = new ArrayList<Byte>();
            hun.add((byte)4);
            hun.add((byte)8);
            hun.add((byte)9);

            List<Byte> collections = new ArrayList<Byte>();


            System.out.println(pairWinAlgorithm(map, hun, collections));
        }
        System.out.println(System.currentTimeMillis() - tid);

    }





}
