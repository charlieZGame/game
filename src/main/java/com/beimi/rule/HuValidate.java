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

import java.util.*;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public class HuValidate {



    private static List<ICheckScoreRule> checkScoreRuleList;

    static {
        checkScoreRuleList = new ArrayList<ICheckScoreRule>();
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
                coverSize = coverSize + getdajiangSize(player);

            }

            ReturnResult allTempReturnResult = null;
            for (List<Byte> collection : player.getCollections()) {
                for (Map.Entry<String, Integer> entry : userScore.entrySet()) {
                    userScore.put(entry.getKey(), 0);
                }
                int i = 0;
                StringBuilder vaStr = new StringBuilder();
                for (ICheckScoreRule checkScoreRule : checkScoreRuleList) {
                    if("koudajiang".equals(playway.getCode()) && checkScoreRule instanceof WHHValidate){
                        continue;
                    }
                    checkScoreRule.setData(collection, player.getActions(), player.getPowerfullArray());
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
                }
                map.put(player.getPlayuser(), i);
                winUser = player.getPlayuser();
                ReturnResult returnResult = new ReturnResult();
                returnResult.setWin(true);
                returnResult.setCollections(collection);
                StringBuilder sb = new StringBuilder();
                //庄票处理
                System.out.println("票数量["+gameRoom.getPiao()+"]");
                if (gameRoom.getPiao() != null && gameRoom.getPiao() > 0) {
                    if (player.isBanker()) {
                        sb.append("飘" + gameRoom.getPiao() + " × 3 =" + gameRoom.getPiao() * 3 + "分 ");
                        returnResult.setScore(gameRoom.getPiao() * 3);
                    } else {
                        sb.append("飘" + gameRoom.getPiao() + " × 1 =" + gameRoom.getPiao() + "分 ");
                        returnResult.setScore(gameRoom.getPiao());
                    }
                }
                if (player.isZm()) {
                    if (i == 1) {
                        //老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        returnResult.setDesc(sb.append(" 老龙(自摸" + vaStr.toString() + ") ").toString());
                        if(player.isBanker()){
                            returnResult.setScore(36 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 12,returnResult,players);
                        }else{
                            returnResult.setScore(32 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 10,returnResult,players);
                            for(Player player1 : players){
                                if(player1.isBanker()){
                                    userScore.put(player1.getPlayuser(),userScore.get(player1.getPlayuser())-2);
                                }
                            }
                        }
                    } else if (i == 2) {
                        //老老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        returnResult.setDesc(sb.append(" 老老龙(自摸" + vaStr.toString() + ") ").toString());
                        if(player.isBanker()){
                            returnResult.setScore(66 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 22,returnResult,players);
                        }else{
                            returnResult.setScore(62 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 20,returnResult,players);
                            for(Player player1 : players){
                                if(player1.isBanker()){
                                    userScore.put(player1.getPlayuser(),userScore.get(player1.getPlayuser())-2);
                                }
                            }
                        }
                    }else if(i >= 3){
                        //老老老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        returnResult.setDesc(sb.append(" 老老老龙(自摸" + vaStr.toString() + ") ").toString());
                        if(player.isBanker()) {
                            returnResult.setScore(126 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 42,returnResult,players);
                        }else{
                            returnResult.setScore(122 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 40,returnResult,players);
                            for(Player player1 : players){
                                if(player1.isBanker()){
                                    userScore.put(player1.getPlayuser(),userScore.get(player1.getPlayuser())-2);
                                }
                            }
                        }
                    } else {
                        // 自摸 篓子每一家两分
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        if(player.isBanker()) {
                            returnResult.setScore(12 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 4,returnResult,players);
                        }else{
                            returnResult.setScore(8 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            otherPlayerScore(userScore, player, 2,returnResult,players);
                            for(Player player1 : players){
                                if(player1.isBanker()){
                                    userScore.put(player1.getPlayuser(),userScore.get(player1.getPlayuser())-2);
                                }
                            }
                        }
                        returnResult.setDesc(sb.append(" 赢(自摸) ").toString());
                    }
                } else {
                    if (i == 1) {
                        //少龙
                        // 自摸 篓子每一家两分
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        returnResult.setDesc(sb.append(" 少龙(" + vaStr.toString() + ") ").toString());
                        if(player.isBanker()) {
                            returnResult.setScore(19 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 19);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }else{
                            returnResult.setScore(17 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 17);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }
                    } else if (i == 2) {
                        //老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        returnResult.setDesc(sb.append(" 老龙(" + vaStr.toString() + ") ").toString());
                        if(player.isBanker()) {
                            returnResult.setScore(36 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 36);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }else{
                            returnResult.setScore(32 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 32);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }
                    }else if(i >= 3){
                        //老老龙
                        returnResult.setUserId(player.getPlayuser());
                        String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                        sb.append(pengGangResult == null ? "" : pengGangResult);
                        returnResult.setDesc(sb.append(" 老老龙(" + vaStr.toString() + ")").toString());
                        if(player.isBanker()) {
                            returnResult.setScore(66 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 66);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }else{
                            returnResult.setScore(62 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 62);
                            otherPlayerScore(userScore, player, 0, returnResult, players);
                        }
                    } else {
                        //坎子
                        try {
                            returnResult.setUserId(player.getPlayuser());
                            String pengGangResult = getGangAndPengHandler(player.getActions(), true, userScore);
                            //// TODO: 2018/4/22  ZCL菽粟计算再是有问题
                            if(player.isBanker()) {
                                userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 8);
                                returnResult.setScore(8 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            }else{
                                userScore.put(player.getTargetUser(), userScore.get(player.getTargetUser()) - 5);
                                returnResult.setScore(5 + userScore.get(player.getPlayuser()) + returnResult.getScore());
                            }
                            sb.append(pengGangResult == null ? "" : pengGangResult);
                            returnResult.setDesc(sb.append(" 赢(点炮) ").toString());
                            // 坎子点炮的人全出
                            otherPlayerScore(userScore, player, 0,returnResult,players);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (allTempReturnResult == null) {
                    allTempReturnResult = returnResult;
                    allUserScore = userScore;
                } else {
                    if (allTempReturnResult.getScore() < returnResult.getScore()) {
                        allTempReturnResult = returnResult;
                        allUserScore = userScore;
                    }
                }
            }
            //庄扣处理
            if(coverSize > 0){
                allTempReturnResult.setScore(allTempReturnResult.getScore()*(coverSize*2));
                allTempReturnResult.setDesc(allTempReturnResult.getDesc() + "扣["+coverSize+"] 总分[" + allTempReturnResult.getScore() + "]");
            }else {
                allTempReturnResult.setDesc(allTempReturnResult.getDesc() + "总分[" + allTempReturnResult.getScore() + "]");
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
                    returnResult.setScore(returnResult.getScore()*(coverSize*2));
                }
                returnResult.setDesc("失败 输[" + returnResult.getScore() + "]分");
                returnResults.add(returnResult);
            }
        }

        return returnResults;
    }


    private static int getdajiangSize(Player player){


        return 0;

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
                    userScore.put(score.getKey(), score.getValue() - player.getPiao());
                    returnResult.setScore(returnResult.getScore() + player.getPiao());
                    for(Player player1 : players){
                        if(player1.getPlayuser().equals(score.getKey())) {
                            userScore.put(score.getKey(), score.getValue() - player1.getPiao());
                            returnResult.setScore(returnResult.getScore() + player1.getPiao());
                        }
                    }

                } else if (score.getKey().equals(player.getTargetUser())) {
                    userScore.put(score.getKey(), score.getValue() - player.getPiao());
                    returnResult.setScore(returnResult.getScore() + player.getPiao());
                    for(Player player1 : players){
                        if(player1.getPlayuser().equals(score.getKey())) {
                            userScore.put(score.getKey(), score.getValue() - player1.getPiao());
                            returnResult.setScore(returnResult.getScore() + player1.getPiao());
                        }
                    }
                }
                userScore.put(score.getKey(), score.getValue() - sc);
            }
        }
    }



    private static String getGangAndPengHandler(List<Action> actions,boolean isNeedScore,Map<String,Integer> userScore) {

        if (CollectionUtils.isEmpty(actions)) {
            return null;
        }
        int p = 0, ag = 0, mg = 0;
        for (Action action : actions) {
            if (action.getAction().equals(BMDataContext.PlayerAction.GANG.toString())) {
                if (BMDataContext.PlayerGangAction.AN.toString().equals(action.getType())) {
                    ag++;
                } else if (BMDataContext.PlayerGangAction.MING.toString().equals(action.getType())) {
                    if(userScore.containsKey(action.getSrcUserId())){
                        userScore.put(action.getSrcUserId(),userScore.get(action.getSrcUserId())-3);
                    }else{
                        userScore.put(action.getSrcUserId(),-3);
                    }
                    mg++;
                }
            } else if (BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())) {
                p++;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (p > 0) {
            sb.append("碰 " + p + " ");
        }
        if (ag > 0) {
            for(Map.Entry<String,Integer> entry:userScore.entrySet()){
                if(!actions.get(0).getUserid().equals(entry.getKey())) {
                    userScore.put(entry.getKey(), entry.getValue() - 2);
                }
            }
            userScore.put(actions.get(0).getUserid(),userScore.get(actions.get(0).getUserid())+(ag * 3 * 2));
            sb.append("暗杠(" + ag + ") × 2 × 3 ="+(ag * 3 * 2)+"分");
        }
        if (mg > 0) {
            sb.append("明杠("+mg+") × 3 ="+(mg * 3)+"分").toString();
            userScore.put(actions.get(0).getUserid(),userScore.get(actions.get(0).getUserid())+ mg * 3);
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


