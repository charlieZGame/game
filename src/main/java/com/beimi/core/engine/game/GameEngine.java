package com.beimi.core.engine.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import com.beimi.backManager.WEChartUtil;
import com.beimi.core.engine.game.impl.UserBoard;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.core.engine.game.model.Playway;
import com.beimi.util.cache.hazelcast.HazlcastCacheHelper;
import com.beimi.util.cache.hazelcast.impl.ProxyGameRoomCache;
import com.beimi.util.rules.model.*;
import com.beimi.web.model.PlayUser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.state.GameEvent;
import com.beimi.core.engine.game.task.majiang.CreateMJRaiseHandsTask;
import com.beimi.util.GameUtils;
import com.beimi.util.RandomCharUtil;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.util.server.handler.BeiMiClient;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.service.repository.es.PlayUserClientESRepository;
import com.beimi.web.service.repository.jpa.GameRoomRepository;
import com.corundumstudio.socketio.SocketIOServer;

@Service(value="beimiGameEngine")
public class GameEngine {
	
	@Autowired
	protected SocketIOServer server;

	@Resource
	private KieSession kieSession;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void gameRequest(String userid ,String playway , String room , String orgi , PlayUserClient userClient , BeiMiClient beiMiClient,String data ) {
		long tid = System.currentTimeMillis();
		GameEvent gameEvent = gameRequest(userClient.getId(), beiMiClient.getPlayway(), beiMiClient, beiMiClient.getOrgi(), userClient, data); //如果是新玩家，创建房间，加入队列等待
		if (gameEvent != null) {

			/**
			 * 举手了，表示游戏可以开始了
			 */
			if (userClient != null) {
				userClient.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
			}

			/**
			 * 游戏状态 ， 玩家请求 游戏房间，活动房间状态后，发送事件给 StateMachine，由 StateMachine驱动 游戏状态 ， 此处只负责通知房间内的玩家
			 * 1、有新的玩家加入
			 * 2、给当前新加入的玩家发送房间中所有玩家信息（不包含隐私信息，根据业务需求，修改PlayUserClient的字段，剔除掉隐私信息后发送）
			 */
			//JoinRoom joinRoom = new JoinRoom(userClient, gameEvent.getIndex(), gameEvent.getGameRoom().getPlayers(), gameEvent.getGameRoom());
			//logger.info("tid:{},userId:{} send JoinRoom data:{} ",tid,beiMiClient.getUserid(),JSONObject.toJSONString(joinRoom));
			ActionTaskUtils.sendEvent("joinroom", new JoinRoom(WEChartUtil.clonePlayUserClient(userClient), gameEvent.getIndex(), gameEvent.getGameRoom().getPlayers(), gameEvent.getGameRoom()), gameEvent.getGameRoom());
			//ActionTaskUtils.sendEvent("joinroom", joinRoom);
			/**
			 * 发送给单一玩家的消息
			 */
			ActionTaskUtils.sendPlayers(beiMiClient, gameEvent.getGameRoom());
			/**
			 * 当前是在游戏中还是 未开始
			 */
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameEvent.getRoomid(), gameEvent.getOrgi());
			if (board != null) {
				Player currentPlayer = null;
				for (Player player : board.getPlayers()) {
					if (player.getPlayuser().equals(userClient.getId())) {
						currentPlayer = player;
						break;
					}
				}
				if (currentPlayer != null) {
					boolean automic = false;
					// 停掉机器人操作
					/*if((board.getLast()!=null && board.getLast().getUserid().equals(currentPlayer.getPlayuser())) || (board.getLast() == null && board.getBanker().equals(currentPlayer.getPlayuser()))){
						automic = true ;
					}*/
					// 这个回复逻辑有问题，取牌有可能取错
					//ActionTaskUtils.sendEvent("recovery", new RecoveryData(currentPlayer, board.getLasthands(), board.getNextplayer() != null ? board.getNextplayer().getNextplayer() : null, 25, automic, board), gameEvent.getGameRoom());


					//暂时不需要，从getCurentCards 里取牌
					logger.info("tid:{} 恢复用户信息(JOINROOM) currentPlayer:{}",tid, currentPlayer.getPlayuser());
					String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userid, orgi);
					GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi);        //直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
					List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi) ;
					GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), orgi) ;

					if("koudajiang".equals(gamePlayway.getCode())){
						koudajiangHandler(tid,playerList,(MaJiangBoard)board,currentPlayer,beiMiClient,gameEvent);
					}else{
						RecoveryData recoveryData = CardRecoverUtil.hunHandler(tid,(MaJiangBoard)board,currentPlayer,gameEvent.getGameRoom());
						logger.info("tid:{} 恢复hun牌面信息发牌完成 cards:{}", tid, JSONObject.toJSONString(recoveryData));
						//beiMiClient.getClient().sendEvent("recovery",recoveryData, gameEvent.getGameRoom());
						beiMiClient.getClient().sendEvent("recovery",recoveryData);
					}
					if(CollectionUtils.isEmpty(currentPlayer.getActions()) && currentPlayer.getCardsArray().length == 14) {
						sendHandleMessage((MaJiangBoard) board, gamePlayway, currentPlayer, true,gameRoom);
					}else if(currentPlayer.getActions().size() * 3 + currentPlayer.getCardsArray().length == 14) {
						sendHandleMessage((MaJiangBoard) board, gamePlayway, currentPlayer, true,gameRoom);
					}else{
						sendHandleMessage((MaJiangBoard) board, gamePlayway, currentPlayer, false,gameRoom);
					}
				}
			} else {
				//通知状态 开局 新增选飘操作，人员够了选完飘才能开局
				//GameUtils.getGame(beiMiClient.getPlayway(), gameEvent.getOrgi()).change(gameEvent);    //通知状态机 , 此处应由状态机处理异步执行
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userid, orgi);
				GameRoom gameRoom  = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi);        //直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
				if(gameRoom.getPiao() == 0) {
					GameUtils.getGame(beiMiClient.getPlayway(), gameEvent.getOrgi()).change(gameEvent);    //通知状态机 , 此处应由状态机处理异步执行
				}else {
					List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi);
					if (CollectionUtils.isNotEmpty(playerList) && playerList.size() == 4) {
						logger.info("join 通知选票");
						ActionTaskUtils.sendEventCommand("selectPiao", "ok", gameRoom);
					}
				}
			}
		}
	}


	private void sendHandleMessage(MaJiangBoard board,GamePlayway gamePlayway,Player player,boolean isCurrentTurn,GameRoom gameRoom){

		if(isCurrentTurn) {
			byte card = player.getCardsArray()[player.getCardsArray().length - 1];
			byte[] tempB = new byte[player.getCardsArray().length - 1];
			System.arraycopy(player.getCardsArray(), 0, tempB, 0, tempB.length);
			player.setCards(tempB);
			MJCardMessage mjCard = board.checkMJCard(player, card, true, gamePlayway.getCode(),gameRoom.isAllowPeng());
			logger.info("恢复指令14 data:{}",mjCard);
			boolean hasAction = false;
			if (mjCard.isGang() || mjCard.isPeng() || mjCard.isChi() || mjCard.isHu()) {
				/**
				 * 通知客户端 有杠碰吃胡了
				 */
				hasAction = true;
				ActionTaskUtils.sendEvent(player.getPlayuser(), mjCard);
			}
			player.setCards(ArrayUtils.add(player.getCardsArray(), card));
			/**
			 * 抓牌 , 下一个玩家收到的牌里会包含 牌面，其他玩家的则不包含牌面  //todo ZCL主要是更新牌面
			 */
			for (Player temp : board.getPlayers()) {
				if (temp.getPlayuser().equals(player.getPlayuser())) {
					ActionTaskUtils.sendEvent("dealcard", temp.getPlayuser(), new DealCard(player.getPlayuser(), board.getDeskcards().size(), temp.getColor(), card, hasAction));
				} else {
					ActionTaskUtils.sendEvent("dealcard", temp.getPlayuser(), new DealCard(player.getPlayuser(), board.getDeskcards().size()));
				}
			}
		}else{

			if(board == null || board.getLast() == null || player.getPlayuser().equals(board.getNextplayer())){
				return;
			}

			MJCardMessage mjCard = board.checkMJCard(player, board.getLast().getCard(), false, gamePlayway.getCode(),gameRoom.isAllowPeng());
			logger.info("恢复指令13 data:{}",mjCard);
			if (mjCard.isGang() || mjCard.isPeng() || mjCard.isChi() || mjCard.isHu()) {
				/**
				 * 通知客户端 有杠碰吃胡了
				 */
				ActionTaskUtils.sendEvent(player.getPlayuser(), mjCard);
			}
		}
	}

	/**
	 *
	 * @param tid
	 * @param playerList
	 * @param board
	 * @param currentPlayer
	 * @param beiMiClient
     * @param gameEvent
     */
	private void koudajiangHandler(long tid,List<PlayUserClient> playerList,MaJiangBoard board,Player currentPlayer,BeiMiClient beiMiClient,GameEvent gameEvent){

		for (PlayUserClient player : playerList) {
			if (!currentPlayer.getPlayuser().equals(player.getId())) {
				continue;
			}
			RecoveryData recoveryData = null;
			if(board.isFPEnd()) {
				recoveryData = CardRecoverUtil.kouRecoverHandler(currentPlayer,board,gameEvent.getGameRoom());
				logger.info("tid:{},恢复kou牌面信息发牌完成 deskinfo:{}",tid,JSONObject.toJSONString(recoveryData));
			}else{
				recoveryData = CardRecoverUtil.kouRecoverHandler(currentPlayer,board,gameEvent.getGameRoom());
				logger.info("tid:{},恢复kou牌面信息发牌未完成 deskinfo:{}",tid,JSONObject.toJSONString(recoveryData));
			}
			//beiMiClient.getClient().sendEvent("recovery",recoveryData, gameEvent.getGameRoom());
			beiMiClient.getClient().sendEvent("recovery",recoveryData);
		}
	}



	public static boolean isExistProxy(String roomId){
		ProxyGameRoomCache cacheBean = (ProxyGameRoomCache) CacheHelper.getProxyGameRoomCache().getCacheInstance(HazlcastCacheHelper.CacheServiceEnum.ProxyGameRoomCache.toString());
		for (Object map : cacheBean.getInstance().values()) {
			if (((Map<String, String>) map).containsKey(roomId)) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * 玩家房间选择， 新请求，游戏撮合， 如果当前玩家是断线重连， 或者是 退出后进入的，则第一步检查是否已在房间
	 * 如果已在房间，直接返回
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public GameEvent gameRequest(String userid ,String playway , BeiMiClient beiMiClient , String orgi , PlayUserClient playUser,String data){
		GameEvent gameEvent = null ;

		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userid, orgi) ;
		logger.info("userId:{}获取roomid:{}",userid,roomid);
		GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(playway, orgi) ;
		boolean needtakequene = false;
		if(gamePlayway!=null){
			gameEvent = new GameEvent(gamePlayway.getPlayers() , gamePlayway.getCardsnum() , orgi) ;
			GameRoom gameRoom = null ;
			if(!StringUtils.isBlank(roomid) && CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi)!=null){//
				gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;		//直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
			}else{
				if(isExistProxy(roomid) && BMDataContext.BEIMI_SYSTEM_ROOM.equals(beiMiClient.getExtparams().get("gamemodel"))){
					gameRoom = this.creatGameRoom(gamePlayway, userid , true , beiMiClient,roomid) ;
				}else if(beiMiClient.getExtparams()!=null && BMDataContext.BEIMI_SYSTEM_ROOM.equals(beiMiClient.getExtparams().get("gamemodel"))){	//房卡游戏 , 创建ROOM
					gameRoom = this.creatGameRoom(gamePlayway, userid , true , beiMiClient,null) ;
				}else{	//
					/**
					 * 大厅游戏 ， 撮合游戏 , 发送异步消息，通知RingBuffer进行游戏撮合，撮合算法描述如下：
					 * 1、按照查找
					 * 
					 */
					gameRoom = (GameRoom) CacheHelper.getQueneCache().poll(playway , orgi) ;
					if(gameRoom != null){	
						/**
						 * 修正获取gameroom获取的问题，因为删除房间的时候，为了不损失性能，没有将 队列里的房间信息删除，如果有玩家获取到这个垃圾信息
						 * 则立即进行重新获取房价， 
						 */
						while(CacheHelper.getGameRoomCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) == null){
							gameRoom = (GameRoom) CacheHelper.getQueneCache().poll(playway , orgi) ;
							if(gameRoom == null){
								break ;
							}
						}
					}
					
					if(gameRoom==null){	//无房间 ， 需要
						gameRoom = this.creatGameRoom(gamePlayway, userid , false , beiMiClient,null) ;
					}else{
						playUser.setPlayerindex(System.currentTimeMillis());//从后往前坐，房主进入以后优先坐在 首位
						needtakequene =  true ;
					}
				}
			}
			if(gameRoom!=null){
				/**
				 * 设置游戏当前已经进行的局数
				 */
				setGameParam(data,gameRoom);
				// 更新混子

				/**
				 * 更新缓存
				 */
				CacheHelper.getGameRoomCacheBean().put(gameRoom.getId(), gameRoom, orgi);
				/**
				 * 如果当前房间到达了最大玩家数量，则不再加入到 撮合队列
				 */
				List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
				if(playerList == null || playerList.size() == 0){
					playerList = new ArrayList<PlayUserClient>();
					gameEvent.setEvent(BeiMiGameEvent.ENTER.toString());
				}else if(playerList.size() == gamePlayway.getPlayers()){
					gameEvent.setEvent(BeiMiGameEvent.ENOUGH.toString());
				}else{
					gameEvent.setEvent(BeiMiGameEvent.JOIN.toString());
				}
				gameEvent.setGameRoom(gameRoom);
				gameEvent.setRoomid(gameRoom.getId());
				
				/**
				 * 无条件加入房间
				 */
				this.joinRoom(gameRoom, playUser, playerList);
				
				for(PlayUserClient temp : playerList){
					if(temp.getId().equals(playUser.getId())){
						gameEvent.setIndex(playerList.indexOf(temp)); break ;
					}
				}
				/**
				 * 如果当前房间到达了最大玩家数量，则不再加入到 撮合队列
				 */
				if(playerList.size() < gamePlayway.getPlayers() && needtakequene == true){
					CacheHelper.getQueneCache().put(gameRoom, orgi);	//未达到最大玩家数量，加入到游戏撮合 队列，继续撮合
				}
				
			}
		}
		return gameEvent;
	}


	/**
	 *
	 * @param data
	 * @param gameRoom
     */
	public void setGameParam(String data,GameRoom gameRoom) {

		if (StringUtils.isNotEmpty(data)) {
			Map<String, Object> map = JSONObject.parseObject(data, Map.class);
			if (map != null && !map.isEmpty() && map.containsKey("extparams")) {
				JSONObject jsonObject = (JSONObject) map.get("extparams");
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("hun"))) {
					gameRoom.setPowerfulsize(Integer.parseInt(jsonObject.get("hun").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("jun"))) {
					gameRoom.setNumofgames(Integer.parseInt(jsonObject.get("jun").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("koujun"))) {
					gameRoom.setNumofgames(Integer.parseInt(jsonObject.get("koujun").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("hunfeng"))) {
					gameRoom.setWindow(Boolean.parseBoolean(jsonObject.get("hunfeng").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("koufeng"))) {
					gameRoom.setWindow(Boolean.parseBoolean(jsonObject.get("koufeng").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("hunpiao"))) {
					gameRoom.setPiao(Integer.parseInt(jsonObject.get("hunpiao").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("koupiao"))) {
					gameRoom.setPiao(Integer.parseInt(jsonObject.get("koupiao").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("koupiao"))) {
					gameRoom.setPiao(Integer.parseInt(jsonObject.get("koupiao").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("hunpeng"))) {
					gameRoom.setAllowPeng(Boolean.parseBoolean(jsonObject.get("hunpeng").toString()));
				}
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("koupeng"))) {
					gameRoom.setAllowPeng(Boolean.parseBoolean(jsonObject.get("koupeng").toString()));
				}
			}
		}
	}


	public static void main(String[] args) {
		System.out.println(Boolean.parseBoolean("false"));
	}


	/**
	 * 
	 * 玩家加入房间
	 * @param gameRoom
	 * @param playUser
	 * @param playerList
	 */
	public void joinRoom(GameRoom gameRoom , PlayUserClient playUser , List<PlayUserClient> playerList){
		boolean inroom = false ;
		for(PlayUserClient user : playerList){
			if(user.getId().equals(playUser.getId())){
				inroom = true ; break ;
			}
		}
		if(inroom == false){
			playUser.setPlayerindex(System.currentTimeMillis());
			playUser.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
			playUser.setPlayertype(BMDataContext.PlayerTypeEnum.NORMAL.toString());
			playUser.setRoomid(gameRoom.getId());
			playUser.setRoomready(false);

			playerList.add(playUser) ;
			NettyClients.getInstance().joinRoom(playUser.getId(), gameRoom.getId());
			//CacheHelper.getGamePlayerCacheBean().put(playUser.getId(), playUser, playUser.getOrgi()); //将用户加入到 room ， MultiCache
			CacheHelper.getGamePlayerCacheBean().put(gameRoom.getId(), playUser, gameRoom.getOrgi()); //将用户加入到 room ， MultiCache
			List<PlayUserClient> playerList1 = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			//CacheHelper.getRoomMappingCacheBean().put(playUser.getId(), orgi); ;

		}
		
		/**
		 *	不管状态如何，玩家一定会加入到这个房间 
		 */
		CacheHelper.getRoomMappingCacheBean().put(playUser.getId(), gameRoom.getId(), playUser.getOrgi());
	}
	
	/**
	 * 抢地主，斗地主
	 * @param roomid

	 * @param orgi
	 * @return
	 */
	public void actionRequest(String roomid, PlayUserClient playUser, String orgi , boolean accept){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			DuZhuBoard board = (DuZhuBoard) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			Player player = board.player(playUser.getId()) ;
			board = ActionTaskUtils.doCatch(board, player , accept) ;
			
			ActionTaskUtils.sendEvent("catchresult",new GameBoard(player.getPlayuser() , player.isAccept(), board.isDocatch() , board.getRatio()),gameRoom) ;
			GameUtils.getGame(gameRoom.getPlayway() , orgi).change(gameRoom , BeiMiGameEvent.AUTO.toString() , 15);	//通知状态机 , 继续执行
			
			CacheHelper.getBoardCacheBean().put(gameRoom.getId() , board , gameRoom.getOrgi()) ;
			
			CacheHelper.getExpireCache().put(gameRoom.getRoomid(), ActionTaskUtils.createAutoTask(1, gameRoom));
		}
	}
	
	/**
	 * 抢地主，斗地主
	 * @param roomid

	 * @param orgi
	 * @return
	 */
	public void startGameRequest(String roomid, PlayUserClient playUser, String orgi , boolean opendeal){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			playUser.setRoomready(true);
			if(opendeal == true){
				playUser.setOpendeal(opendeal);
			}
			
			CacheHelper.getGamePlayerCacheBean().put(playUser.getId(), playUser, playUser.getOrgi());
			ActionTaskUtils.roomReady(gameRoom, GameUtils.getGame(gameRoom.getPlayway() , gameRoom.getOrgi()));
			
			UKTools.published(playUser,BMDataContext.getContext().getBean(PlayUserClientESRepository.class));
			
			ActionTaskUtils.sendEvent(playUser.getId(), new Playeready(playUser.getId() , "playeready"));
		}
	}
	
	
	/**
	 * 抢地主，斗地主
	 * @param roomid

	 * @param orgi
	 * @return
	 */
	public void cardTips(String roomid, PlayUserClient playUser, String orgi , String cardtips){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			DuZhuBoard board = (DuZhuBoard) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			Player player = board.player(playUser.getId()) ;
			
			TakeCards takeCards = null ;
			
			if(!StringUtils.isBlank(cardtips)){
				String[] cards = cardtips.split(",") ;
				byte[] tipCards = new byte[cards.length] ;
				for(int i= 0 ; i<cards.length ; i++){
					tipCards[i] = Byte.parseByte(cards[i]) ;
				}
				takeCards = board.cardtip(player, board.getCardTips(player, tipCards)) ;
			}
			if(takeCards == null || takeCards.getCards() == null){
				if(board.getLast() != null && !board.getLast().getUserid().equals(player.getPlayuser())){	//当前无出牌信息，刚开始出牌，或者出牌无玩家 压
					takeCards = board.cardtip(player, board.getLast()) ;
				}else{
					takeCards = board.cardtip(player, null) ;
				}
			}
			
			if(takeCards.getCards() == null){
				takeCards.setAllow(false);	//没有 管的起的牌
			}
			ActionTaskUtils.sendEvent("cardtips", takeCards ,gameRoom) ;
		}
	}
	
	/**
	 * 出牌，并校验出牌是否合规  ZCL 自己主动出牌逻辑
	 * @param roomid
	 * 
	 * @param auto 是否自动出牌，超时/托管/AI会调用 = true
     *
	 * @param orgi
	 * @return
	 */
	public TakeCards takeCardsRequest(String roomid, String playUserClient, String orgi , boolean auto , byte[] playCards,boolean isAllowPG){// playCards 为打出去的牌
		TakeCards takeCards = null ;
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			if(board!=null){
				Player player = board.player(playUserClient) ;
				if(board.getNextplayer()!=null && player.getPlayuser().equals(board.getNextplayer().getNextplayer()) && board.getNextplayer().isTakecard() == false){
					takeCards = board.takeCardsRequest(gameRoom, board, player, orgi, auto, playCards,isAllowPG) ;
				}
			}
		}
		return takeCards ;
	}
	
	/**
	 * 检查是否所有玩家 都已经处于就绪状态，如果所有玩家都点击了 继续开始游戏，则发送一个 ALL事件，继续游戏，
	 * 否则，等待10秒时间，到期后如果玩家还没有就绪，就将该玩家T出去，等待新玩家加入
	 * @param roomid
	 * @return
	 */
	public void restartRequest(String roomid , PlayUserClient playerUser, BeiMiClient beiMiClient , boolean opendeal){
		long tid = System.nanoTime();
		logger.info("tid:{} restartRequest handler",tid);
		boolean notReady = false ;
		List<PlayUserClient> playerList = null ;
		GameRoom gameRoom = null ;
		if(!StringUtils.isBlank(roomid)){
			gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, playerUser.getOrgi()) ;
			playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
			if(playerList!=null && playerList.size() > 0){
				/**
				 * 有一个 等待 
				 */
				for(PlayUserClient player : playerList){
					if(player.isRoomready() == false){
						notReady = true ; break ;
					}
				}
			}
		}
		if(notReady == true && gameRoom!=null){
			/**
			 * 需要增加一个状态机的触发事件：等待其他人就绪，超过5秒以后未就绪的，直接踢掉，然后等待机器人加入
			 */
			this.startGameRequest(roomid, playerUser, playerUser.getOrgi(), opendeal);
		}else if(playerList == null || playerList.size() == 0 || gameRoom == null){//房间已解散
			BMDataContext.getGameEngine().gameRequest(playerUser.getId(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), playerUser , beiMiClient,null) ;
			/**
			 * 结算后重新开始游戏
			 */
			playerUser.setRoomready(true);
			CacheHelper.getGamePlayerCacheBean().put(playerUser.getId(), playerUser, playerUser.getOrgi());
		}
	}
	
	/**
	 * 出牌，并校验出牌是否合规
	 * @param roomid
	 *
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public SelectColor selectColorRequest(String roomid, String userid, String orgi , String color){
		SelectColor selectColor = null ;
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			if(board!=null){
				//超时了 ， 执行自动出牌
//				Player[] players = board.getPlayers() ;
				/**
				 * 检查是否所有玩家都已经选择完毕 ， 如果所有人都选择完毕，即可开始
				 */
				selectColor = new SelectColor(board.getBanker());
				if(!StringUtils.isBlank(color)){
					if(!StringUtils.isBlank(color) && color.matches("[0-2]{1}")){
						selectColor.setColor(Integer.parseInt(color));
					}else{
						selectColor.setColor(0);
					}
					selectColor.setTime(System.currentTimeMillis());
					selectColor.setCommand("selectresult");
					
					selectColor.setUserid(userid);
				}
				boolean allselected = true ;
				for(Player ply : board.getPlayers()){
					if(ply.getPlayuser().equals(userid)){
						if(!StringUtils.isBlank(color) && color.matches("[0-2]{1}")){
							ply.setColor(Integer.parseInt(color));
						}else{
							ply.setColor(0);
						}
						ply.setSelected(true);
					}
					if(!ply.isSelected()){
						allselected = false ;
					}
				}
				CacheHelper.getBoardCacheBean().put(gameRoom.getId() , board, gameRoom.getOrgi());	//更新缓存数据
				ActionTaskUtils.sendEvent("selectresult", selectColor , gameRoom);	
				/**
				 * 检查是否全部都已经 定缺， 如果已全部定缺， 则发送 开打 
				 */
				if(allselected){
					/**
					 * 重置计时器，立即执行
					 */
					CacheHelper.getExpireCache().put(gameRoom.getId(), new CreateMJRaiseHandsTask(1 , gameRoom , gameRoom.getOrgi()) );
					GameUtils.getGame(gameRoom.getPlayway() , orgi).change(gameRoom , BeiMiGameEvent.RAISEHANDS.toString() , 0);	
				}
			}
		}
		return selectColor ;
	}
	
	/**
	 * 麻将 ， 杠碰吃胡过
	 * @param roomid
	 * 
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public ActionEvent actionEventRequest(String roomid, String userid, String orgi , String action) {
		ActionEvent actionEvent = null;
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi);
		if (gameRoom != null) {
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			if (board != null) {
				Player player = board.player(userid);
				byte card = board.getLast().getCard();
				actionEvent = new ActionEvent(board.getBanker(), userid, card, action);
				if (!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.GUO.toString())) {
					/**
					 * 用户动作，选择 了 过， 下一个玩家直接开始抓牌 
					 * bug，待修复：如果有多个玩家可以碰，则一个碰了，其他玩家就无法操作了
					 *
					 * 点过 如果是当前用户就不出发下一个用户取牌，还是当前用户出牌，但是得通知其他用户当前用户过了
					 */
					if (!board.getNextplayer().getNextplayer().equals(userid)) {
						if (((MaJiangBoard) board).getHuController().size() > 0) {
							if (board instanceof MaJiangBoard) {
								synchronized (((MaJiangBoard) board).getHuController()) {
									MJCardMessage mjCardMessage = ((MaJiangBoard) board).getHuController().remove(userid);
									logger.info("userId:{} 过移除 mjCardMessage:{}",userid,mjCardMessage);
									if(mjCardMessage.isHu()){
										((MaJiangBoard) board).getCycleController().put(player.getPlayuser(),true);
									}
									((MaJiangBoard) board).getHuController().notifyAll();
								}
								logger.info("用户放弃杠碰胡牌1 userId:{}", userid);
							}
						}
						logger.info("userId:{} getHuController:{}",player.getPlayuser(),((MaJiangBoard) board).getHuController().size());
						if (((MaJiangBoard) board).getHuController().size() == 0) {
							//处理完上次的吃碰胡才能进行取牌操作
							board.dealRequest(gameRoom, board, orgi, false, null);
						}
					} else {

						// 如果是下一个用户是当前用户，说明 是自己取牌，这样的话就不用 通知下一个用户 取牌，必须自己先打出一张牌，同时也没有notify的情况
					}
				}else if (!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.PENG.toString()) && allowAction(card, player.getActions(), BMDataContext.PlayerAction.PENG.toString())) {
					Action playerAction = new Action(userid, action, card);
					playerAction.setSrcUserId(board.getLast().getUserid());
					int color = card / 36;
					int value = card % 36 / 4;
					List<Byte> otherCardList = new ArrayList<Byte>();
					int size = 0;
					for (int i = 0; i < player.getCardsArray().length; i++) {
						if (player.getCardsArray()[i] / 36 == color && (player.getCardsArray()[i] % 36) / 4 == value && size < 2) {
							logger.info("size:{}",size);
							size ++;
							continue;
						}
						otherCardList.add(player.getCardsArray()[i]);
					}
					byte[] otherCards = new byte[otherCardList.size()];
					for (int i = 0; i < otherCardList.size(); i++) {
						otherCards[i] = otherCardList.get(i);
					}

					coverCardsHandler(player, color, value);
					player.setCards(otherCards);
					player.getActions().add(playerAction);
					for (Player per : board.getPlayers()) {
						if (board.getLast().getUserid().equals(per.getPlayuser()) && per.getRecoveryHistoryArray() != null && per.getRecoveryHistoryArray().length > 0) {
							byte[] b = new byte[per.getRecoveryHistoryArray().length - 1];
							System.arraycopy(per.getRecoveryHistoryArray(), 0, b, 0, b.length);
							per.setRecoveryHistory(b);
						}
					}
					board.setNextplayer(new NextPlayer(userid, false));

					if (((MaJiangBoard) board).getHuController().size() > 0) {
						if (board instanceof MaJiangBoard) {
							synchronized (((MaJiangBoard) board).getHuController()) {
								((MaJiangBoard) board).setHandlerDoIt(true);
								MJCardMessage mjCardMessage = ((MaJiangBoard) board).getHuController().remove(userid);
								logger.info("userId:{} 碰移除 mjCardMessage:{}",userid,mjCardMessage);
								((MaJiangBoard) board).getHuController().notifyAll();
								//((MaJiangBoard) board).setHandlerDoIt(true);
							}
							logger.info("用户碰 userId:{}", userid);
						}
					}

					//actionEvent.setTarget(board.getLast().getUserid());
					actionEvent.setTarget(board.getLast().getUserid());
					ActionTaskUtils.sendEvent("selectaction", actionEvent, gameRoom);

					CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());    //更新缓存数据

					board.playcards(board, gameRoom, player, orgi);

				} else if (!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.GANG.toString()) &&
						allowAction(card, player.getActions(), BMDataContext.PlayerAction.GANG.toString())) {
					Action playerAction = new Action(userid, action, card);
					if (board.getNextplayer().getNextplayer().equals(userid)) {
						Map<Integer, Object> map = GameUtils.getGangCard(player.getCardsArray(), player.getActions());
						card = (Byte) map.get(2);
						if (!(Boolean) map.get(1)) {

							// 对于碰杠 有可能存在其他家糊的情况， 所以 还要校验其他家有没有糊的情况
							byte[] playCards = new byte[1];
							playCards[0] = card;
							BMDataContext.getGameEngine().takeCardsRequest(roomid, userid, gameRoom.getOrgi(), false, playCards,false);
							if(((MaJiangBoard)board).isQingHu()){
								return null;
							}
							actionEvent = new ActionEvent(board.getBanker(), userid, card, action);
							actionEvent.setActype(BMDataContext.PlayerGangAction.MING.toString());
							playerAction.setType(BMDataContext.PlayerGangAction.MING.toString());
							playerAction.setSrcUserId((String) map.get(6));
							actionEvent.setRemoveCardNum(1);
						} else {
							actionEvent = new ActionEvent(board.getBanker(), userid, card, action);
							actionEvent.setActype(BMDataContext.PlayerGangAction.AN.toString());
							playerAction.setType(BMDataContext.PlayerGangAction.AN.toString());
							actionEvent.setRemoveCardNum(4);
						}
						playerAction.setCard(card);
						actionEvent.setCard(card);
					} else {
						for (Player per : board.getPlayers()) {
							if (board.getLast().getUserid().equals(per.getPlayuser()) && per.getRecoveryHistoryArray() != null && per.getRecoveryHistoryArray().length > 0) {
								byte[] b = new byte[per.getRecoveryHistoryArray().length - 1];
								System.arraycopy(per.getRecoveryHistoryArray(), 0, b, 0, b.length);
								per.setRecoveryHistory(b);
							}
						}
						actionEvent.setActype(BMDataContext.PlayerGangAction.MING.toString());    //还需要进一步区分一下是否 弯杠
						playerAction.setType(BMDataContext.PlayerGangAction.MING.toString());
						playerAction.setSrcUserId(board.getLast().getUserid());
						actionEvent.setRemoveCardNum(3);
					}

					int color = card / 36;
					int value = card % 36 / 4;
					List<Byte> otherCardList = new ArrayList<Byte>();

					boolean isBegin = true;
					//查看碰的里边有杠的没有

					Action tempAction = null;
					String srcUser = null;
					if (CollectionUtils.isNotEmpty(player.getActions())) {
						for (Action at : player.getActions()) {
							if (BMDataContext.PlayerAction.PENG.toString().equals(at.getAction())) {
								if (at.getCard() / 4 == card / 4) {
									tempAction = at;
									srcUser = at.getSrcUserId();
								}
							}
						}
					}

					if (tempAction != null) {
						player.getActions().remove(tempAction);
						Action newAction = new Action(player.getPlayuser(), BMDataContext.PlayerAction.GANG.toString(),
								BMDataContext.PlayerGangAction.MING.toString(), card);
						newAction.setSrcUserId(srcUser);
						player.getActions().add(newAction);

						for (int i = 0; i < player.getCardsArray().length; i++) {
							if (player.getCardsArray()[i] / 36 == color && (player.getCardsArray()[i] % 36) / 4 == value) {
								continue;
							}
							otherCardList.add(player.getCardsArray()[i]);
						}
						byte[] otherCards = new byte[otherCardList.size()];
						for (int i = 0; i < otherCardList.size(); i++) {
							otherCards[i] = otherCardList.get(i);
						}
						coverCardsHandler(player, color, value);
						player.setCards(otherCards);

					} else {
						for (int i = 0; i < player.getCardsArray().length; i++) {
							if (player.getCardsArray()[i] / 36 == color && (player.getCardsArray()[i] % 36) / 4 == value) {
								continue;
							}
							otherCardList.add(player.getCardsArray()[i]);
						}
						byte[] otherCards = new byte[otherCardList.size()];
						for (int i = 0; i < otherCardList.size(); i++) {
							otherCards[i] = otherCardList.get(i);
						}
						coverCardsHandler(player, color, value);
						player.setCards(otherCards);
						player.getActions().add(playerAction);
					}

					if (BMDataContext.PlayerGangAction.MING.toString().equals(actionEvent.getActype())) {
						actionEvent.setTarget(board.getLast().getUserid());
					} else {
						actionEvent.setTarget("all");    //只有明杠 是 其他人打出的 ， target 是单一对象
					}

					if (((MaJiangBoard) board).getHuController().size() > 0) {
						if (board instanceof MaJiangBoard) {
							synchronized (((MaJiangBoard) board).getHuController()) {
								((MaJiangBoard) board).setHandlerDoIt(true);
								MJCardMessage mjCardMessage = ((MaJiangBoard) board).getHuController().remove(userid);
								logger.info("userId:{} 杠移除 ",userid,mjCardMessage);
								((MaJiangBoard) board).getHuController().notifyAll();
								//((MaJiangBoard) board).setHandlerDoIt(true);
							}
							logger.info("用户杠 userId:{}", userid);
						}
					}


					ActionTaskUtils.sendEvent("selectaction", actionEvent, gameRoom);

					/**
					 * 杠了以后， 从 当前 牌的 最后一张开始抓牌
					 */
					board.dealRequest(gameRoom, board, orgi, true, userid);
				} else if (!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.HU.toString())) {    //判断下是不是 真的胡了 ，避免外挂乱发的数据
					//Action playerAction = new Action(userid, action, card);
					//player.getActions().add(playerAction);
					GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi());
					/**
					 * 不同的胡牌方式，处理流程不同，推倒胡，直接进入结束牌局 ， 血战：当前玩家结束牌局，血流：继续进行，下一个玩家
					 */
					if (gamePlayway.getWintype().equals(BMDataContext.MaJiangWinType.TUI.toString())) {        //推倒胡
						logger.info("userId:{} 糊",userid);
						actionEvent = new ActionEvent(board.getBanker(), userid, card, action);
						actionEvent.setActype(BMDataContext.PlayerAction.HU.toString());
						actionEvent.setTarget(board.getLast().getUserid());
						if(board instanceof MaJiangBoard) {
							if (((MaJiangBoard) board).getHuController().size() > 0) {
								synchronized (((MaJiangBoard) board).getHuController()) {
									((MaJiangBoard) board).setHandlerDoIt(true);
									MJCardMessage mjCardMessage = ((MaJiangBoard) board).getHuController().remove(userid);
									logger.info("userId:{} 糊移除 ",userid,mjCardMessage);
									((MaJiangBoard) board).getHuController().notifyAll();
									logger.info("用户胡牌 userId:{}", userid);
								}
							}
						}
						((MaJiangBoard)board).setQingHu(true);
						player.setWin(true);
						if (board.getNextplayer().getNextplayer().equals(userid)) {
							player.setZm(true);
							actionEvent.setZm(true);
						} else {
							player.setZm(false);
							actionEvent.setZm(false);
							player.setTargetUser(board.getLast().getUserid());
						}
						ActionTaskUtils.sendEvent("selectaction", actionEvent, gameRoom);
						player.setCards(ArrayUtils.add(player.getCardsArray(), card));

						GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.ALLCARDS.toString(), 0);    //打完牌了,通知结算
					} else { //血战到底
						if (gamePlayway.getWintype().equals(BMDataContext.MaJiangWinType.END.toString())) {        //标记当前玩家的状态 是 已结束
							player.setEnd(true);
						}
						player.setHu(true);    //标记已经胡了
						/**
						 * 当前 Player打上标记，已经胡牌了，杠碰吃就不会再有了
						 */
						/**
						 * 下一个玩家出牌
						 */
						player = board.nextPlayer(board.index(player.getPlayuser()));
						/**
						 * 记录胡牌的相关信息，推倒胡 | 血战 | 血流
						 */
						board.setNextplayer(new NextPlayer(player.getPlayuser(), false));

						actionEvent.setTarget(board.getLast().getUserid());
						/**
						 * 用于客户端播放 胡牌的 动画 ， 点胡 和 自摸 ，播放不同的动画效果
						 */
						ActionTaskUtils.sendEvent("selectaction", actionEvent, gameRoom);
						CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());    //更新缓存数据

						/**
						 * 杠了以后， 从 当前 牌的 最后一张开始抓牌
						 */
						board.dealRequest(gameRoom, board, orgi, true, player.getPlayuser());
					}
				}
			}
		}
		return actionEvent;
	}

	private void coverCardsHandler(Player player,Integer color,Integer value){

		if(CollectionUtils.isNotEmpty(player.getCoverCards())){
			List<Byte> coverCardList = new ArrayList<Byte>();
			for(int i=0 ; i<player.getCoverCards().size() ; i++){
				if(player.getCoverCards().get(i)/36 == color && player.getCoverCards().get(i) / 4 == value){
					continue ;
				}
				coverCardList.add(player.getCoverCards().get(i)) ;
			}
			player.setCoverCards(coverCardList);
		}
	}


	/**
	 * 为防止同步数据错误，校验是否允许刚碰牌
	 * @param card
	 * @param actions
	 * @return
	 */
	public boolean allowAction(byte card , List<Action> actions , String actiontype){
		int take_color = card / 36 ;
		int take_value = card%36 / 4 ;
		boolean allow = true ;
		for(Action action : actions){
			int color = action.getCard() / 36 ;
			int value = action.getCard() % 36 / 4 ;
			if(take_color == color && take_value == value && action.getAction().equals(actiontype)){
				allow = false ; break ;
			}
		}
		return allow ;
	}
	
	/**
	 * 出牌，不出牌
	 * @param roomid

	 * @param orgi
	 * @return
	 */
	public void noCardsRequest(String roomid, PlayUserClient playUser, String orgi){
		
	}
	
	/**
	 * 加入房间，房卡游戏
	 * @param roomid

	 * @param orgi
	 * @return
	 */
	public GameRoom joinRoom(String roomid, PlayUserClient playUser, String orgi){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			CacheHelper.getGamePlayerCacheBean().put(gameRoom.getId(), playUser, orgi); //将用户加入到 room ， MultiCache
		}
		return gameRoom ;
	}
	
	/**
	 * 退出房间
	 * 1、房卡模式，userid是房主，则解散房间
	 * 2、大厅模式，如果游戏未开始并且房间仅有一人，则解散房间
	 * @param orgi
	 * @return
	 */
	public GameRoom leaveRoom(PlayUserClient playUser , String orgi){
		GameRoom gameRoom = whichRoom(playUser.getId(), orgi) ;
		if(gameRoom!=null){
			List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi) ;
			if(gameRoom.isCardroom()){
				CacheHelper.getGameRoomCacheBean().delete(gameRoom.getId(), gameRoom.getOrgi()) ;
				CacheHelper.getGamePlayerCacheBean().clean(gameRoom.getId() , orgi) ;
				UKTools.published(gameRoom , null , BMDataContext.getContext().getBean(GameRoomRepository.class) , BMDataContext.UserDataEventType.DELETE.toString());
			}else{
				if(players.size() <= 1){
					//解散房间 , 保留 ROOM资源 ， 避免 从队列中取出ROOM
					CacheHelper.getGamePlayerCacheBean().clean(gameRoom.getId() , orgi) ;
				}else{
					CacheHelper.getGamePlayerCacheBean().delete(playUser.getId(), orgi) ;
				}
			}
		}
		return gameRoom;
	}
	/**
	 * 当前用户所在的房间
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public GameRoom whichRoom(String userid, String orgi){
		GameRoom gameRoom = null ;
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userid, orgi) ;
		if(!StringUtils.isBlank(roomid)){//
			gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;		//直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
		}
		return gameRoom;
	}
	
	/**
	 * 结束 当前牌局

	 * @param orgi
	 * @return
	 */
	public void finished(String roomid, String orgi){
		if(!StringUtils.isBlank(roomid)){//
			CacheHelper.getExpireCache().remove(roomid);
			CacheHelper.getBoardCacheBean().delete(roomid, orgi) ;
		}
	}
	/**
	 * 创建新房间 ，需要传入房间的玩法 ， 玩法定义在 系统运营后台，玩法创建后，放入系统缓存 ， 客户端进入房间的时候，传入 玩法ID参数
	 * @param playway
	 * @param userid
	 * @return
	 */
	public GameRoom creatGameRoom(GamePlayway playway , String userid , boolean cardroom , BeiMiClient beiMiClient,String roomId) {
		GameRoom gameRoom = new GameRoom();
		gameRoom.setCreatetime(new Date());
		gameRoom.setRoomid(UKTools.getUUID());
		gameRoom.setUpdatetime(new Date());

		if (playway != null) {
			gameRoom.setPlayway(playway.getId());
			gameRoom.setRoomtype(playway.getRoomtype());
			gameRoom.setPlayers(playway.getPlayers());
		}
		gameRoom.setPlayers(playway.getPlayers());
		gameRoom.setCardsnum(playway.getCardsnum());

		gameRoom.setCurpalyers(1);
		gameRoom.setCardroom(cardroom);

		gameRoom.setStatus(BeiMiGameEnum.CRERATED.toString());

		gameRoom.setCardsnum(playway.getCardsnum());

		gameRoom.setCurrentnum(0);

		gameRoom.setCreater(userid);

		gameRoom.setMaster(userid);
		gameRoom.setNumofgames(playway.getNumofgames());   //无限制
		gameRoom.setOrgi(playway.getOrgi());

		/**
		 * 房卡模式启动游戏
		 */
		//if (beiMiClient.getExtparams() != null && BMDataContext.BEIMI_SYSTEM_ROOM.equals(beiMiClient.getExtparams().get("gamemodel"))) {
		//if (beiMiClient.getExtparams() != null) {
			gameRoom.setRoomtype(BMDataContext.ModelType.ROOM.toString());
			gameRoom.setCardroom(true);
			if(beiMiClient != null) {
				gameRoom.setExtparams(beiMiClient.getExtparams());
			}
			/**
			 * 产生 房间 ID ， 麻烦的是需要处理冲突 ，准备采用的算法是 先生成一个号码池子，然后重分布是缓存的 Queue里获取
			 */
			if (StringUtils.isNotEmpty(roomId)) {
				gameRoom.setRoomid(roomId);
			} else {
				gameRoom.setRoomid(RandomCharUtil.getRandomNumberChar(6));
			}

			/**
			 * 分配房间号码 ， 并且，启用 规则引擎，对房间信息进行赋值
			 */
			kieSession.insert(gameRoom);
			kieSession.fireAllRules();
		//} else {
		//	gameRoom.setRoomtype(BMDataContext.ModelType.HALL.toString());
		//}

		CacheHelper.getQueneCache().put(gameRoom, playway.getOrgi());    //未达到最大玩家数量，加入到游戏撮合 队列，继续撮合

		UKTools.published(gameRoom, null, BMDataContext.getContext().getBean(GameRoomRepository.class), BMDataContext.UserDataEventType.SAVE.toString());

		return gameRoom;
	}
	
	
	/**
	 * 解散房间 , 解散的时候，需要验证下，当前对象是否是房间的创建人
	 */
	public void dismissRoom(GameRoom gameRoom , String userid,String orgi){
		if(gameRoom.getMaster().equals(userid)){
			CacheHelper.getGamePlayerCacheBean().delete(gameRoom.getId(), orgi) ;
			List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi) ;
			for(PlayUserClient player : players){
				/**
				 * 解散房间的时候，只清理 AI
				 */
				if(player.getPlayertype().equals(BMDataContext.PlayerTypeEnum.AI.toString())){
					CacheHelper.getGamePlayerCacheBean().delete(player.getId(), orgi) ;
					CacheHelper.getRoomMappingCacheBean().delete(player.getId(), orgi) ;
				}
			}
			/**
			 * 先不删
			 */
//			UKTools.published(gameRoom, null, BMDataContext.getContext().getBean(GameRoomRepository.class) , BMDataContext.UserDataEventType.DELETE.toString());
		}
	}
}
