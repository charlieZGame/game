
package com.beimi.util.server.handler;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.beimi.backManager.HouseCardHandlerService;
import com.beimi.backManager.StandardResponse;
import com.beimi.core.engine.game.GameEngine;
import com.beimi.model.DefineMap;
import com.beimi.model.OutRoom;
import com.beimi.model.PlayCache;
import com.beimi.util.RandomCharUtil;
import com.beimi.util.cache.CacheBean;
import com.beimi.util.cache.hazelcast.HazlcastCacheHelper;
import com.beimi.util.cache.hazelcast.impl.ProxyGameRoomCache;
import com.beimi.util.rules.model.*;
import com.beimi.web.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.Message;
import com.beimi.util.GameUtils;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.web.service.repository.es.PlayUserClientESRepository;
import com.beimi.web.service.repository.jpa.GameRoomRepository;
import com.beimi.web.service.repository.jpa.PlayUserClientRepository;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;

public class GameEventHandler {
	protected SocketIOServer server;
	private static Logger logger = LoggerFactory.getLogger(GameEventHandler.class);


	@Autowired
	public GameEventHandler(SocketIOServer server) {
		this.server = server;
	}

	@OnConnect
	public void onConnect(SocketIOClient client) {

		logger.info("sessionId:{}, reconnect", client.getSessionId());

		client.sendEvent("connect", new Message() {

			private String msg = "success";
			private String command;

			@Override
			public String getCommand() {
				return this.command;
			}

			@Override
			public void setCommand(String command) {
				this.command = command;
			}

		});
	}

	@OnEvent("heartbeat")
	public void heartbeat(SocketIOClient client,String token){
	//	logger.info("sessionId:{}心跳,tokenId:{}",client.getSessionId(),token);
		Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
		if(userToken == null){
			client.sendEvent("heartbeat token null", token);
			return;
		}
		logger.info("发出心跳sessionId:{},userId:{}",client.getSessionId(),userToken.getUserid());
		CacheHelper.getInstance().putUserId(client.getSessionId().toString(),userToken.getUserid());
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(userToken.getUserid());
		if(beiMiClient == null){
			client.sendEvent("heartbeat", token);
			return;
		}
		beiMiClient.setClient(client);
		client.sendEvent("heartbeat", new Object[] { token });
		sendJoinUserAgain(beiMiClient,client);
	}


	private void sendJoinUserAgain(BeiMiClient beiMiClient,SocketIOClient client) {

		logger.info("sessionId:{} 心跳重发用户信息");
		Token userToken;
		if (beiMiClient != null && !StringUtils.isBlank(beiMiClient.getToken()) && (userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getToken(), beiMiClient.getOrgi())) != null) {
			//鉴权完毕
			PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
			if (userClient == null) {
				return;
			}

			String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userClient.getId(), userClient.getOrgi());
			if (StringUtils.isEmpty(roomid) || CacheHelper.getBoardCacheBean().getCacheObject(roomid, userClient.getId()) == null) {
				return;
			}
			GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, userClient.getOrgi());
			List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			int offset = 0;
			for (PlayUserClient temp : playerList) {
				if (temp.getId().equals(userToken.getUserid())) {
					offset = playerList.indexOf(temp);
					break;
				}
			}
			logger.info("发送用户同步消息");
			JoinRoom joinRoom = new JoinRoom(userClient, offset, gameRoom.getPlayers(), gameRoom);
			joinRoom.setCommand("joinroom");
			client.sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, joinRoom);
		}
	}


	//抢地主事件   //// TODO: 2018/3/14 zcl 系统退出麻将时，会走这个方法
	@OnEvent(value = "start")
	public void onStart(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {

				PlayUserClient playUser = (PlayUserClient) CacheHelper.getGamePlayerCacheBean().getPlayer(userToken.getUserid(), userToken.getOrgi());
				if (playUser != null) {
					BMDataContext.getGameEngine().startGameRequest(playUser.getRoomid(), playUser, userToken.getOrgi(), "true".equals(data));
				}
			}
		}
	}

	//抢地主事件
	@OnEvent(value = "recovery")
	public void onRecovery(SocketIOClient client, String data) {
		long tid = System.nanoTime();
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				BMDataContext.getGameEngine().gameRequest(playUser.getId(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), playUser, beiMiClient,data);
			}
		}
	}

	@OnEvent(value = "leave")
	public void onLeaveLHandler(SocketIOClient client, String data) {
	}

	//玩家离开
/*	public void onLeaveL(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString(), true);
			}
		}
	}*/


	@OnEvent(value = "leaveroom")
	public void applyLeaveRoom(SocketIOClient client, String data) {
		try {

			long tid = System.currentTimeMillis();
			logger.info("data:{} 离场请求数据信息",data);
			Map<String, Object> map = JSONObject.parseObject(data, Map.class);
			BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
			//logger.info("userId:{},room:{} 离场请求用户信息",beiMiClient.getUserid(),beiMiClient.getRoom(),beiMiClient.getToken());
			if ("1".equals(map.get("type"))) {
				logger.info("userId:{},room:{} 强制离场",beiMiClient.getUserid(),beiMiClient.getRoom(),beiMiClient.getToken());
				//// TODO: 2018/3/16  标记用户是不友好用户 这个逻辑以后处理
				//signUserUnfriendly(tid,client,data);
				signUserUnfriendly(tid,client,data);
				onLeaveL(client, data);
			} else if ("2".equals(map.get("type"))) {
				logger.info("userId:{},room:{} 申请离场",beiMiClient.getUserid(),beiMiClient.getRoom(),beiMiClient.getToken());
				acceptLeaveRoom(tid,client, map);
			} else if ("4".equals(map.get("type"))) {
				logger.info("userId:{},room:{} 得到用户投票相应 userID:{},投票结果：result:{}",beiMiClient.getUserid(),data);
				if(applyLeaveRoomResponse(tid,client, map)){
					onLeaveL(client,data);
				}
			}else if("7".equals(map.get("type"))){
				userNormalExist(beiMiClient);
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi()) ;
				List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(playUser.getRoomid(), playUser.getOrgi()) ;
				if(CollectionUtils.isNotEmpty(players) && players.size() <= 1){
					onLeaveL(client,data);
				}else {
					CacheHelper.getRoomMappingCacheBean().delete(playUser.getId(), playUser.getOrgi());
				}
				if(CollectionUtils.isEmpty(players)){
					return;
				}
				players.remove(playUser);
				//onLeaveL(client,data);
			}
		} catch (RuntimeException e) {
			logger.error("请求退出异常", e);
			throw e;
		}
	}

	@OnEvent(value="gameOverSummary")
	public void gameOverSummary (SocketIOClient client, String data){
		logger.info("主动获取总结数据 data:{}",data);
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				HouseCardHandlerService cardHandlerService = BMDataContext.getContext().getBean("houseCardHandlerService",HouseCardHandlerService.class);
				StandardResponse response = cardHandlerService.queryPlayerEnd(Integer.parseInt(data),userToken.getUserid());
				beiMiClient.getClient().sendEvent("gameSummaryExist",response);
			}
		}else{
			tokenIllegal(client);
		}

	}

	private void tokenIllegal(SocketIOClient client){
		client.sendEvent("tokenIllegal",new DefineMap<String, String>().putData("type", "-1").putData("msg","token illegal"));
	}


	private void existSendSummary(BeiMiClient beiMiClient){

		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
				if(StringUtils.isEmpty(roomid)){
					logger.info("roomId 空");
					return;
				}
				HouseCardHandlerService cardHandlerService = BMDataContext.getContext().getBean("houseCardHandlerService",HouseCardHandlerService.class);
				GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
				StandardResponse response = cardHandlerService.queryPlayerEnd(Integer.parseInt(gameRoom.getRoomid()),userToken.getUserid());
				beiMiClient.getClient().sendEvent("gameSummaryExist",response);
			}
		}

	}



	public static void userNormalExist(BeiMiClient beiMiClient) {

		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
		if(gameRoom == null || gameRoom.getId() == null){
			return ;
		}
		List<PlayUserClient> playUserClients = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), beiMiClient.getOrgi());

		List<PlayUserClient> playUsers = new ArrayList<PlayUserClient>();
		for (PlayUserClient playUserClient : playUserClients) {
			if (playUserClient.getId().equals(beiMiClient.getUserid())) {
				continue;
			}
			playUsers.add(playUserClient);
		}
		GamePlayers gamePlayers = new GamePlayers(gameRoom.getPlayers(), playUsers, BMDataContext.BEIMI_PLAYERS_EVENT);

		for (PlayUserClient playUserClient : playUserClients) {
			if (playUserClient.getId().equals(beiMiClient.getUserid())) {
				continue;
			}
			logger.info("人数未达到发出解散 userId:{}", playUserClient.getId());
			BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
			tmpClient.getClient().sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, gamePlayers);

		}
	}

	@OnEvent("chat")
	public void chatMessage(SocketIOClient client,String data){

		Map<String,Object> map = JSONObject.parseObject(data,Map.class);
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));

		if(map == null || map.isEmpty()){
			return ;
		}

		if("1".equals(map.get("type"))){
			String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
			GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
			List<PlayUserClient> playUserClients = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), beiMiClient.getOrgi());
			if(playUserClients == null || playUserClients.size() == 0){
				return;
			}
			for (PlayUserClient playUserClient : playUserClients) {
				if (playUserClient.getId().equals(beiMiClient.getUserid())) {
					continue;
				}
				logger.info(" 发送聊天 userId:{}", playUserClient.getId());
				BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
				tmpClient.getClient().sendEvent(BMDataContext.CHAT, map);
			}
		}else if("2".equals(map.get("type"))){

			//// TODO: 2018/3/19 后期处理
		}
	}


	/**
	 *
	 * @param client
	 * @param map
     */
	private void acceptLeaveRoom(Long tid,SocketIOClient client,Map<String,Object> map){

		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (StringUtils.isEmpty(token)) {
			client.sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new PlayerChartMsg<String>(null, null, null, null, "-1", "illegal token"));
			return;
		}
		PlayCache.put(beiMiClient.getUserid(),new OutRoom().setApplyUserId(beiMiClient.getUserid()));
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
		PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		logger.info("tid:{} 准备发起投票 playerList:{}",tid,playerList.size());
		for (PlayUserClient playUserClient : playerList) {
			if (playUserClient.getId().equals(beiMiClient.getUserid())) {
				continue;
			}
			logger.info("tid:{} 发出投票请求 userId:{}",tid,playUserClient.getId());
			BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
			tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new PlayerChartMsg<String>(playUser.getId(), playUser.getUsername()+"", playUserClient.getId(), playUserClient.getUsername()+"", "3", "用户 [" + playUser.getUsername() + "]请求离场"));
		}

	}


	/**
	 *
	 * @param client
	 * @param response
     */
	private boolean applyLeaveRoomResponse(long tid,SocketIOClient client, Map<String,Object> response) {

		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (StringUtils.isEmpty(token)) {
			client.sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new DefineMap<String, String>().putData("type", "-1"));
			return false;
		}

		PlayCache.put(beiMiClient.getUserid(), new OutRoom().setApplyUserId(beiMiClient.getUserid()));

		OutRoom outRoom = PlayCache.get((String)response.get("srcUserId"), OutRoom.class);
		logger.info("outRoom:{} 信息",outRoom);
		synchronized (outRoom) {
			outRoom.getVolate().put(beiMiClient.getUserid(), "1".equals(response.get("isAgree")) ? true : false);
			logger.info("outRoomSize:{} 信息",outRoom.getVolate().size());
			if (outRoom.getVolate().size() < 3) {
				logger.info("outRoomFalse:{} 信息",false);
				return false;
			}
			logger.info("outRoomSize:{} 够三个",outRoom.getVolate().size());
			for (Iterator<Boolean> it = outRoom.getVolate().values().iterator(); it.hasNext(); ) {
				if (!it.next()) {
					logger.info("outRoom :{} 有未赞成的用户",outRoom.getVolate().size());
					PlayCache.clear(beiMiClient.getUserid());
					BeiMiClient tmpClient = NettyClients.getInstance().getClient((String)response.get("srcUserId"));
					tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new DefineMap<String, String>().putData("type", "5").putData("isAgree", "0").putData("msg","小伙伴让我告诉你,再玩会呗!"));
					return false;
				}
			}
			PlayCache.clear(beiMiClient.getUserid());
			PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
			List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(playUser.getRoomid(), beiMiClient.getOrgi());
			logger.info("tid:{} 准备发起解散 playerList:{}",tid,playerList.size());
			for (PlayUserClient playUserClient : playerList) {
				logger.info("tid:{} 发出解散 userId:{}", tid, playUserClient.getId());
				BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
				GameRoom gameRoom  = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
				tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new DefineMap<String, String>().putData("type", "5").putData("isAgree", "1").putData("isHaveSummary",gameRoom.getCurrentnum() > 1 ? true : false).putData("msg", "小伙伴都同意解散房间"));
			}
			return true;
		}
	}

	
	public void signUserUnfriendly(long tid,SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
		if(playerList == null || playerList.size() == 0){
			return ;
		}
		GameRoom gameRoom  = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
		logger.info("tid:{} 玩家强制离场 playerList:{}", tid, playerList.size());
		for (PlayUserClient playUserClient : playerList) {
			logger.info("tid:{} 玩家强制离场 userId:{}", tid, playUserClient.getId());
			BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
			tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new DefineMap<String, String>().putData("type", "6").putData("isHaveSummary",gameRoom.getCurrentnum() > 1 ? true : false));
			if(gameRoom.getCurrentnum() > 1) {
				existSendSummary(tmpClient);
				HouseCardHandlerService cardHandlerService = BMDataContext.getContext().getBean("houseCardHandlerService", HouseCardHandlerService.class);
			//	cardHandlerService.cardHandler(gameRoom,players,board,returnResults);
			}
		}
	}



	@OnEvent(value = "getCurrentCards")
	public void getCurrentCards(SocketIOClient client, String data){
		// 方法暂时停用
		if(1 ==1) {
			return;
		}

		long tid = System.currentTimeMillis();
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		PlayUserClient currentPlayer = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		logger.info("tid:{} 恢复用户信息[GET-CRURRENT-CARDS] currentPlayer:{}",tid,currentPlayer);
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		if(StringUtils.isEmpty(roomid)){
			logger.info("tid:{} roomid is null");
			beiMiClient.getClient().sendEvent("recovery", new DefineMap<String, String>().putData("status", "-1").putData("msg","未开始"));
			return;
		}
		GameRoom gameRoom  = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());        //直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
		if(gameRoom == null){
			logger.info("tid:{} gameRoom is null",tid);
			beiMiClient.getClient().sendEvent("recovery", new DefineMap<String, String>().putData("status", "-1").putData("msg","未开始"));
			return;
		}
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());

		if(board == null){
			logger.info("tid:{} bord is null",tid);
			beiMiClient.getClient().sendEvent("recovery", new DefineMap<String, String>().putData("status", "-1").putData("msg","未开始"));
			return;
		}
		for (Player player : board.getPlayers()) {
			if (!currentPlayer.getId().equals(player.getPlayuser())) {
				continue;
			}
			logger.info("tid:{} 恢复牌面信息[GET-CRURRENT-CARDS]  cards:{}", tid, player.getCards());
			GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), beiMiClient.getOrgi()) ;
			RecoveryData recoveryData = null;
			if("koudajiang".equals(gamePlayway.getCode())){
				recoveryData = CardRecoverUtil.kouRecoverHandler(player,board,gameRoom);
				logger.info("tid:{} 恢复kou-shou牌面信息发牌完成[GET-CRURRENT-CARDS]  cards:{}", tid, JSONObject.toJSONString(recoveryData));
			}else{
				recoveryData = CardRecoverUtil.hunHandler(tid,(MaJiangBoard)board,player,gameRoom);
				logger.info("tid:{} 恢复hun-shou牌面信息发牌完成[GET-CRURRENT-CARDS]  cards:{}", tid, JSONObject.toJSONString(recoveryData));
			}
			//beiMiClient.getClient().sendEvent("recovery",recoveryData, gameRoom);
			beiMiClient.getClient().sendEvent("recovery",recoveryData);
			//ActionTaskUtils.sendEvent("recovery", new RecoveryData(player, board.getLasthands(), board.getNextplayer() != null ? board.getNextplayer().getNextplayer() : null, 25, false, board,-1), gameRoom);
		}
	}



	//杂七杂八的指令，混合到一起
	@OnEvent(value = "command")
	public void onCommand(SocketIOClient client, String data) {
		Command command = JSON.parseObject(data, Command.class);
		Message message = null;
		if (command != null && !StringUtils.isBlank(command.getToken())) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(command.getToken(), BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				switch (command.getCommand()) {
					case "subsidy":
						message = GameUtils.subsidyPlayerClient(userToken.getUserid(), userToken.getOrgi());
						break;
				}
			}
			if (message != null) {
				client.sendEvent(message.getCommand(), message);
			}
		}
	}

	//聊天
	@OnEvent(value = "message")
	public void onMessage(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString(), false);
			}
		}
	}


	//抢地主事件  zcl 加入房间
	@OnEvent(value = "joinroom")
	public void onJoinRoom(SocketIOClient client, AckRequest request, String data) {
		long tid = System.nanoTime();
		logger.info("tid:{}, 加入房间信息  data:{},sessionId:{}",tid,data,client.getSessionId());
		BeiMiClient beiMiClient = JSON.parseObject(data, BeiMiClient.class);
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			/**
			 * Token不为空，并且，验证Token有效，验证完毕即开始进行游戏撮合，房卡类型的
			 * 1、大厅房间处理
			 *    a、从房间队列里获取最近一条房间信息
			 *    b、将token对应玩家加入到房间
			 *    c、如果房间凑齐了玩家，则将房间从等待撮合队列中移除，放置到游戏中的房间信息，如果未凑齐玩家，继续扔到队列
			 *    d、通知房间的所有人，有新玩家加入
			 *    e、超时处理，增加AI进入房价
			 *    f、事件驱动
			 *    g、定时器处理
			 * 2、房卡房间处理
			 * 	  a、创建房间
			 * 	  b、加入到等待中队列
			 */
			Token userToken;
			if (beiMiClient != null && !StringUtils.isBlank(token) && (userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, beiMiClient.getOrgi())) != null) {
				//鉴权完毕
				logger.info("再次登录 userId:{}",userToken.getUserid());
				PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				beiMiClient.setClient(client);
				beiMiClient.setUserid(userClient.getId());
				beiMiClient.setSession(CacheHelper.getInstance().getUserId(CacheHelper.getInstance().getUserId(client.getSessionId().toString())));

				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				if(StringUtils.isEmpty(roomid)) {
					PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
					int jun = 0;
					if(StringUtils.isNotEmpty(data)) {
						Map<String, Object> map = JSONObject.parseObject(data, Map.class);
						if(map != null && !map.isEmpty() && map.containsKey("extparams")){
							JSONObject jsonObject = (JSONObject)map.get("extparams");
							if(jsonObject != null && org.apache.commons.lang3.StringUtils.isNotEmpty((String)jsonObject.get("jun"))){
								jun = Integer.parseInt(jsonObject.get("jun").toString());
							}
							if(jsonObject != null && org.apache.commons.lang3.StringUtils.isNotEmpty((String)jsonObject.get("koujun"))){
								jun = Integer.parseInt(jsonObject.get("koujun").toString());
							}
						}
					}
					if(playUser.getCards()<=0 || playUser.getCards() < jun){
						beiMiClient.getClient().sendEvent(BMDataContext.CARD_CHECK, new DefineMap<String, String>().putData("status", "-1").putData("msg","房卡不够，请充值!"));
						return;
					}
				}

				/**
				 * 心跳时间
				 */
				beiMiClient.setTime(System.currentTimeMillis());
				NettyClients.getInstance().putClient(userClient.getId(), beiMiClient);

				/**
				 * 更新当前玩家状态，在线|离线
				 */
				userClient.setOnline(true);

				/**
				 * 更新状态
				 */
				ActionTaskUtils.updatePlayerClientStatus(userClient, BMDataContext.PlayerTypeEnum.NORMAL.toString(), false);
				UKTools.published(userClient, BMDataContext.getContext().getBean(PlayUserClientESRepository.class), BMDataContext.getContext().getBean(PlayUserClientRepository.class));
				BMDataContext.getGameEngine().gameRequest(userToken.getUserid(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), userClient, beiMiClient,data);
			}
		}else{
			tokenIllegal(client);
		}
	}


	@OnEvent(value = "proxyCreateRoom")
	public void proxyCreateRoom(SocketIOClient client, AckRequest request, String data) {

		Map<String, Object> map = JSONObject.parseObject(data, Map.class);
		String token = null;
		if (map != null && !map.isEmpty()) {
			token = (String)map.get("token");
		} else {
			client.sendEvent("proxyCreateRoom", "token illegal");
			return;
		}
		Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
		if (userToken == null) {
			logger.error("clientId:{} 非法登录", client.getSessionId());
			tokenIllegal(client);
			return;
		}

		int jun = 0;
		PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
		if (StringUtils.isNotEmpty(data)) {
			if (map != null && !map.isEmpty() && map.containsKey("extparams")) {
				JSONObject jsonObject = (JSONObject) map.get("extparams");
				if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("jun"))) {
					jun = Integer.parseInt(jsonObject.get("jun").toString());
				}
				if (jsonObject != null && org.apache.commons.lang3.StringUtils.isNotEmpty((String) jsonObject.get("koujun"))) {
					jun = Integer.parseInt(jsonObject.get("koujun").toString());
				}
			}
		}
		if (playUser.getCards() <= 0 || playUser.getCards() < jun) {
			client.sendEvent(BMDataContext.CARD_CHECK, new DefineMap<String, String>().putData("status", "-1").putData("msg", "房卡不够，请充值!"));
			return;
		}

		String roomId = RandomCharUtil.getRandomNumberChar(6);
		if (CacheHelper.getProxyGameRoomCache().getCacheObject(playUser.getId(), playUser.getOrgi()) == null) {
			Map<String, String> roomMap = new HashMap<String, String>();
			roomMap.put(roomId, data);
			CacheHelper.getProxyGameRoomCache().put(playUser.getId(), roomMap, playUser.getOrgi());
		} else {
			((Map<String, String>) CacheHelper.getProxyGameRoomCache().getCacheObject(playUser.getId(), playUser.getOrgi())).put(roomId, data);
		}
		client.sendEvent("proxyCreateRoom", new DefineMap<String, String>().putData("roomId", roomId).putData("msg", "OK"));
	}




	//抢地主事件
	// zcl 创建房间 调用此方法
	@OnEvent(value = "gamestatus")
	public void onGameStatus(SocketIOClient client, String data) {
		logger.info("sessionId:{} 获取用户状态 data:{}",client.getSessionId(),data);
		BeiMiClient beiMiClient = JSON.parseObject(data, BeiMiClient.class);
		Token userToken;
		GameStatus gameStatus = new GameStatus();
		gameStatus.setGamestatus(BMDataContext.GameStatusEnum.NOTREADY.toString());
		if (beiMiClient != null && !StringUtils.isBlank(beiMiClient.getToken()) && (userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getToken(), beiMiClient.getOrgi())) != null) {
			//鉴权完毕
			PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
			if (userClient != null) {
				gameStatus.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userClient.getId(), userClient.getOrgi());
				//// TODO: 2018/3/13
				if (!StringUtils.isBlank(roomid) && CacheHelper.getBoardCacheBean().getCacheObject(roomid, userClient.getId()) != null) {
					//if (!StringUtils.isBlank(roomid) /*&& CacheHelper.getBoardCacheBean().getCacheObject(roomid, userClient.getOrgi())!=null*/) {
					gameStatus.setUserid(userClient.getId());
					gameStatus.setOrgi(userClient.getOrgi());

					GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, userClient.getOrgi());
					GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), userClient.getOrgi());
					gameStatus.setGametype(gamePlayway.getCode());
					gameStatus.setPlayway(gamePlayway.getId());
					gameStatus.setGamestatus(BMDataContext.GameStatusEnum.PLAYING.toString());
					gameStatus.setRoomId(gameRoom.getRoomid());
					gameStatus.setNotFinishGame(true);
					if (gameRoom.isCardroom()) {
						gameStatus.setCardroom(true);
					}
				}
			}
		} else {
			gameStatus.setGamestatus(BMDataContext.GameStatusEnum.TIMEOUT.toString());
		}
		client.sendEvent(BMDataContext.BEIMI_GAMESTATUS_EVENT, gameStatus);
	}


	@OnEvent(value = "searchHaveNotFinishGame")
	public void searchHaveNotFinishGame(SocketIOClient client,String token){
		logger.info("token:{} 获取是否存在房间",token);
		Token userToken = (Token)CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
		if(userToken == null){
			logger.info("token:{} token illeage",token);
			tokenIllegal(client);
			return;
		}
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
		logger.info("userId:{} 获取是否存在房间,roomId:{}",userToken.getUserid(),roomid);
		if(StringUtils.isEmpty(roomid)) {
			client.sendEvent("searchHaveNotFinishGame", new DefineMap<String, String>().putData("type", "1").putData("msg", "ok!"));
		}else{
			GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, userToken.getOrgi());
			GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), userToken.getOrgi());
			DefineMap<String, String> map = new DefineMap<String, String>().putData("type", "2").
					putData("roomId",gameRoom.getRoomid()).
					putData("msg", "ok!").
					putData("jun",gameRoom.getNumofgames()+"").
					putData("hun",gameRoom.getPowerfulsize()+"").
					putData("hunfeng",gameRoom.isWindow()+"").
					putData("hunpiao",gameRoom.getPiao()+"").
					putData("playway",gameRoom.getPlayway()).
					putData("gamemodel","room").
					putData("gametype",gamePlayway.getCode());
			client.sendEvent("searchHaveNotFinishGame", map);
			logger.info("token:{} 返回数据,map:{}",token,map);
		}
	}


	//抢地主事件
	// zcl 查找房间
	@OnEvent(value = "searchroom")
	public void onSearchRoom(SocketIOClient client, String data,String token) {
		SearchRoom searchRoom = JSON.parseObject(data, SearchRoom.class);
		GamePlayway gamePlayway = null;
		SearchRoomResult searchRoomResult = null;
		boolean joinRoom = false;
		GameRoom gameRoom = null;

		if (searchRoom == null ||searchRoom.getToken() == null) {
			tokenIllegal(client);
			return;
		}
		if( CacheHelper.getApiUserCacheBean().getCacheObject(searchRoom.getToken(), BMDataContext.SYSTEM_ORGI) == null){
			tokenIllegal(client);
			return;
		}

		if (searchRoom != null && StringUtils.isNotEmpty(searchRoom.getUserid())) {
			GameRoomRepository gameRoomRepository = BMDataContext.getContext().getBean(GameRoomRepository.class);
			PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(searchRoom.getUserid(), searchRoom.getOrgi());
			if (playUser != null) {
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				if (!StringUtils.isBlank(roomid)) {
					gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, playUser.getOrgi());
				} else {
					List<GameRoom> gameRoomList = gameRoomRepository.findByRoomidAndOrgi(searchRoom.getRoomid(), playUser.getOrgi());
					if (gameRoomList != null && gameRoomList.size() > 0) {
						GameRoom tempGameRoom = gameRoomList.get(0);
						gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(tempGameRoom.getId(), playUser.getOrgi());
					}
				}
				if (gameRoom != null) {
					/**
					 * 将玩家加入到 房间 中来 ， 加入的时候需要处理当前的 房间 已满员或未满员，如果满员，需要检查是否允许围观
					 */
					gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi());
					List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
					//List<PlayUserClient> playerList = (List<PlayUserClient>) CacheHelper.getGamePlayerCacheBean().getPlayer(gameRoom.getId(), gameRoom.getOrgi());

					for (PlayUserClient playUserClient : playerList) {
						if (playUserClient.getId().equals(playUser.getId())) {
							joinRoom = true;
						}
					}
					if (playerList.size() < gamePlayway.getPlayers() && !joinRoom) {
						BMDataContext.getGameEngine().joinRoom(gameRoom, playUser, playerList);
						joinRoom = true;
					}
					/**
					 * 获取的玩法，将玩法数据发送给当前请求的玩家
					 */
				} else {
					ProxyGameRoomCache cacheBean = (ProxyGameRoomCache) CacheHelper.getProxyGameRoomCache().getCacheInstance(HazlcastCacheHelper.CacheServiceEnum.ProxyGameRoomCache.toString());
					for (Object map : cacheBean.getInstance().values()) {
						if (((Map<String, String>) map).containsKey(searchRoom.getRoomid())) {
							String dataMap = ((Map<String, String>) map).get(searchRoom.getRoomid());
							Map tempMap = JSONObject.parseObject(dataMap,Map.class);
							List<PlayUserClient> playerList = new ArrayList<PlayUserClient>();
							gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject((String)tempMap.get("playway"), (String)tempMap.get("orgi"));
							gameRoom = BMDataContext.getGameEngine().creatGameRoom(gamePlayway, playUser.getId(), true, null, searchRoom.getRoomid());
							BMDataContext.getGameEngine().setGameParam(dataMap, gameRoom);
							BMDataContext.getGameEngine().joinRoom(gameRoom, playUser, playerList);
							CacheHelper.getGameRoomCacheBean().put(gameRoom.getId(), gameRoom, (String)tempMap.get("orgi"));
							joinRoom = true;
							break;
						}
					}
				}
			}else{
				//// TODO: 2018/5/2 会话失效
			}
		}
		if (gamePlayway != null) {
			//通知客户端
			if (joinRoom == true) {        //加入成功 ， 是否需要输入加入密码？
				searchRoomResult = new SearchRoomResult(gamePlayway.getId(), gamePlayway.getCode(), BMDataContext.SearchRoomResultType.OK.toString());
				searchRoomResult.setJun(gameRoom.getNumofgames()+"");
				searchRoomResult.setRoomid(gameRoom.getRoomid());
				searchRoomResult.setHun(gameRoom.getPowerfulsize()+"");
				searchRoomResult.setPlayway(gamePlayway.getId());
				searchRoomResult.setHunfeng(gameRoom.isWindow()+"");
				searchRoomResult.setHunpiao(gameRoom.getPiao()+"");
			} else {                        //加入失败
				searchRoomResult = new SearchRoomResult(BMDataContext.SearchRoomResultType.FULL.toString());
			}
		} else { //房间不存在
			searchRoomResult = new SearchRoomResult(BMDataContext.SearchRoomResultType.NOTEXIST.toString());
		}

		client.sendEvent(BMDataContext.BEIMI_SEARCHROOM_EVENT, searchRoomResult);
	}


	//添加@OnDisconnect事件，客户端断开连接时调用，刷新客户端信息
	@OnDisconnect
	public void onDisconnect(SocketIOClient client) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		if (beiMiClient != null) {
			/**
			 * 玩家离线
			 */

		long tid = System.currentTimeMillis();
			PlayUserClient playUserClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
			if (playUserClient != null) {
				if (BMDataContext.GameStatusEnum.PLAYING.toString().equals(playUserClient.getGamestatus())) {
					//signUserUnfriendly(tid,client,null);
					GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.OFFLINE.toString(), false);
				} else {
					//如果不是正在打牌状态可以进行退出，不做强退
					//userNormalExist(beiMiClient);
					// 用户信息暂时不用删除 用户房间信息需要删除
					//CacheHelper.getApiUserCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi());
					/*if (CacheHelper.getGamePlayerCacheBean().getPlayer(beiMiClient.getUserid(), beiMiClient.getOrgi()) != null) {
						CacheHelper.getGamePlayerCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi());
					}*/
					//CacheHelper.getRoomMappingCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi());

					/**
					 * 玩家退出游戏，需要发送事件给所有玩家，如果房主退出，则房间解散
					 */
				}
				/**
				 * 退出房间，房卡模式下如果房间还有剩余局数 ， 则不做任何操作，如果无剩余或未开始扣卡，则删除房间
				 */

			}
		}
	}


	//玩家离开
	//@OnEvent(value = "leave")
	public void onLeaveL(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		if(beiMiClient == null){
			logger.info("退出客户端为空");
			return;
		}
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString(), true);
			}
		}
	}


	@OnEvent(value = "answerKou")
	public void giveKouInfo(SocketIOClient client, String data) {

		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		logger.info("接收到客户端扣应答,userId:{},data:{},userName:{}", beiMiClient.getUserid(), data);
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
				GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
				MaJiangBoard board = (MaJiangBoard) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
				if (board.getAnswer().containsKey(beiMiClient.getUserid())) {
					if ("0".equals(data)||board.getAnswer().get(beiMiClient.getUserid()) == -1) {
						board.getAnswer().put(beiMiClient.getUserid(), -1);
					} else {
						board.getAnswer().put(beiMiClient.getUserid(), board.getAnswer().get(beiMiClient.getUserid()) + 1);
					}
				} else {
					if ("0".equals(data)) {
						board.getAnswer().put(beiMiClient.getUserid(), -1);
					} else {
						board.getAnswer().put(beiMiClient.getUserid(), 1);
					}
				}
			}
			client.sendEvent("answerKou", data);
		}
	}

	@OnEvent(value = "getUserInfo")
	public void getUserInfo(SocketIOClient client, String data){

		Map<String,String> map = JSONObject.parseObject(data,Map.class);
		String token = null;
		if(map != null && !map.isEmpty()){
			token = map.get("token");
		}else{
			client.sendEvent("getUserInfo", "token illegal");
			return ;
		}
		Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
		PlayUserClientRepository playUserClientRepository = BMDataContext.getContext().getBean("playUserClientRepository",PlayUserClientRepository.class);
		PlayUserClient playUserClient = playUserClientRepository.findById(userToken.getUserid());
		client.sendEvent("getUserInfo", playUserClient);
	}



	@OnEvent(value = BMDataContext.CARD_CHECK)
	public void cardCheck(SocketIOClient client, String data) {

		logger.info("房卡检查,data:{},", data);
		Token userToken = null;
		if (StringUtils.isEmpty(data)) {
			logger.info("非法请求2");
			return;
		}
		Map<String, Object> map = JSONObject.parseObject(data, Map.class);
		String token = (String) map.get("token");
		if (StringUtils.isEmpty(token)) {
			logger.info("非法请求2");
			return;
		} else {
			userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken == null) {
				logger.info("非法请求3");
				return;
			}
		}
		int jun = 0;
		if (map != null && !map.isEmpty() && map.containsKey("extparams")) {
			JSONObject jsonObject = (JSONObject) map.get("extparams");
			if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("jun"))) {
				jun = Integer.parseInt(jsonObject.get("jun").toString());
			}
			if (jsonObject != null && StringUtils.isNotEmpty((String) jsonObject.get("koujun"))) {
				jun = Integer.parseInt(jsonObject.get("koujun").toString());
			}
		}
		PlayUserClientRepository playUserClientRepository = BMDataContext.getContext().getBean("playUserClientRepository",PlayUserClientRepository.class);
		PlayUserClient playUserClient = playUserClientRepository.findById(userToken.getUserid());
		if (playUserClient.getCards() <= 0 || playUserClient.getCards() < jun) {
			client.sendEvent(BMDataContext.CARD_CHECK, new DefineMap<String, String>().putData("status", "-1").putData("msg", "房卡不够，请充值!"));
		} else {
			client.sendEvent(BMDataContext.CARD_CHECK, new DefineMap<String, String>().putData("status", "1").putData("msg", "ok"));
		}
	}


	@OnEvent(value = "getPlayhistory")
	public void getPlayhistory(SocketIOClient client, String data) {

		long tid = System.currentTimeMillis();
		Map<String,String> map = JSONObject.parseObject(data,Map.class);
		int startPage = 0,pageSize = 10;
		Integer roomId = null;
		String token = null;
		if(map != null && !map.isEmpty()){
			startPage = StringUtils.isNotEmpty(map.get("startPage"))?Integer.parseInt(map.get("startPage")) : startPage;
			pageSize = StringUtils.isNotEmpty(map.get("pageSize"))?Integer.parseInt(map.get("pageSize")) : startPage;
			roomId = StringUtils.isNotEmpty(map.get("roomId"))?Integer.parseInt(map.get("roomId")) : null;
			token = map.get("token");
		}
		logger.info("tid:{}, 获取战绩  data:{},token:{}",tid,data,token);
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				HouseCardHandlerService cardHandlerService = BMDataContext.getContext().getBean("houseCardHandlerService",HouseCardHandlerService.class);
				StandardResponse response = cardHandlerService.queryUserFlow(userToken.getUserid(),startPage,pageSize,roomId);
				logger.info("tid:{} 返回数据 data:{}",tid,response.toJSON());
				client.sendEvent("getPlayhistory", response);
			}
		}
	}

	@OnEvent(value = "getPlayhistoryDetail")
	public void getPlayhistoryDetail(SocketIOClient client, String data) {

		long tid = System.currentTimeMillis();
		Map<String,String> map = JSONObject.parseObject(data,Map.class);
		int startPage = 0,pageSize = 10;
		Integer roomId = null;
		String token = null;
		if(map != null && !map.isEmpty()){
			startPage = StringUtils.isNotEmpty(map.get("startPage"))?Integer.parseInt(map.get("startPage")) : startPage;
			pageSize = StringUtils.isNotEmpty(map.get("pageSize"))?Integer.parseInt(map.get("pageSize")) : startPage;
			roomId = StringUtils.isNotEmpty(map.get("roomId"))?Integer.parseInt(map.get("roomId")) : null;
			token = map.get("token");
		}
		logger.info("tid:{}, 获取战绩详情  data:{},token:{}",tid,data,token);
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				HouseCardHandlerService cardHandlerService = BMDataContext.getContext().getBean("houseCardHandlerService",HouseCardHandlerService.class);
				StandardResponse response = cardHandlerService.queryUserFlow(userToken.getUserid(),startPage,pageSize,roomId);
				logger.info("tid:{} 返回数据 data:{}",tid,response.toJSON());
				client.sendEvent("getPlayhistoryDetail", response);
			}
		}
	}



	//抢地主事件
	@OnEvent(value = "docatch")
	public void onCatch(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().actionRequest(roomid, playUser, playUser.getOrgi(), true);
			}
		}
	}



	//出牌  // TODO: 2018/3/15 zcl 自己打牌调用
	@OnEvent(value = "doplaycards")
	public void onPlayCards(SocketIOClient client, String data) {// data 为打出去的牌
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		logger.info("useID:{} 自己打出去牌 data:{}",beiMiClient.getUserid(),data);
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token) && !StringUtils.isBlank(data)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
				Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
				GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi());
				if (board instanceof MaJiangBoard && "koudajiang".equals(gamePlayway.getCode())) {
					if (!((MaJiangBoard) board).isFPEnd()) {
						logger.info("not start");
						return;
					}
				}

				String[] cards = data.split(",");
				logger.info("用户打出来的牌为 data:{}", data);
				byte[] playCards = new byte[cards.length];
				for (int i = 0; i < cards.length; i++) {
					playCards[i] = Byte.parseByte(cards[i]);
				}
				BMDataContext.getGameEngine().takeCardsRequest(roomid, userToken.getUserid(), userToken.getOrgi(), false, playCards);
			}
		}
	}

	//出牌
	@OnEvent(value = "nocards")
	public void onNoCards(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().takeCardsRequest(roomid, userToken.getUserid(), userToken.getOrgi(), false, null);
			}
		}
	}

	//出牌 //todo ZCL 选着花色
	@OnEvent(value = "selectcolor")
	public void onSelectColor(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().selectColorRequest(roomid, playUser.getId(), userToken.getOrgi(), data);
			}
		}
	}

	//出牌  // TODO: 2018/3/15 吃碰杠胡方法调用
	@OnEvent(value = "selectaction")
	public void onActionEvent(SocketIOClient client, String data) {
		logger.info("sessionId:{}",client.getSessionId());
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().actionEventRequest(roomid, playUser.getId(), userToken.getOrgi(), data);
			}
		}
	}

	//抢地主事件
	@OnEvent(value = "restart")
	public void onRestart(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().restartRequest(roomid, playUser, beiMiClient, "true".equals(data));
			}
		}
	}


	//不抢/叫地主事件
	@OnEvent(value = "giveup")
	public void onGiveup(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().actionRequest(roomid, playUser, playUser.getOrgi(), false);
			}
		}
	}

	//不抢/叫地主事件
	@OnEvent(value = "cardtips")
	public void onCardTips(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(CacheHelper.getInstance().getUserId(client.getSessionId().toString()));
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi());
				BMDataContext.getGameEngine().cardTips(roomid, playUser, playUser.getOrgi(), data);
			}
		}
	}


}