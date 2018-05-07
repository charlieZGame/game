package com.beimi.rule;

import com.alibaba.fastjson.JSONObject;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Action;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class HuValidate {



    private static List<AbsCheckScoreRule> checkScoreRuleList;

    static {
        checkScoreRuleList = new ArrayList<AbsCheckScoreRule>();
        checkScoreRuleList.add(new YTLValidate());
        checkScoreRuleList.add(new WHHValidate());
        checkScoreRuleList.add(new QYSValidate());
        checkScoreRuleList.add(new QXDValidate());
    }


    public static List<ReturnResult> validateHu(Player[] players,GameRoom gameRoom,String bank,GamePlayway playway) {

        if (CollectionUtils.isEmpty(checkScoreRuleList) || players == null || players.length == 0) {
            return null;
        }

        String winUser = null;
        Map<String, Integer> map = new HashMap<String, Integer>();
        Map<String, Integer> allUserScore = null;
        Map<String, Integer> userScore = new HashMap<String, Integer>();
        int coverSize = 0;
        int coverInnerSize = 0;

        Map<String,Player> playMap = new HashMap<String,Player>();
        for(Player player : players){
            playMap.put(player.getPlayuser(),player);
        }

        List<ReturnResult> returnResults = new ArrayList<ReturnResult>();

        for (Player player : players) {
            userScore.put(player.getPlayuser(), 0);
        }

        boolean isHaveWin = false;
        for (Player player : players) {
            if (!player.isWin()) {
                continue;
            }
            isHaveWin = true;
            if(CollectionUtils.isNotEmpty(player.getCoverCards())){
                coverSize = player.getCoverCards().size()/4;

            }

            ReturnResult allTempReturnResult = null;
            for (List<Byte> collection : player.getCollections()) {
                for (Map.Entry<String, Integer> entry : userScore.entrySet()) {
                    userScore.put(entry.getKey(), 0);
                }
                int tempCoverInnerSize = 0;
                int i = 0;
                StringBuilder vaStr = new StringBuilder();
                for (AbsCheckScoreRule checkScoreRule : checkScoreRuleList) {
                    checkScoreRule.setData(collection, player.getActions(), player.getPowerfullArray());
                    if ("koudajiang".equals(playway.getCode())) {
                        if (checkScoreRule.isSatisfy()) {
                            if (checkScoreRule instanceof YTLValidate) {
                                vaStr.append("一条龙");
                            } else if (checkScoreRule instanceof QYSValidate) {
                                vaStr.append("清一色");
                            } else if (checkScoreRule instanceof QXDValidate) {
                                vaStr.append("七小对");
                            }
                            i++;
                        }
                        // 混一色只存在扣的情况还是都存在
                        if (CollectionUtils.isNotEmpty(player.getCoverCards())) {
                            tempCoverInnerSize = getdajiangSize(collection, player.getActions(), player.getPowerfullArray(), player.getCoverCards());
                        }
                    } else if ("majiang".equals(playway.getCode())) {
                        if (checkScoreRule.isSatisfy()) {
                            if (checkScoreRule instanceof YTLValidate) {
                                vaStr.append("一条龙");
                            } else if (checkScoreRule instanceof WHHValidate) {
                                vaStr.append("无混糊");
                            } else if (checkScoreRule instanceof QYSValidate) {
                                vaStr.append("清一色");
                            } else if (checkScoreRule instanceof QXDValidate) {
                                vaStr.append("七小对");
                            }
                            i++;
                        }
                    } else {
                        break;
                    }
                }
                map.put(player.getPlayuser(), i);
                winUser = player.getPlayuser();
                ReturnResult returnResult = new ReturnResult();
                returnResult.setWin(true);
                returnResult.setCollections(collection);
                StringBuilder sb = new StringBuilder();
                //庄票处理
                System.out.println("票数量["+gameRoom.getPiao()+"]");
                if (player.isZm()) {
                    if (i == 1) {
                        //老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()){
                            returnResult.setScore(36 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 12,returnResult,players);
                        }else{
                            returnResult.setScore(32 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 10,returnResult,players);
                            zuangHandler(players,userScore,player,returnResult);
                        }
                        returnResult.setDesc(returnResult.getDesc() + sb.append(" 老龙(自摸" + vaStr.toString() + ") ").toString());
                    } else if (i == 2) {
                        //老老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()){
                            returnResult.setScore(66 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 22,returnResult,players);
                        }else{
                            returnResult.setScore(62 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 20,returnResult,players);
                            zuangHandler(players,userScore,player,returnResult);
                        }
                        returnResult.setDesc(returnResult.getDesc() + sb.append(" 老老龙(自摸" + vaStr.toString() + ") ").toString());
                    }else if(i >= 3){
                        //老老老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()) {
                            returnResult.setScore(126 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 42,returnResult,players);
                        }else{
                            returnResult.setScore(122 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 40,returnResult,players);
                            zuangHandler(players,userScore,player,returnResult);
                        }
                        returnResult.setDesc(returnResult.getDesc()+ sb.append(" 老老老龙(自摸" + vaStr.toString() + ") ").toString());
                    } else {
                        // 自摸 篓子每一家两分
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()) {
                            returnResult.setScore(12 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 4,returnResult,players);
                        }else{
                            returnResult.setScore(8 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 2,returnResult,players);
                            zuangHandler(players,userScore,player,returnResult);
                        }
                        returnResult.setDesc(returnResult.getDesc()+ sb.append(" 赢(自摸) ").toString());
                    }
                } else {
                    if (i == 1) {
                        //少龙
                        // 自摸 篓子每一家两分
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()) {
                            returnResult.setScore(19 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 19);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }else{
                            returnResult.setScore(17 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 17);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }
                        returnResult.setDesc(returnResult.getDesc() + sb.append(" 少龙(" + vaStr.toString() + ") ").toString());
                    } else if (i == 2) {
                        //老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()) {
                            returnResult.setScore(36 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 36);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }else{
                            returnResult.setScore(32 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 32);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }
                        returnResult.setDesc(returnResult.getDesc() + sb.append(" 老龙(" + vaStr.toString() + ") ").toString());
                    }else if(i >= 3){
                        //老老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()) {
                            returnResult.setScore(66 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 66);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }else{
                            returnResult.setScore(62 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 62);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }
                        returnResult.setDesc(returnResult.getDesc() + sb.append(" 老老龙(" + vaStr.toString() + ")").toString());
                    } else {
                        //坎子
                        try {

                            // 坎子的情况在都大将的时候 自己的杠不算分

                            returnResult.setUserId(player.getPlayuser());
                            String pengGangResult = getGangAndPengHandler(players,playway.getCode(),player, userScore);
                            //// TODO: 2018/4/22  ZCL菽粟计算再是有问题
                            if(player.isBanker()) {
                                userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 8);
                                returnResult.setScore(8 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            }else{
                                userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 5);
                                returnResult.setScore(5 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            }
                            sb.append(pengGangResult == null ? "" : pengGangResult);
                            // 坎子点炮的人全出
                            otherPlayerScore(userScore, player, 0,returnResult,players);
                            returnResult.setDesc(returnResult.getDesc() + sb.append(" 赢(点炮) ").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(tempCoverInnerSize > 0){
                    userScore.put(player.getPlayuser(),userScore.get(player.getPlayuser())*tempCoverInnerSize*2);
                    returnResult.setScore(returnResult.getScore() * tempCoverInnerSize * 2);
                }
                if (allTempReturnResult == null) {
                    allTempReturnResult = returnResult;
                    allUserScore = userScore;
                    coverInnerSize = tempCoverInnerSize;
                } else {
                    if (allTempReturnResult.getScore() < returnResult.getScore()) {
                        allTempReturnResult = returnResult;
                        allUserScore = userScore;
                        coverInnerSize = tempCoverInnerSize;
                    }
                }
            }
            //庄扣处理 大将的那部分在上边已经处理了
            if(coverSize > 0){
                allTempReturnResult.setScore(allTempReturnResult.getScore()*(coverSize*2));
                if(gameRoom.getPiao() > 0){
                    allTempReturnResult.setDesc("飘(" + player.getPiao() + ")"+allTempReturnResult.getDesc() +"扣[" + coverSize + "] 总分[" + allTempReturnResult.getScore() + "]");
                }else {
                    allTempReturnResult.setDesc(allTempReturnResult.getDesc() + "扣[" + coverSize + "] 总分[" + allTempReturnResult.getScore() + "]");
                }
            }else {
                if(gameRoom.getPiao() > 0) {
                    allTempReturnResult.setDesc("飘(" + player.getPiao() + ")" + allTempReturnResult.getDesc() + "总分[" + allTempReturnResult.getScore() + "]");
                }else{
                    allTempReturnResult.setDesc(allTempReturnResult.getDesc() + " 总分[" + allTempReturnResult.getScore() + "]");
                }
            }
            returnResults.add(allTempReturnResult);
        }

        if(!isHaveWin){
            return returnResults;
        }

        for (Map.Entry<String, Integer> entry : allUserScore.entrySet()) {
            if (!entry.getKey().equals(winUser)) {
                ReturnResult returnResult = new ReturnResult();
                returnResult.setUserId(entry.getKey());
                returnResult.setScore(entry.getValue());
                if(coverSize > 0){
                    int doub = coverSize;
                    if(coverInnerSize > 0){
                        doub = doub + coverInnerSize;
                    }
                    returnResult.setScore(returnResult.getScore()*(doub*2));
                }
                if(gameRoom.getPiao() > 0){
                    returnResult.setDesc("飘(" + playMap.get(entry.getKey()).getPiao() + ")"+"失败 " + ((returnResult.getScore() < 0) ? "输[" + returnResult.getScore() + "]分" : "赢[" + returnResult.getScore() + "]分"));
                }else {
                    returnResult.setDesc("失败 " + ((returnResult.getScore() < 0) ? "输[" + returnResult.getScore() + "]分" : "赢[" + returnResult.getScore() + "]分"));
                }
                returnResults.add(returnResult);
            }
        }

        return returnResults;
    }


    private static void zuangHandler(Player[] players,Map<String, Integer> userScore,Player player,ReturnResult returnResult) {
        for (Player player1 : players) {
            if (player1.isBanker()) {
                userScore.put(player1.getPlayuser(), userScore.get(player1.getPlayuser()) - 2);
                userScore.put(player1.getPlayuser(), userScore.get(player1.getPlayuser()) - player.getPiao());
                returnResult.setScore(returnResult.getScore() + player.getPiao());
                userScore.put(player1.getPlayuser(), userScore.get(player1.getPlayuser()) - player1.getPiao());
                returnResult.setScore(returnResult.getScore() + player1.getPiao());
            }
        }
    }


    private static int getdajiangSize(List<Byte> collections, List<Action> actions, byte[] powerful, List<Byte> coverCards){

        int size = 0;
        AbsCheckScoreRule scoreRule = new HYSValidate();
        scoreRule.setData(collections, actions, powerful);
        if(scoreRule.isSatisfy()){
            size ++;
        }

        KDJValidate kdjValidate = new KDJValidate();
        kdjValidate.setData(collections, actions, powerful,coverCards);
        size = size + kdjValidate.isSatisfy();
        return size;
    }



    /**
     *
     * @param userScore
     * @param player
     * @param sc
     */
    private static void otherPlayerScore(Map<String, Integer> userScore,Player player,int sc,ReturnResult returnResult,Player[] players) {


        for (Map.Entry<String, Integer> score : userScore.entrySet()) {
            // 进来的都是赢了的用户
            if (!score.getKey().equals(player.getPlayuser())) {
                if (player.isBanker()) {
                    //算庄自己的票
                    userScore.put(score.getKey(), score.getValue() - player.getPiao());
                    returnResult.setScore(returnResult.getScore() + player.getPiao());
                    for(Player player1 : players){
                        //算对方的票
                        if(player1.getPlayuser().equals(score.getKey())) {
                            userScore.put(score.getKey(), score.getValue() - player1.getPiao());
                            returnResult.setScore(returnResult.getScore() + player1.getPiao());
                        }
                    }
                }/*else if(player.isZm() && !player.isBanker()){

                    for(Player player1 : players){
                        if(player1.isBanker()) {
                            userScore.put(score.getKey(), score.getValue() - player.getPiao());
                            returnResult.setScore(returnResult.getScore() + player.getPiao());
                            userScore.put(score.getKey(), score.getValue() - player1.getPiao());
                            returnResult.setScore(returnResult.getScore() + player1.getPiao());
                        }
                    }
                }*/else if (!player.isZm() && StringUtils.isNotEmpty(player.getTargetUser()) && score.getKey().equals(player.getTargetUser())) {  // target 表示点炮用户
                    userScore.put(score.getKey(), score.getValue() - player.getPiao());
                    returnResult.setScore(returnResult.getScore() + player.getPiao());
                    for(Player player1 : players){
                        if(player1.getPlayuser().equals(player.getTargetUser())) {
                            userScore.put(score.getKey(), score.getValue() - player1.getPiao());
                            returnResult.setScore(returnResult.getScore() + player1.getPiao());
                        }
                    }
                }
                userScore.put(score.getKey(), score.getValue() - sc);
            }
        }
    }



    private static String getGangAndPengHandler(Player[] players,String playWay,Player player,Map<String,Integer> userScore) {

        StringBuilder sb = new StringBuilder();
        for(Player tempPlayer : players) {
           if("koudajiang".equals(playWay)&&!player.isZm() && tempPlayer.getPlayuser().equals(player.getTargetUser())){
               continue;
           }
           List<Action> actions = tempPlayer.getActions();
           if (CollectionUtils.isEmpty(actions)) {
               continue;
           }
           int p = 0, ag = 0, mg = 0;
           for (Action action : actions) {
               if (action.getAction().equals(BMDataContext.PlayerAction.GANG.toString())) {
                   if (BMDataContext.PlayerGangAction.AN.toString().equals(action.getType())) {
                       ag++;
                   } else if (BMDataContext.PlayerGangAction.MING.toString().equals(action.getType())) {
                       if (userScore.containsKey(action.getSrcUserId())) {
                           userScore.put(action.getSrcUserId(), userScore.get(action.getSrcUserId()) - 3);
                       } else {
                           userScore.put(action.getSrcUserId(), -3);
                       }
                       mg++;
                   }
               } else if (BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())) {
                   p++;
               }
           }
           if (p > 0 && tempPlayer.isWin()) {
               sb.append("碰 " + p + " ");
           }
           if (ag > 0) {
               for (Map.Entry<String, Integer> entry : userScore.entrySet()) {
                   if (!actions.get(0).getUserid().equals(entry.getKey())) {
                       userScore.put(entry.getKey(), entry.getValue() - 2);
                   }
               }
               userScore.put(actions.get(0).getUserid(), userScore.get(actions.get(0).getUserid()) + (ag * 3 * 2));
               if(tempPlayer.isWin()) {
                   sb.append("暗杠(" + ag + ") × 2 × 3 =" + (ag * 3 * 2) + "分");
               }
           }
           if (mg > 0) {
               if(tempPlayer.isWin()) {
                   sb.append("明杠(" + mg + ") × 3 =" + (mg * 3) + "分").toString();
               }
               userScore.put(actions.get(0).getUserid(), userScore.get(actions.get(0).getUserid()) + mg * 3);
           }
       }
        return sb.toString();
    }


    public static void main(String[] args) {

        Player[] players = new Player[4];
        Player player1 = new Player("1");
        Player player2 = new Player("2");
        Player player3 = new Player("3");
        Player player4 = new Player("4");
        Action action = new Action("1",BMDataContext.PlayerAction.PENG.toString(),(byte)82);
        Action action3 = new Action("1",BMDataContext.PlayerAction.PENG.toString(),(byte)82);
        Action action2 = new Action("1",BMDataContext.PlayerAction.GANG.toString(),(byte)58);
        action2.setType(BMDataContext.PlayerGangAction.AN.toString());
        action2.setSrcUserId("2");
        action.setSrcUserId("2");

        List<Action> actions = new ArrayList<Action>();
        actions.add(action);
        actions.add(action3);
        player1.setActions(actions);

        players[0] = player1;
        players[1] = player2;
        players[2] = player3;
        players[3] = player4;

        byte[] b1 = new byte[]{11,43,54,68,10,15,9};
        List<List<Byte>> collections = new ArrayList<List<Byte>>();
        List<Byte> collection = new ArrayList<Byte>();
        for(byte b : b1){
            collection.add(b);
        }
        collections.addAll(userHandler());
        player1.setCollections(collections);
        player1.setCards(b1);
        List<Byte> cover = new ArrayList<Byte>();
        cover.add((byte)1);
        cover.add((byte)2);
        cover.add((byte)5);
        cover.add((byte)6);
        player1.setCoverCards(cover);
        player1.setBanker(true);
        player1.setZm(true);
        player1.setTargetUser("2");
        player1.setWin(true);
        GameRoom gameRoom = new GameRoom();
        gameRoom.setPiao(2);
        List<ReturnResult> returnResults = validateHu(players,gameRoom,"2",null);
       System.out.println(JSONObject.toJSONString(returnResults));

    }


    private static List<List<Byte>> userHandler() {
        byte[] cards = new byte[]{11,43,54,68,10,15,9};
        byte takecard = 86;

        List<Byte> test = new ArrayList<Byte>();
        for (byte temp : cards) {
            test.add(temp);
        }
        test.add(takecard);
        Collections.sort(test);
        for (byte temp : test) {
            int value = (temp % 36) / 4;            //牌面值
            int rote = temp / 36;                //花色
            System.out.print(value + 1);
            if (rote == 0) {
                System.out.print("万,");
            } else if (rote == 1) {
                System.out.print("筒,");
            } else if (rote == 2) {
                System.out.print("条,");
            }
        }
        Player player = new Player("USER1");
        player.setColor(2);
        byte[] powerfull = new byte[3];
        powerfull[0] = 11;
        powerfull[1] = 9;
        powerfull[2] = 15;
        player.setPowerfull(powerfull);
        List<Action> actions = new ArrayList<Action>();

        Action playerAction2 = new Action("adfaf2", BMDataContext.PlayerAction.PENG.toString(), (byte) 58);
        Action playerAction3 = new Action("adfaf4", BMDataContext.PlayerAction.PENG.toString(), (byte) 82);
        actions.add(playerAction2);
        actions.add(playerAction3);

        player.setCards(cards);
        player.setActions(actions);

        List<List<Byte>> collections = new ArrayList<List<Byte>>();
        MJCardMessage mjCardMessage = GameUtils.processLaiyuanMJCardResult(player, cards, takecard, false, collections, "majiang");
        System.out.println(JSONObject.toJSONString(mjCardMessage));

        if (mjCardMessage.isHu()) {
            for (List<Byte> cds : collections) {
                System.out.println("=============================");
                for (Byte card : cds) {
                    System.out.print(card + ",");
                }
                System.out.println();
                System.out.println("=============================");
            }
            System.out.println("+++++++++++++++++++++++++");
            for (Byte card : player.getCardsArray()) {
                System.out.print(card + ",");
            }
            System.out.println();
            System.out.println("++++++++++++++++++++");
        }

        return collections;

    }


}


