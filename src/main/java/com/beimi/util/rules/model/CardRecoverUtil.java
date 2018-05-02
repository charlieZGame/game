package com.beimi.util.rules.model;

import com.beimi.core.engine.game.impl.UserBoard;
import com.beimi.core.engine.game.state.GameEvent;
import com.beimi.util.server.handler.BeiMiClient;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import org.apache.commons.collections4.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/13.
 */
public class CardRecoverUtil {


    /**
     * 扣
     * @param currentPlayer
     * @param board
     * @param gameRoom
     * @return
     */
    public static RecoveryData kouRecoverHandler(Player currentPlayer,Board board, GameRoom gameRoom) {

        MaJiangBoard tempBoard = generateTempBoard((MaJiangBoard) board);
        int number = tempBoard.getNumber() == 1 ? 1 : tempBoard.getNumber() - 1;
        int answerNum = 0;
        if (((MaJiangBoard) board).getAnswer() != null && !((MaJiangBoard) board).getAnswer().isEmpty() && ((MaJiangBoard) board).getAnswer().containsKey(currentPlayer.getPlayuser())) {
            answerNum = ((MaJiangBoard) board).getAnswer().get(currentPlayer.getPlayuser());
        }
        byte[] b = null;
        // if (currentPlayer.getPlayuser().equals(board.getBanker())) {
        b = new byte[number < 3 ? number * 4 : currentPlayer.getCardsArray().length];
        System.arraycopy(currentPlayer.getCardsArray(), 0, b, 0, b.length);
        //tempMap.put(playerUser.getId(), b);
     /*   } else {
            b = new byte[number < 3 ? number * 4 : currentPlayer.getCardsArray().length];
            System.arraycopy(currentPlayer.getCardsArray(), 0, b, 0, b.length);
            // tempMap.put(playerUser.getId(), b);
        }*/

        if (CollectionUtils.isNotEmpty(currentPlayer.getCoverCards())) {
            if (currentPlayer.getCoverCards().size() == currentPlayer.getCardsArray().length) {
                b = new byte[0];
            } else {
                byte[] _b = new byte[b.length - currentPlayer.getCoverCards().size()];
                int i = 0;
                for (byte tempb : b) {
                    boolean isExit = false;
                    for (byte c : currentPlayer.getCoverCards()) {
                        if (tempb == c) {
                            isExit = true;
                        }
                    }
                    if (!isExit) {
                        _b[i] = tempb;
                        i++;
                    }
                }
                b = _b;
            }
        }
        UserBoard userBoard = new UserBoard(tempBoard, currentPlayer.getPlayuser(), "recovery", gameRoom.getNumofgames(), gameRoom.getCurrentnum(), true, b);
        userBoard.setPlayway("2");
        userBoard.setDeskcards(136 - (number < 3 ? 16 * (number) : 16 * number + 5));
        return new RecoveryData(userBoard, currentPlayer, board.getLasthands(), board.getNextplayer() != null ?
                board.getNextplayer().getNextplayer() : null, 25, false, tempBoard, answerNum);
    }



    /**
     *
     * @param tid
     * @param board
     * @param currentPlayer
     */
    public static RecoveryData hunHandler(long tid, MaJiangBoard board, Player currentPlayer, GameRoom gameRoom) {
        UserBoard userBoard = new UserBoard(board, currentPlayer.getPlayuser(), "recovery", gameRoom.getNumofgames(), gameRoom.getCurrentnum(), false, currentPlayer.getCardsArray());
        return new RecoveryData(currentPlayer, userBoard, board.getLasthands(), board.getNextplayer() != null ? board.getNextplayer().getNextplayer() : null, 25, false, board);
    }


    public static List<Byte> listByteCopy(List<Byte> src, List<Byte> des){

        if(CollectionUtils.isEmpty(src)){
            return des;
        }
        if(CollectionUtils.isEmpty(des)){
            des = new ArrayList<Byte>();
        }
        for(Byte b : src){
            des.add(b);
        }
        return des;
    }




   /* private static byte[] getCards(PlayUserClient playUserClient,Board board){

        for(Player player : board.getPlayers()){
            if(playUserClient.getId().equals(player.getPlayuser())){
                return player.getCardsArray();
            }
        }
        return null;
    }
*/

    /**
     *
     * @param board
     * @return
     */
    public static MaJiangBoard generateTempBoard(MaJiangBoard board){

        MaJiangBoard temp = new MaJiangBoard();
        temp.setCommand(board.getCommand());
        temp.setAdded(board.isAdded());
        temp.setBanker(board.getBanker());
        temp.setCurrcard(board.getCurrcard());
        temp.setDeskcards(board.getDeskcards());
        temp.setCurrplayer(board.getCurrplayer());
        temp.setDocatch(board.isDocatch());
        temp.setFinished(board.isFinished());
        temp.setCommand(board.getCommand());
        temp.setHistory(board.getHistory());
        temp.setId(board.getId());
        temp.setCurrcard(board.getCurrcard());
        temp.setLast(board.getLast());
        temp.setNextplayer(board.getNextplayer());
        temp.setPlayers(board.getPlayers());
        temp.setLasthands(board.getLasthands());
        temp.setRatio(board.getRatio());
        temp.setRoom(board.getRoom());
        temp.setWinner(board.getWinner());
        temp.setNumber(board.getNumber());
        return temp;
    }




}
