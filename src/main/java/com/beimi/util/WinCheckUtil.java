package com.beimi.util;

import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.util.rules.model.MaJiangWinResult;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;

/**
 * Created by zhengchenglei on 2018/3/23.
 */
public class WinCheckUtil {


    /**
     *
     * @param gamePlayway
     * @param player
     * @return
     */
    public static MJCardMessage checkWin(GamePlayway gamePlayway, Player player) {


        if (player.getCardsArray().length < 14) {
            return null;
        }

        byte temp = 0;
        byte[] tempCard = player.getCardsArray();
        if (player.getCardsArray().length == 14) {
            temp = player.getCardsArray()[13];
            byte[] b = new byte[13];
            System.arraycopy(player.getCardsArray(), 0, b, 0, 13);
            player.setCards(b);
        }
        MJCardMessage mjCard = GameUtils.processMJCard(player, player.getCardsArray(), temp, false, null);
        mjCard.setDeal(false);
        mjCard.setTakeuser(player.getPlayuser());
        player.setCards(tempCard);
        return mjCard;
    }

    /**
     *
     * @param gamePlayway
     * @param player
     * @param winResult
     * @return
     */
    public static boolean checkWin(GamePlayway gamePlayway, Player player, MaJiangWinResult winResult){

        if(GameTypeEnum.LAIYUAN_HUN.getKey().equals(gamePlayway)){

        }else if(GameTypeEnum.LAIYUAN_KOU.getKey().equals(gamePlayway)){

        }else{
            return false;
        }

        return false;

    }


}
