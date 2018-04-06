package com.beimi.core.engine.game.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.beimi.util.rules.model.MaJiangBoard;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.impl.Banker;
import com.beimi.core.engine.game.impl.UserBoard;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  设置庄
 *  发牌操作
 */
public class CreateBeginTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public CreateBeginTask(long timer , GameRoom gameRoom, String orgi){
		super();
		this.timer = timer ;
		this.gameRoom = gameRoom ;
		this.orgi = orgi ;
	}
	@Override
	public long getCacheExpiryTime() {
		return System.currentTimeMillis()+timer*1000;	//5秒后执行
	}

	public void execute(){
		List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi) ;
		/**
		 *
		 * 顺手 把牌发了，注：此处应根据 GameRoom的类型获取 发牌方式
		 */
		boolean inroom = false; //初始设置当前不是新开房间
		if(!StringUtils.isBlank(gameRoom.getLastwinner())){
			for(PlayUserClient player : playerList){
				if(player.getId().equals(gameRoom.getLastwinner())){
					inroom = true ;
				}
			}
		}
		if(inroom == false){//如果是新开房间 设置第一个进入房间的人为庄
			gameRoom.setLastwinner(playerList.get(0).getId());
		}
		/**
		 * 通知所有玩家 新的庄
		 */
		GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), orgi);
		if(!"koudajiang".equals(gamePlayway.getCode())) {
			ActionTaskUtils.sendEvent("banker", new Banker(gameRoom.getLastwinner()), gameRoom);
		}

		Board board = GameUtils.playGame(playerList, gameRoom, gameRoom.getLastwinner(), gameRoom.getCardsnum()) ;
		CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());
		if ("koudajiang".equals(gamePlayway.getCode())) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (!koudajiangHandler(board, playerList)) {
						logger.error("发牌失败");
					}
				}
			}).start();
		}else {
			for (Object temp : playerList) {
				PlayUserClient playerUser = (PlayUserClient) temp;
				playerUser.setGamestatus(BMDataContext.GameStatusEnum.PLAYING.toString());
				/**
				 * 更新状态到 PLAYING
				 */
				if (CacheHelper.getApiUserCacheBean().getCacheObject(playerUser.getId(), playerUser.getOrgi()) != null) {
					CacheHelper.getApiUserCacheBean().put(playerUser.getId(), playerUser, orgi);
				}
				/**
				 * 每个人收到的 牌面不同，所以不用 ROOM发送广播消息，而是用 遍历房间里所有成员发送消息的方式
				 */
				ActionTaskUtils.sendEvent(playerUser, new UserBoard(board, playerUser.getId(), "play", gameRoom.getNumofgames(), gameRoom.getCurrentnum(),false,null));
			}
		}

		CacheHelper.getGameRoomCacheBean().put(gameRoom.getId(), gameRoom, gameRoom.getOrgi());


		/**
		 * 发送一个 Begin 事件
		 *
		 * 跳出选色逻辑
		 */
		//super.getGame(gameRoom.getPlayway(), orgi).change(gameRoom , BeiMiGameEvent.AUTO.toString() , 2);	//通知状态机 , 此处应由状态机处理异步执行
		super.getGame(gameRoom.getPlayway(), orgi).change(gameRoom , BeiMiGameEvent.RAISEHANDS.toString() , 2);	//通知状态机 , 此处应由状态机处理异步执行
	}


	private boolean koudajiangHandler(Board board,List<PlayUserClient> playerList){

		for (PlayUserClient playerUser : playerList) {
			playerUser.setGamestatus(BMDataContext.GameStatusEnum.PLAYING.toString());
			if (CacheHelper.getApiUserCacheBean().getCacheObject(playerUser.getId(), playerUser.getOrgi()) != null) {
				CacheHelper.getApiUserCacheBean().put(playerUser.getId(), playerUser, orgi);
			}
		}


		MaJiangBoard tempBoard = generateTempBoard((MaJiangBoard) board);
		for(int i = 0;i<4;i++) {
			Map<String,byte[]> tempMap = new HashMap<String,byte[]>();
			for (int j = 0;j< playerList.size();j++) {
				PlayUserClient playerUser = playerList.get(j);
				byte[] b = null;
				if(playerUser.getId().equals(tempBoard.getBanker())) {
					b = new byte[ i < 3 ? 4 : 2];
					System.arraycopy(getCards(playerUser, board), i * 4, b, 0, i < 3 ? 4 : 2);
					tempMap.put(playerUser.getId(),b);
				}else{
					b = new byte[ i < 3 ? 4 : 1];
					System.arraycopy(getCards(playerUser, board), i * 4, b, 0, i < 3 ? 4 : 1);
					tempMap.put(playerUser.getId(),b);
				}

				UserBoard userBoard = new UserBoard(tempBoard, playerUser.getId(), "play", gameRoom.getNumofgames(), gameRoom.getCurrentnum(),true,b);
				userBoard.setPlayway("2");
				userBoard.setDeskcards(136 - (i<3?16*(i+1):5));
				logger.info("扣大将第num:{}发牌b:{}",i,b.length);
				ActionTaskUtils.sendEvent(playerUser, userBoard);
			}
			long startTime = System.currentTimeMillis();
			while(true) {

				if((System.currentTimeMillis() - startTime)/1000 > 600){
					return false;
				}
				boolean isQi = true;
				if(((MaJiangBoard) board).getAnswer().size() < 4){
					isQi = false;
				}else {

					for (Map.Entry<String, Integer> entry : ((MaJiangBoard) board).getAnswer().entrySet()) {
						if(entry.getValue() == (i+1)){
							if(!tempMap.containsKey(entry.getKey())){
								continue;
							}
							for(Player player : ((MaJiangBoard) board).getPlayers()){
								if(!player.getPlayuser().equals(entry.getKey())){
									continue;
								}
								for(byte _b : tempMap.get(entry.getKey())){
									if(player.getCoverCards() == null){
										player.setCoverCards(new ArrayList<Byte>());
									}
									player.getCoverCards().add(_b);
									tempMap.remove(entry.getKey());
								}
							}
						}
						if ((entry.getValue() != (i + 1) || entry.getValue() > 4 || entry.getValue() <= 0)&& entry.getValue() != -1) {
							isQi = false;
						}
					}
				}
				if(System.currentTimeMillis()%2 == 0) {
					logger.info("tid:{}校验客户端相应信息 isQi:{},answer:{}", startTime, isQi, JSONObject.toJSONString(((MaJiangBoard) board).getAnswer()));
				}
				if(!isQi){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					break;
				}
			}

		}
		ActionTaskUtils.sendEvent("banker", new Banker(gameRoom.getLastwinner()), gameRoom);
		((MaJiangBoard)board).setFPEnd(true);
		return true;
	}

	private byte[] getCards(PlayUserClient playUserClient,Board board){

		for(Player player : board.getPlayers()){
			if(playUserClient.getId().equals(player.getPlayuser())){
				return player.getCardsArray();
			}
		}
		return null;
	}

	/**
	 *
	 * @param board
	 * @return
     */
	private MaJiangBoard generateTempBoard(MaJiangBoard board){

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
		return temp;
	}


}
