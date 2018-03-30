package com.beimi.util;


import java.util.List;
/**
 * Created by zhengchenglei on 2018/3/29.
 */
public class GameWinCheckUtil {


    /**
     *
     * @param hunSize
     * @param collectionCards
     * @param offset
     * @param hunTemp
     * @param cards
     * @param rabbishCards
     * @param collectionSize
     */
    public static void cardCollection(int hunSize,List<Byte>collectionCards,int offset,List<Byte>hunTemp,
                                      List<Byte>cards,List<Byte>rabbishCards,int collectionSize){
        if(hunSize >= 1 && collectionSize == 1){
            collectionCards.add(hunTemp.remove(0));
            collectionCards.add(cards.get(offset));
            collectionCards.add(cards.get(offset+1));
            rabbishCards.add(cards.get(offset));
            rabbishCards.add(cards.get(offset+1));
        }else if(hunSize >= 2 && collectionSize == 2) {
            collectionCards.add(hunTemp.remove(0));
            collectionCards.add(hunTemp.remove(1));
            collectionCards.add(cards.get(offset));
            rabbishCards.add(cards.get(offset));
        }else if(hunSize >= 0 && collectionSize == 0){
            collectionCards.add(cards.get(offset));
            collectionCards.add(cards.get(offset+1));
            collectionCards.add(cards.get(offset+2));
            rabbishCards.add(cards.get(offset));
            rabbishCards.add(cards.get(offset+1));
            rabbishCards.add(cards.get(offset+2));
        }else if(hunSize >= 4 && collectionSize == 4){
            collectionCards.add(hunTemp.remove(0));
            collectionCards.add(hunTemp.remove(1));
            collectionCards.add(hunTemp.remove(2));
            collectionCards.add(hunTemp.remove(3));
            collectionCards.add(cards.get(offset));
            collectionCards.add(cards.get(offset+1));
            rabbishCards.add(cards.get(offset));
            rabbishCards.add(cards.get(offset+1));
        }

    }



}
