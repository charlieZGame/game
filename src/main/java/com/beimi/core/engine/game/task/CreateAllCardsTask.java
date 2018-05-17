package com.beimi.core.engine.game.task;

import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.util.GameUtils;
import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.model.Summary;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;

import java.util.ArrayList;
import java.util.List;


/**
 * 游戏结束 清理资源 回收房卡
 */
public class CreateAllCardsTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	
	public CreateAllCardsTask(long timer , GameRoom gameRoom, String orgi){
		super();
		this.timer = timer ;
		this.gameRoom = gameRoom ;
		this.orgi = orgi ;
	}
	@Override
	public long getCacheExpiryTime() {
		return 0l;	//5秒后执行
	}


	public void execute(){
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
		board.setFinished(true);
		String userId = board.getPlayers()[0].getPlayuser();
		GamePlayway gamePlayWay = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi()) ;
		boolean gameOver = false ;
		if(gamePlayWay!=null){
			/**
			 * 结算信息 ， 更新 玩家信息
			 */
			Summary summary = board.summary(board, gameRoom, gamePlayWay) ;


			sendEvent("allcards",  summary , gameRoom) ;	//通知所有客户端结束牌局，进入结算
			if(summary.isGameRoomOver()){
				gameOver = true ;
			}
			gameRoom.getPioaQi().clear();
		}
		for(Player player : board.getPlayers()){
			PlayUserClient playUserClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(player.getPlayuser(), this.orgi) ;
			if(playUserClient!=null){
				if(!playUserClient.getPlayertype().equals(BMDataContext.PlayerTypeEnum.AI.toString())){
					playUserClient.setGamestatus(BMDataContext.GameStatusEnum.NOTREADY.toString());
					CacheHelper.getApiUserCacheBean().put(playUserClient.getId(),playUserClient, gameRoom.getOrgi()) ;
					
					if(playUserClient.getPlayertype().equals(BMDataContext.PlayerTypeEnum.NORMAL.toString())){
						/**
						 * 历史遗留的问题，CacheHelper.getApiUserCacheBean()获取的是真实玩家的 数据，包括玩家的状态，CacheHelper.getGamePlayerCacheBean()存放的是包含机器人玩家的数据
						 */
						playUserClient = (PlayUserClient) CacheHelper.getGamePlayerCacheBean().getPlayer(player.getPlayuser(), this.orgi) ;
						if(playUserClient == null){
							continue;
						}
						playUserClient.setRoomready(false);
						CacheHelper.getGamePlayerCacheBean().put(playUserClient.getId(),playUserClient, gameRoom.getOrgi()) ;
					}else if(playUserClient.getPlayertype().equals(BMDataContext.PlayerTypeEnum.LEAVE.toString()) || playUserClient.getPlayertype().equals(BMDataContext.PlayerTypeEnum.OFFLINE.toString())){
						/**
						 * 离线和托管玩家，离开房间以后，牌局结束时从当前房间清理出去
						 */
						//ActionTaskUtils.updatePlayerClientStatus(playUserClient, playUserClient.getPlayertype(),true);
					}
				}
			}
		}
		
		if(gameOver){
			CacheHelper.getGamePlayerCacheBean().clean(gameRoom.getId() ,orgi) ;
			for(Player player : board.getPlayers()){
				CacheHelper.getGameRoomCacheBean().delete(player.getPlayuser(), gameRoom.getOrgi()) ;
				CacheHelper.getRoomMappingCacheBean().delete(player.getPlayuser(), this.orgi) ;
				
			}
			if(gameRoom.isCardroom() == false){ //房卡模式，清理掉房卡资源
				/**
				 * 重新加入房间资源到 队列
				 */
				CacheHelper.getQueneCache().put(gameRoom, gameRoom.getOrgi());
			}
		}
		
		BMDataContext.getGameEngine().finished(gameRoom.getId(), orgi);
		if(gameRoom.getCurrentnum() == gameRoom.getNumofgames()+1) {
			GameUtils.updatePlayerClientStatus(userId, gameRoom.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString(), true);
		}
		CacheHelper.getBoardCacheBean().delete(gameRoom.getId(),gameRoom.getOrgi());

	}
}
