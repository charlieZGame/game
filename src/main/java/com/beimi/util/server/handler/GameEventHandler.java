
package com.beimi.util.server.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.beimi.model.DefineMap;
import com.beimi.model.OutRoom;
import com.beimi.model.PlayCache;
import com.beimi.util.rules.model.*;
import com.beimi.web.model.*;
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
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	@Autowired
	public GameEventHandler(SocketIOServer server) {
		this.server = server;
	}

	@OnConnect
	public void onConnect(SocketIOClient client) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		if (beiMiClient != null && !StringUtils.isBlank(beiMiClient.getUserid())) {
			if (CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi()) != null) {
				ActionTaskUtils.sendEvent("", beiMiClient.getUserid(), null);
			}
		}
	}


	//抢地主事件   //// TODO: 2018/3/14 zcl 系统退出麻将时，会走这个方法
	@OnEvent(value = "start")
	public void onStart(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				BMDataContext.getGameEngine().gameRequest(playUser.getId(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), playUser, beiMiClient);
			}
		}
	}

	//玩家离开
	@OnEvent(value = "leave")
	public void onLeaveL(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString(), true);
			}
		}
	}


	@OnEvent(value = "leaveroom")
	public void applyLeaveRoom(SocketIOClient client, String data) {
		try {

			long tid = System.currentTimeMillis();
			logger.info("data:{} 离场请求数据信息",data);
			Map<String, Object> map = JSONObject.parseObject(data, Map.class);
			BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
			logger.info("userId:{},room:{} 离场请求用户信息",beiMiClient.getUserid(),beiMiClient.getRoom(),beiMiClient.getToken());
			if ("1".equals(map.get("type"))) {
				logger.info("userId:{},room:{} 强制离场",beiMiClient.getUserid(),beiMiClient.getRoom(),beiMiClient.getToken());
				//// TODO: 2018/3/16  标记用户是不友好用户 这个逻辑以后处理
				//signUserUnfriendly(tid,client,data);
				onLeave(client, data);
				onCommand(client,data);
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
				onLeaveL(client,data);
			}
		} catch (RuntimeException e) {
			logger.error("请求退出异常", e);
			throw e;
		}
	}

	private void userNormalExist(BeiMiClient beiMiClient) {

		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, beiMiClient.getOrgi());
		List<PlayUserClient> playUserClients = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), beiMiClient.getOrgi());

		PlayUserClient temp = null;
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
			logger.info(" 人数未达到发出解散 userId:{}", playUserClient.getId());
			BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
			tmpClient.getClient().sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, gamePlayers);

		}
	}


	/**
	 *
	 * @param client
	 * @param map
     */
	private void acceptLeaveRoom(Long tid,SocketIOClient client,Map<String,Object> map){

		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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
			tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new PlayerChartMsg<String>(playUser.getId(), playUser.getUsername(), playUserClient.getId(), playUserClient.getUsername(), "3", "用户 [" + playUser.getUsername() + "]请求离场"));
		}

	}


	/**
	 *
	 * @param client
	 * @param response
     */
	private boolean applyLeaveRoomResponse(long tid,SocketIOClient client, Map<String,Object> response) {

		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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
				tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new DefineMap<String, String>().putData("type", "5").putData("isAgree", "1").putData("msg", "小伙伴都同意解散房间"));
			}

			return true;
		}
	}


	@OnEvent(value = "forceleaveroom")
	public void forceLeaveRoom(SocketIOClient client, String data) {
		onLeave(client,data);
	}

	
	private void signUserUnfriendly(long tid,SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(beiMiClient.getRoom(), beiMiClient.getOrgi());
		PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
		logger.info("tid:{} 玩家强制离场 playerList:{}", tid, playerList.size());
		for (PlayUserClient playUserClient : playerList) {
			logger.info("tid:{} 玩家强制离场 userId:{}", tid, playUserClient.getId());
			BeiMiClient tmpClient = NettyClients.getInstance().getClient(playUserClient.getId());
			tmpClient.getClient().sendEvent(BMDataContext.APPLY_LEAVE_ROOM, new PlayerChartMsg<String>(playUser.getId(), playUser.getUsername(), playUserClient.getId(), playUserClient.getUsername(), "6", "玩家强制离场"));
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
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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
				PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				beiMiClient.setClient(client);
				beiMiClient.setUserid(userClient.getId());
				beiMiClient.setSession(client.getSessionId().toString());
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

				BMDataContext.getGameEngine().gameRequest(userToken.getUserid(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), userClient, beiMiClient);
			}
		}
	}


	//抢地主事件
	// zcl 创建房间 调用此方法
	@OnEvent(value = "gamestatus")
	public void onGameStatus(SocketIOClient client, String data) {
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


	//抢地主事件
	// zcl 查找房间
	@OnEvent(value = "searchroom")
	public void onSearchRoom(SocketIOClient client, String data) {
		SearchRoom searchRoom = JSON.parseObject(data, SearchRoom.class);
		GamePlayway gamePlayway = null;
		SearchRoomResult searchRoomResult = null;
		boolean joinRoom = false;
		if (searchRoom != null && StringUtils.isNotEmpty(searchRoom.getUserid())) {
			GameRoomRepository gameRoomRepository = BMDataContext.getContext().getBean(GameRoomRepository.class);
			PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(searchRoom.getUserid(), searchRoom.getOrgi());
			if (playUser != null) {
				GameRoom gameRoom = null;
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
					if (playerList.size() < gamePlayway.getPlayers()) {
						BMDataContext.getGameEngine().joinRoom(gameRoom, playUser, playerList);
						joinRoom = true;
					}
					/**
					 * 获取的玩法，将玩法数据发送给当前请求的玩家
					 */
				}
			}
		}
		if (gamePlayway != null) {
			//通知客户端
			if (joinRoom == true) {        //加入成功 ， 是否需要输入加入密码？
				searchRoomResult = new SearchRoomResult(gamePlayway.getId(), gamePlayway.getCode(), BMDataContext.SearchRoomResultType.OK.toString());
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
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		if (beiMiClient != null) {
			/**
			 * 玩家离线
			 */
			PlayUserClient playUserClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi());
			if (playUserClient != null) {
				if (BMDataContext.GameStatusEnum.PLAYING.toString().equals(playUserClient.getGamestatus())) {
					GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.OFFLINE.toString(), true);
				} else {
					CacheHelper.getApiUserCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi());
					if (CacheHelper.getGamePlayerCacheBean().getPlayer(beiMiClient.getUserid(), beiMiClient.getOrgi()) != null) {
						CacheHelper.getGamePlayerCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi());
					}
					CacheHelper.getRoomMappingCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi());
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
	@OnEvent(value = "leave")
	public void onLeave(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString(), true);
			}
		}
	}





	//抢地主事件
	@OnEvent(value = "docatch")
	public void onCatch(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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

	//不抢/叫地主事件
	@OnEvent(value = "giveup")
	public void onGiveup(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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


	//出牌  // TODO: 2018/3/15 zcl 自己打牌调用
	@OnEvent(value = "doplaycards")
	public void onPlayCards(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
		String token = beiMiClient.getToken();
		if (!StringUtils.isBlank(token) && !StringUtils.isBlank(data)) {
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI);
			if (userToken != null) {
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi());
				String[] cards = data.split(",");

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
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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

	//出牌
	@OnEvent(value = "selectcolor")
	public void onSelectColor(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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

	//出牌  // TODO: 2018/3/15 碰方法调用
	@OnEvent(value = "selectaction")
	public void onActionEvent(SocketIOClient client, String data) {
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(client.getSessionId().toString());
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


}