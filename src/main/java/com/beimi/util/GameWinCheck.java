package com.beimi.util;


import com.beimi.core.BMDataContext;
import com.beimi.model.GameResultSummary;
import com.beimi.util.rules.model.Action;
import com.beimi.util.rules.model.Player;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Created by zhengchenglei on 2018/3/29.
 */
public class GameWinCheck {


    /**
     * @param cards 不是全部的牌，需要把混子去掉
     * @param huns
     * @return
     */
    public static void pairWinAlgorithm(Map<Integer, List<Byte>> cards, List<Byte> huns, List<List<Byte>> collectionCardList) {

        // 先从混子里边找对子，应为这个一个共同的过程
        if (huns.size() >= 2) {
            List<Byte> hunsTemp = cloneList(huns);
            List<Byte> collectionCards = new ArrayList<Byte>();
            collectionCards.add(hunsTemp.remove(0));
            collectionCards.add(hunsTemp.remove(0));
            boolean isHu = true;
            for (Map.Entry<Integer, List<Byte>> entry : cards.entrySet()) {
                if (!sameCardValidateHu(cloneList(entry.getValue()), collectionCards, hunsTemp)) {
                    isHu = false;
                    break;
                }
            }
            if (isHu) {
                collectionCardList.add(collectionCards);
                //  return true;
            }
        }
        for (Map.Entry<Integer, List<Byte>> entry : cards.entrySet()) {
            sameSeValidateHu(entry.getValue(), entry.getKey(), cards, huns, collectionCardList);
        }
    }



    public static void exceCategory(byte card,Map<Integer,List<Byte>>mapCards){

        if(card < -15){
            if(mapCards.get(-2) == null){
                List<Byte> list = new ArrayList<Byte>();
                list.add(card);
                mapCards.put(-2,list);
            }else{
                mapCards.get(-2).add(card);
            }
        }else if(card >= -15 && card < 0){
            if(mapCards.get(-1) == null){
                List<Byte> list = new ArrayList<Byte>();
                list.add(card);
                mapCards.put(-1,list);
            }else{
                mapCards.get(-1).add(card);
            }
        }else{
            if(mapCards.get(card/36) == null){
                List<Byte> list = new ArrayList<Byte>();
                list.add(card);
                mapCards.put(card/36,list);
            }else{
                mapCards.get(card/36).add(card);
            }
        }

    }


    /**
     * 校验一盘，按色校验
     *
     * @param cards
     * @param cardsMap
     * @param huns
     * @param collectionCardList
     * @return
     */
    private static void sameSeValidateHu(List<Byte> cards, int se, Map<Integer, List<Byte>> cardsMap, List<Byte> huns, List<List<Byte>> collectionCardList) {

        List<Byte> tempCard = null;
        if (huns.size() > 0) {
            for (int i = 0; i < cards.size(); i++) {
                List<Byte> collectionCards = new ArrayList<Byte>();
                tempCard = cloneList(cards);
                List<Byte> hunsTemp = cloneList(huns);
                collectionCards.add(hunsTemp.remove(0));
                collectionCards.add(tempCard.remove(i));
                Collections.sort(tempCard);
                boolean isHu = true;
                for (Map.Entry<Integer, List<Byte>> entry : cardsMap.entrySet()) {
                    if (!sameCardValidateHu(entry.getKey() == se ? tempCard : cloneList(entry.getValue()), collectionCards, hunsTemp)) {
                        isHu = false;
                        break;
                    }
                }
                if (isHu) {
                    collectionCardList.add(collectionCards);
                }
            }
        }
        tempCard = cloneList(cards);
        Map<Integer, List<Byte>> map = findPair(tempCard);
        if ((map == null || map.isEmpty()) && huns.size() <= 0) {
            return;
        } else {

            //这个检查逻辑以后可以去掉，因为就是从当前点校验
            if (!map.containsKey(se)) {
                return;
            }
            List<Byte> pairs = map.get(se);
            for (int i = 0; i < pairs.size() - 1; i = i + 2) {
                List<Byte> collectionCards = new ArrayList<Byte>();
                collectionCards.add(pairs.get(i));
                collectionCards.add(pairs.get(i + 1));
                tempCard.remove(pairs.get(i));
                tempCard.remove(pairs.get(i + 1));
                Collections.sort(tempCard);
                List<Byte> hunsTemp = cloneList(huns);
                boolean isHu = true;
                for (Map.Entry<Integer, List<Byte>> entry : cardsMap.entrySet()) {
                    if (!sameCardValidateHu(se == entry.getKey() ? tempCard : cloneList(entry.getValue()), collectionCards, hunsTemp)) {
                        isHu = false;
                        break;
                    }
                }
                if (!isHu) {
                    tempCard = cloneList(cards);
                    continue;
                }
                collectionCardList.add(collectionCards);
            }
        }
    }


    /**
     * 校验一门
     *
     * @param tempCards
     * @param collectionCards
     * @param hunTemp
     * @return
     */
    private static boolean sameCardValidateHu(List<Byte> tempCards, List<Byte> collectionCards, List<Byte> hunTemp) {

        int i = 0;
        int hunSize = hunTemp.size();
        List<Byte> rabbishCards = new ArrayList<Byte>();
        Collections.sort(tempCards);
        while (true) {

            if (tempCards.size() > i && tempCards.size() > (i + 1)) {
                int faraway = Math.abs((tempCards.get(i) / 4 - tempCards.get(i + 1) / 4));
                if (faraway > 2) {
                    GameWinCheckUtil.cardCollection(hunSize, collectionCards, i, hunTemp, tempCards, rabbishCards, 2);
                    hunSize = hunSize - 2;
                    i = i + 1;
                } else if (faraway == 2) {
                    GameWinCheckUtil.cardCollection(hunSize, collectionCards, i, hunTemp, tempCards, rabbishCards, 1);
                    hunSize = hunSize - 1;
                    i = i + 2;
                } else if (faraway == 0) {
                    if (tempCards.size() == (i + 2)) {
                        i = i + 2;
                        continue;
                    }
                    faraway = Math.abs((tempCards.get(i + 1) / 4 - tempCards.get(i + 2) / 4));
                    if (faraway == 0) {
                        GameWinCheckUtil.cardCollection(hunSize, collectionCards, i, hunTemp, tempCards, rabbishCards, 0);
                        i = i + 3;
                    } else {
                        GameWinCheckUtil.cardCollection(hunSize, collectionCards, i, hunTemp, tempCards, rabbishCards, 1);
                        hunSize = hunSize - 1;
                        i = i + 2;
                    }
                } else if (faraway == 1) {
                    if (tempCards.size() == (i + 2)) {
                        i = i + 2;
                        continue;
                    }
                    faraway = Math.abs((tempCards.get(i + 1) / 4 - tempCards.get(i + 2) / 4));
                    if (faraway == 1) {
                        GameWinCheckUtil.cardCollection(hunSize, collectionCards, i, hunTemp, tempCards, rabbishCards, 0);
                        i = i + 3;
                    } else {
                        GameWinCheckUtil.cardCollection(hunSize, collectionCards, i, hunTemp, tempCards, rabbishCards, 1);
                        hunSize = hunSize - 1;
                        i = i + 2;
                    }
                }
            } else {
                tempCards.removeAll(rabbishCards);
                if (tempCards.size() == 0) {
                    return true;
                } else if (tempCards.size() == 1) {
                    GameWinCheckUtil.cardCollection(hunSize, collectionCards, 0, hunTemp, tempCards, rabbishCards, 2);
                    hunSize = hunSize - 2;
                } else if (Math.abs((tempCards.get(1) / 4 - tempCards.get(0) / 4)) > 2) {
                    GameWinCheckUtil.cardCollection(hunSize, collectionCards, 0, hunTemp, tempCards, rabbishCards, 4);
                    hunSize = hunSize - 4;
                } else if (Math.abs((tempCards.get(1) / 4 - tempCards.get(0) / 4)) <= 2) {
                    GameWinCheckUtil.cardCollection(hunSize, collectionCards, 0, hunTemp, tempCards, rabbishCards, 1);
                    hunSize = hunSize - 1;
                }
                if (hunSize < 0) {
                    return false;
                } else {
                    return true;
                }
            }

        }
    }


    private static List<Byte> cloneList(List<Byte> src) {
        if (CollectionUtils.isEmpty(src)) {
            return src;
        }
        List<Byte> des = new ArrayList<Byte>();
        for (Byte b : src) {
            des.add(b);
        }
        return des;
    }

    private static Map<Integer, List<Byte>> findPair(List<Byte> cards) {
        Map<Integer, List<Byte>> map = new HashMap<Integer, List<Byte>>();

        for (int i = 0; i < (cards.size() - 1); i++) {
            if (cards.get(i) / 4 == cards.get(i + 1) / 4) {
                if (cards.get(i) < -15) {
                    if (map.get(-2) == null) {
                        List<Byte> pairs = new ArrayList<Byte>();
                        pairs.add(cards.get(i));
                        pairs.add(cards.get(i + 1));
                        map.put(-2, pairs);
                    } else {
                        map.get(-2).add(cards.get(i));
                        map.get(-2).add(cards.get(i + 1));
                    }
                } else if (cards.get(i) < 0 && cards.get(i) >= -15) {
                    if (map.get(-1) == null) {
                        List<Byte> pairs = new ArrayList<Byte>();
                        pairs.add(cards.get(i));
                        pairs.add(cards.get(i + 1));
                        map.put(-1, pairs);
                    } else {
                        map.get(-1).add(cards.get(i));
                        map.get(-1).add(cards.get(i + 1));
                    }
                } else {
                    if (map.get(cards.get(i) / 36) == null) {
                        List<Byte> pairs = new ArrayList<Byte>();
                        pairs.add(cards.get(i));
                        pairs.add(cards.get(i + 1));
                        map.put(cards.get(i) / 36, pairs);
                    } else {
                        map.get(cards.get(i) / 36).add(cards.get(i));
                        map.get(cards.get(i) / 36).add(cards.get(i + 1));
                    }
                }

                i = i + 1;
            }
        }
        return map;
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

            List<Byte> cards = new ArrayList<Byte>();
            cards.add((byte)0);
            cards.add((byte)13);
            cards.add((byte)14);
            cards.add((byte)20);
            cards.add((byte)24);
            cards.add((byte)32);
            cards.add((byte)101);
            cards.add((byte)102);
            cards.add((byte)-24);
            cards.add((byte)-25);
            cards.add((byte)-26);
            cards.add((byte)4);
            cards.add((byte)8);
            cards.add((byte)9);


            temp.add((byte) 0);
            temp.add((byte) 13);
            temp.add((byte) 14);
            temp.add((byte) 20);
            temp.add((byte) 24);
            temp.add((byte) 32);

            temp1.add((byte) 101);
            temp1.add((byte) 102);

            temp2.add((byte) -24);
            temp2.add((byte) -25);
            temp2.add((byte) -26);


            map.put(0, temp);
            map.put(2, temp1);
            map.put(-2, temp2);

            List<Byte> hun = new ArrayList<Byte>();
            hun.add((byte) 4);
            hun.add((byte) 8);
            hun.add((byte) 9);

            List<List<Byte>> collections = new ArrayList<List<Byte>>();

            Player player = new Player("23232");
            byte[] b = new byte[3];
            b[0] = 4;
            b[1] = 8;
            b[2] = 9;
            player.setPowerfull(b);
            Map<Integer, Byte> hunMap = new HashMap<Integer, Byte>();
            haveEightAndQueBefore(player,cards,true,hunMap);
            pairWinAlgorithm(map, hun, collections);
            haveEightAndQueEnd(player,hunMap,null,collections,true);

            if(collections.size() > 0){
                System.out.println("糊了");
            }else{
                System.out.println("未糊");
            }

            for(List<Byte> cds : collections){
                System.out.println("=============================");
                for(Byte card : cds){
                    System.out.print(card+",");
                }
                System.out.println("=============================");
            }

            //  Collections.sort(collections);
         /*   System.out.println("=========================");
            for (int j = 0; j < collections.size(); j++) {
                System.out.print(collections.get(j)+",");

            }
            System.out.println();
            System.out.println("=========================");*/
        }
        System.out.println(System.currentTimeMillis() - tid);

    }


    /**
     *
     * @param player
     * @param cards 全部的牌
     * @param isNeedQue
     * @return
     */
    public static boolean haveEightAndQueBefore(Player player, List<Byte> cards,boolean isNeedQue,Map<Integer, Byte> hunMap) {

        Map<Integer, Integer> que = new HashMap<Integer, Integer>();
        Map<Integer, Byte> data = new HashMap<Integer, Byte>();

       // Map<Integer,Integer> que = new HashMap<Integer,Integer>();
        if(CollectionUtils.isNotEmpty(player.getActions())){
            for(Action action : player.getActions()){
                if(BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
                    addQue(action.getCard(),3,que);
                }else if(BMDataContext.PlayerAction.GANG.toString().equals(action.getAction())){
                    addQue(action.getCard(),4,que);
                }
            }
        }


        for (byte temp : cards) {

            int key = temp / 4;
            if (data.get(key) == null) {
                data.put(key, (byte) 1);
            } else {
                data.put(key, (byte) (data.get(key) + 1));
            }
            if (hunMap.containsKey(temp / 4)) {
                continue;
            }
            Integer se = temp / 36;  //花色
            if (temp < 0) {
                se = -1;
            }
            if (que.get(se) == null) {
                que.put(se, 1);
            } else {
                que.put(se, que.get(se) + 1);
            }

        }

        int hunNum = 0;
        for (Map.Entry<Integer, Byte> entry : data.entrySet()) {
            if (hunMap.containsKey(entry.getKey())) {
                hunNum = hunNum + entry.getValue();
            }
        }

        boolean result = validateQueAndHaveEight(isNeedQue, que, hunNum);
        System.out.println("前部分校验结果为["+result+"]");
        return result;
    }


    public static boolean haveEightAndQueEnd(Player player, Map<Integer,Byte>huns,List<Byte>pairs,List<List<Byte>> collections,boolean isNeedQue) {

        Map<Integer,Integer> que = new HashMap<Integer,Integer>();
        if(CollectionUtils.isNotEmpty(player.getActions())){
            for(Action action : player.getActions()){
                if(BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
                    addQue(action.getCard(),3,que);
                }else if(BMDataContext.PlayerAction.GANG.toString().equals(action.getAction())){
                    addQue(action.getCard(),4,que);
                }
            }
        }


        if(CollectionUtils.isNotEmpty(pairs)){
           int anyCards = addPairQue(pairs,que,huns);
            boolean result = validateQueAndHaveEight(isNeedQue,que,anyCards);
            System.out.println("后校验校验结果 ["+result+"]");
            return result;
        }


        List<List<Byte>> huCards = new ArrayList<List<Byte>>();
        if(CollectionUtils.isNotEmpty(collections)) {
            for (List<Byte> collection : collections) {
                if (CollectionUtils.isEmpty(collection)) {
                    continue;
                }
                int anyCards = 0;
                List<Byte> temp = cloneList(collection);
                List<Byte> pairTemp = new ArrayList<Byte>();
                pairTemp.add(temp.remove(0));
                pairTemp.add(temp.remove(0));
                anyCards = addPairQue(pairTemp,que,huns);
                for(int i = 0;i< temp.size()-2;i = i+3){
                    List<Byte>huntemp = new ArrayList<Byte>();
                    List<Byte>cards = new ArrayList<Byte>();
                    for(int j = i;j<i+3;j++){
                        if(huns.containsKey(temp.get(j)/4)){
                            huntemp.add(temp.get(j));
                        }else{
                            cards.add(temp.get(j));
                        }
                    }
                    if(huntemp.size() == 3){
                        anyCards = anyCards +3;
                    }else{
                        addQue(cards.get(0),3,que);
                    }
                }
                if(validateQueAndHaveEight(isNeedQue,que,anyCards)){
                    huCards.add(collection);
                }
            }

            collections.clear();
            collections.addAll(huCards);
            if(huCards.size() == 0){
                System.out.println("后校验校验结果 ["+false+"]");
                return false;
            }else{
                System.out.println("后校验校验结果 ["+true+"]");
                return true;
            }
        }
        System.out.println("后校验校验结果 ["+false+"]");
        return false;
    }

    private static boolean validateQueAndHaveEight(boolean isNeedQue,Map<Integer,Integer>que,Integer hunNum){

        boolean isQue = false;
        boolean isHaveEight = false;

        if(isNeedQue) {
            if (que.get(-1) == null && que.size() < 3) {
                isQue = true;
            } else if (que.get(-1) != null && que.size() < 4) {
                isQue = true;
            }
        }
        for (Map.Entry<Integer, Integer> entry : que.entrySet()) {

            if (entry.getValue() + hunNum >= 8) {
                isHaveEight = true;
            }
        }

        if(isNeedQue) {
            return isQue && isHaveEight;
        }else{
            return isHaveEight;
        }

    }


    /**
     *
     * @param que
     */
    private static int addPairQue(List<Byte>cards,Map<Integer,Integer>que,Map<Integer,Byte>huns) {

        if (CollectionUtils.isEmpty(cards)) {
            return 0;
        }
        int anyCards = 0;
        List<Byte> temp = cloneList(cards);
        for (int i = 0;i<cards.size() - 1;i++) {
            List<Byte>huntemp = new ArrayList<Byte>();
            List<Byte>tempcards = new ArrayList<Byte>();
            for(int j = i;j<i+2;j++){
                if(huns.containsKey(temp.get(j)/4)){
                    huntemp.add(temp.get(j));
                }else{
                    tempcards.add(temp.get(j));
                }
            }
            if(huntemp.size() == 2){
                anyCards = anyCards + 2;
            }else{
                addQue(tempcards.get(0),2,que);
            }
        }

        return anyCards;

    }


    private static void addQue(byte card,int size,Map<Integer,Integer>que){

        if(card < 0){
            if(que.get(-1) == null){
                que.put(-1,3);
            }else{
                que.put(-1,que.get(-1)+3);
            }
        }else{
            if(que.get(card/36) == null){
                que.put(card/36,3);
            }else{
                que.put(card/36,que.get(card/36)+3);
            }
        }
    }


    /**
     *
     * @param player
     * @param collections
     * @return
     */
    public static List<GameResultSummary> playerSunmary(Player player, List<List<Byte>>collections){

        if(CollectionUtils.isEmpty(collections)){
            List<GameResultSummary> gameResultChecks = new ArrayList<GameResultSummary>();
            GameResultSummary gameResultCheck = new GameResultSummary();
            if(CollectionUtils.isEmpty(player.getActions())){
                return gameResultChecks;
            }
            generateGangPeng(player,gameResultCheck);
            gameResultChecks.add(gameResultCheck);
            return gameResultChecks;
        }
        List<GameResultSummary> gameResultChecks = new ArrayList<GameResultSummary>();
        for(List<Byte> list : collections){
            if(CollectionUtils.isEmpty(list)){
                continue;
            }
            GameResultSummary gameResultCheck = new GameResultSummary();
            List<Byte> pairs = new ArrayList<Byte>();
            gameResultCheck.setPairs(pairs);
            pairs.add(list.remove(0));
            pairs.add(list.remove(0));
            List<Byte> three = new ArrayList<Byte>();
            gameResultCheck.setThree(three);
            for(int i = 0 ; i < list.size() - 2;i = i+3) {
                three.add(list.get(i));
                three.add(list.get(i+1));
                three.add(list.get(i+2));
            }

            if(CollectionUtils.isEmpty(player.getActions())){
                return gameResultChecks;
            }
            generateGangPeng(player,gameResultCheck);
            gameResultChecks.add(gameResultCheck);
        }
        return gameResultChecks;
    }


    private static void generateGangPeng(Player player,GameResultSummary gameResultCheck){
        List<Byte> pengs = new ArrayList<Byte>();
        List<Byte> agangs = new ArrayList<Byte>();
        List<Byte> mgangs = new ArrayList<Byte>();
        gameResultCheck.setPengs(pengs);
        gameResultCheck.setAgangs(agangs);
        gameResultCheck.setMgangs(mgangs);
        for(Action action : player.getActions()){
            if(BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
                pengs.add((byte)(action.getCard()/4 *4));
                pengs.add((byte)(action.getCard()/4 *4+1));
                pengs.add((byte)(action.getCard()/4 *4+2));
            }else if(BMDataContext.PlayerAction.GANG.toString().equals(action.getAction())){
                if(BMDataContext.PlayerGangAction.AN.toString().equals(action.getType())) {
                    agangs.add((byte) (action.getCard() / 4 * 4));
                    agangs.add((byte) (action.getCard() / 4 * 4 + 1));
                    agangs.add((byte) (action.getCard() / 4 * 4 + 2));
                    agangs.add((byte) (action.getCard() / 4 * 4 + 3));
                }else if(BMDataContext.PlayerGangAction.MING.toString().equals(action.getType())){
                    mgangs.add((byte) (action.getCard() / 4 * 4));
                    mgangs.add((byte) (action.getCard() / 4 * 4 + 1));
                    mgangs.add((byte) (action.getCard() / 4 * 4 + 2));
                    mgangs.add((byte) (action.getCard() / 4 * 4 + 3));
                }
            }
        }
    }

}
