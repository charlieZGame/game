package com.beimi.util;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.model.GameResultCheck;
import com.beimi.model.QueAndHaveEight;
import com.beimi.util.rules.model.Action;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Created by zhengchenglei on 2018/3/28.
 */
public class GameHuCheck {



    public static void GameResultHuCheck(Player player, GamePlayway playway, GameResultCheck resultCheck, MJCardMessage message){

        if(player == null || resultCheck == null || message == null){
            return;
        }

        if("1".equals(playway.getGameType())){
            hunResultCheck(player, resultCheck, message);
        }else if("2".equals(playway.getGameType())){

        }else{
            message.setHu(false);
        }
    }


    /**
     *
     * @param player
     * @param resultCheck
     * @param message
     */
    private static void hunResultCheck(Player player, GameResultCheck resultCheck, MJCardMessage message){

        Map<Integer,Integer> hunMap = generateHunMap(player);
        QueAndHaveEight queAndHaveEight = isQueHaveEightEnd(resultCheck,hunMap);
        if(!queAndHaveEight.isQueHaveEight()){
            return ;
        }


    }

    private static boolean validateHu(GameResultCheck resultCheck,MJCardMessage message){


        if(resultCheck.getOthers().size() == 0){
            if(resultCheck.getPairs().size() == 2 || resultCheck.getPairs().size() == 14){
                message.setHu(true);
                return true;
            }else{
                return false;
            }
        }else{

            if(resultCheck.getPairs() == null || resultCheck.getPairs().size() == 0){
                if(resultCheck.getOthers().size() == 2 && resultCheck.getOthers().get(0)/36 == resultCheck.getOthers().get(1)/36 &&
                        resultCheck.getOthers().get(0)/4 == resultCheck.getOthers().get(1)/4 && resultCheck.getPairs().size() == 0){
                    message.setHu(true);
                    return true;
                }


            }else if(resultCheck.getPairs().size() == 1){

            }




          /*  if(resultCheck.getOthers().size() == 2 && resultCheck.getOthers().get(0)/36 == resultCheck.getOthers().get(1)/36 &&
                    resultCheck.getOthers().get(0)/4 == resultCheck.getOthers().get(1)/4 && resultCheck.getPairs().size() == 0){
                message.setHu(true);
                return true;
            }else if(resultCheck.getOthers().size() == 1 && resultCheck.getHuns().size() >= 1){
                message.setHu(true);
                return true;
            }else if(resultCheck.getOthers().size() == 2 && resultCheck.getHuns().size() >= 1){
                Collections.sort(resultCheck.getOthers());
            }else if(resultCheck.getOthers().size() == 3){

            }*/

        }

        return false;
    }



    private static  Map<Integer,Integer> generateHunMap(Player player){
        byte[] powerful = player.getPowerfullArray();
        Map<Integer,Integer> hunMap = new HashMap<Integer,Integer>();
        for(int i = 0;i<powerful.length;i++){
            hunMap.put(powerful[i]/4,null);
        }
        return hunMap;
    }


    /**
     * @param resultCheck
     * @param hunMap
     * @return
     */
    public static QueAndHaveEight isQueHaveEightEnd(GameResultCheck resultCheck,Map<Integer,Integer> hunMap){

        List<Byte> leave = new ArrayList<Byte>();
        QueAndHaveEight queAndHaveEight = new QueAndHaveEight();
        Map<Integer,Integer> que = new HashMap<Integer,Integer>();
        pairsHandler(resultCheck,hunMap,leave,que);
        threeHandler(resultCheck.getKezis(),hunMap,leave,que);
        threeHandler(resultCheck.getStraight(),hunMap,leave,que);
        pengsAndGangsHandler(resultCheck.getPengs(),que);
        othersHandler(resultCheck.getOthers(),hunMap,leave,que);
        int num = 0;
        for(Map.Entry<Integer,Integer> entry : que.entrySet()){

            if(entry.getValue() + leave.size() >= 8){
                queAndHaveEight.setHaveEight(true);
            }
            if(entry.getKey() < 0){
                continue;
            }
            num ++;
        }
        if(num < 3){
            queAndHaveEight.setQue(true);
        }
        return queAndHaveEight;
    }


    /**
     * 对子处理
     * @param resultCheck
     * @param hunMap
     * @param leave
     * @param que
     */
    private static void pairsHandler(GameResultCheck resultCheck,Map<Integer,Integer> hunMap,List<Byte> leave, Map<Integer,Integer> que) {

        if (resultCheck.getPairs() == null) {
            return;
        }
        for (int i = 1; i < resultCheck.getPairs().size(); i++) {
            if (i % 2 != 1) {
                continue;
            }
            byte card = resultCheck.getPairs().get(i);
            byte previousCard = resultCheck.getPairs().get(i - 1);
            if (hunMap.containsKey(card / 4) && hunMap.containsKey(previousCard)) {
                leave.add(card);
                leave.add(previousCard);
                continue;
            } else if(!hunMap.containsKey(card / 4)){
                addQue(que,card,2);
                continue;
            }else if(!hunMap.containsKey(previousCard/4)){
                addQue(que,card,2);
            }
        }
    }


    /**
     *  碰杠处理,碰杠没有混
     * @param cards
     * @param que
     */
    private static void pengsAndGangsHandler(List<Byte> cards,Map<Integer,Integer>que){

        if(CollectionUtils.isEmpty(cards)){
            return;
        }
        for(Byte card : cards){
            addQue(que,card,1);
        }
    }

    /**
     *小三张处理
     * @param threeCards
     * @param hunMap
     * @param leave
     * @param que
     */
    private static void threeHandler(List<Byte> threeCards,Map<Integer,Integer> hunMap,List<Byte> leave, Map<Integer,Integer> que) {

        if (threeCards == null) {
            return;
        }
        for (int i = 0; i < threeCards.size(); i++) {
            if (i % 3 != 2) {
                continue;
            }
            byte card = threeCards.get(i);
            byte previousCardOne = threeCards.get(i - 1);
            byte previousCardTwo = threeCards.get(i - 2);

            if (hunMap.containsKey(card / 4) && hunMap.containsKey(previousCardOne / 4) && hunMap.containsKey(previousCardTwo / 4)) {
                leave.add(card);
                leave.add(previousCardOne);
                leave.add(previousCardTwo);
                continue;
            } else if (!hunMap.containsKey(card / 4)) {
                addQue(que,card,3);
                continue;
            }else if(!hunMap.containsKey(previousCardOne / 4)){
                addQue(que,previousCardOne,3);
                continue;
            }else if(!hunMap.containsKey(previousCardTwo / 4)){
                addQue(que,previousCardTwo,3);
                continue;
            }
        }
    }


    private static void othersHandler(List<Byte>others,Map<Integer,Integer> hunMap,List<Byte> leave, Map<Integer,Integer> que) {

        if(CollectionUtils.isEmpty(others)){
            return;
        }

        for(byte b : others){
            if(hunMap.containsKey(b/4)){
                leave.add(b);
            }else {
                addQue(que, b, 1);
            }
        }
    }




    private static void addQue(Map<Integer,Integer> que,byte card,int size){
        if(card < 0) {
            if (que.containsKey(-1)) {
                que.put(-1, que.get(-1) + 3);
            } else {
                que.put(-1, 3);
            }
        } else {
            if (que.containsKey(card / 36)) {
                que.put(card / 36, que.get(card / 36) + size);
            } else {
                que.put(card / 36, 1);
            }
        }

    }


        /**
         *
         * @param card
         * @param que
         */
    private static void huaSe(byte card,Map<Integer,Integer>que){

        if(que == null){
            return;
        }
        int se = 0;
        if(card < 0){
            se = -1;
        }else{
            se = card / 36;
        }
        if(que.containsKey(se)){
            que.put(se,que.get(se) + 1);
        }else{
            que.put(se,1);
        }
    }

    private static Map<Integer,Integer> getHuns(Player player,GameResultCheck resultCheck){

        Map<Integer,Integer> huns = new HashMap<Integer,Integer>();

        return null;


    }


    public static GameResultCheck generateGameResultCheck(Player player,GameResultCheck resultCheck){

        if(resultCheck == null || player == null){
            return resultCheck;
        }
        if(CollectionUtils.isEmpty(player.getActions())){
            return resultCheck;
        }
        for(Action action : player.getActions()){
            if(BMDataContext.PlayerAction.PENG.toString().equals(action.getAction().toString())){
                for(int i = 0 ; i < 3; i++){
                    resultCheck.getPengs().add(action.getCard());
                }

            }else if(BMDataContext.PlayerAction.GANG.toString().equals(action.getAction().toString())){
                for(int i = 0 ; i < 4; i++){
                    resultCheck.getPengs().add(action.getCard());
                }
            }
        }
        return resultCheck;
    }



}
