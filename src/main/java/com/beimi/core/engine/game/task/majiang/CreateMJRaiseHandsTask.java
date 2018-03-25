package com.beimi.core.engine.game.task.majiang;

import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.util.GameTypeEnum;
import com.beimi.util.WinCheckUtil;
import com.beimi.util.rules.model.*;
import com.beimi.web.model.GamePlayway;
import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.GameBoard;
import com.beimi.core.engine.game.task.AbstractTask;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;

/**
 * 判断人员是否选缺，如果没有选缺 系统自动帮他选缺， 选缺的规则是 玩家手里牌最少的一个
 *
 * 设置下一个出牌玩家是庄，且庄不需要取牌
 *
 */
public class CreateMJRaiseHandsTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	
	public CreateMJRaiseHandsTask(long timer , GameRoom gameRoom, String orgi){
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
		/**
		 * 
		 * 检查是否所有人都已经定缺，如果定缺完毕，则通知庄家开始出牌，如果有未完成定缺的，则自动选择
		 */
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
		Player banker = null ;
		for(Player player : board.getPlayers()){
			if(player.getPlayuser().equals(board.getBanker())){
				banker = player ;
			}
			if(!player.isSelected()){ // 判断用户是否定缺
				SelectColor color = new SelectColor( board.getBanker(), player.getPlayuser()) ;
				color.setColor(GameUtils.selectColor(player.getCardsArray()));
				ActionTaskUtils.sendEvent("selectresult" , color , gameRoom);
				player.setColor(color.getColor()); 
				player.setSelected(true);break ;
			}
		}
		if(banker!=null) {
			board.setNextplayer(new NextPlayer(board.getBanker(), false));
			CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());    //更新缓存数据
			/**
			 * 发送一个通知，告诉大家 ， 开始出牌了
			 */
			sendEvent("lasthands", new GameBoard(banker.getPlayuser(), board.getBanker(), board.getRatio()), gameRoom);

			/**
			 * 更新牌局状态
			 */
			CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, orgi);


			/**
			 * 判断是否有天湖的情况
			 */
			GamePlayway gamePlayWay = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi());

			MJCardMessage mjCardMessage = WinCheckUtil.checkWin(gamePlayWay,banker);
			if (mjCardMessage != null && mjCardMessage.isHu()) {
				ActionTaskUtils.sendEvent(banker.getPlayuser(), mjCardMessage);
				//GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.ALLCARDS.toString(), 0);    //通知结算
				return;
			}


			/**
			 * 发送一个 开始打牌的事件 ， 判断当前出牌人是 玩家还是 AI，如果是 AI，则默认 1秒时间，如果是玩家，则超时时间是25秒
			 */
			PlayUserClient playUserClient = ActionTaskUtils.getPlayUserClient(gameRoom.getId(), banker.getPlayuser(), orgi);

			if (BMDataContext.PlayerTypeEnum.NORMAL.toString().equals(playUserClient.getPlayertype())) {
				// TODO: 2018/3/23 ZCL 这个是第一次开始打牌需要等待时间 需要从数据库读取
				super.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.PLAYCARDS.toString(), 8000);    //应该从 游戏后台配置参数中获取
			} else {
				super.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.PLAYCARDS.toString(), 3);    //应该从游戏后台配置参数中获取
			}
		}
	}
}
