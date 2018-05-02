package com.beimi.util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.beimi.util.rules.model.MaJiangBoard;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import com.beimi.config.web.model.Game;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGame;
import com.beimi.core.engine.game.iface.ChessGame;
import com.beimi.core.engine.game.impl.DizhuGame;
import com.beimi.core.engine.game.impl.MaJiangGame;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.core.engine.game.model.Playway;
import com.beimi.core.engine.game.model.Type;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Action;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.AccountConfig;
import com.beimi.web.model.AiConfig;
import com.beimi.web.model.BeiMiDic;
import com.beimi.web.model.GameConfig;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GamePlaywayGroup;
import com.beimi.web.model.GamePlaywayGroupItem;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.Subsidy;
import com.beimi.web.model.SysDic;
import com.beimi.web.service.repository.es.SubsidyESRepository;
import com.beimi.web.service.repository.jpa.GamePlaywayGroupItemRepository;
import com.beimi.web.service.repository.jpa.GamePlaywayGroupRepository;
import com.beimi.web.service.repository.jpa.GamePlaywayRepository;

public class GameUtils {


	private static Logger logger = LoggerFactory.getLogger(GameUtils.class);

	
	private static Map<String,ChessGame> games = new HashMap<String,ChessGame>();
	static{
		games.put("dizhu", new DizhuGame()) ;
		games.put("majiang", new MaJiangGame()) ;
		games.put("koudajiang", new MaJiangGame()) ;
	}
	
	public static Game getGame(String playway ,String orgi){
		GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(playway, orgi) ;
		Game game = null ;
		if(gamePlayway!=null){
			SysDic dic = (SysDic) CacheHelper.getSystemCacheBean().getCacheObject(gamePlayway.getGame(), gamePlayway.getOrgi()) ;
			if(dic.getCode().equals("dizhu") || gamePlayway.getCode().equals("dizhu")){
				game = (Game) BMDataContext.getContext().getBean("dizhuGame") ;
			}else if(dic.getCode().equals("majiang") || gamePlayway.getCode().equals("majiang")){
				game = (Game) BMDataContext.getContext().getBean("majiangGame") ;
			}else {
				game = (Game) BMDataContext.getContext().getBean("majiangGame") ;
			}
		}


		return game;
	}
	
	/**
	 * 移除GameRoom
	 * @param orgi
	 */
	public static void removeGameRoom(String roomid,String playway,String orgi){
		CacheHelper.getQueneCache().delete(roomid);
	}
	
	/**
	 * 更新玩家状态
	 * @param userid
	 * @param orgi
	 */
	public static void updatePlayerClientStatus(String userid , String orgi , String status,boolean isNeedClean){
		PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userid, orgi) ;
		if(playUser!=null){
		//	playUser.setPlayertype(status);//托管玩家
		//	CacheHelper.getApiUserCacheBean().put(userid,playUser , orgi);

			if(playUser!=null && isNeedClean){
				//playUser = (PlayUserClient) CacheHelper.getGamePlayerCacheBean().getPlayer(userid, orgi) ;
				//// TODO: 2018/3/13 ZCL

				logger.info("qingli 1");
				List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(playUser.getRoomid(), playUser.getOrgi()) ;

				if(players == null || players.size() == 0){
					return ;
				}
				logger.info("qingli 2");
				for(PlayUserClient playUserClient : players) {
					CacheHelper.getGamePlayerCacheBean().delete(playUserClient.getId(), orgi) ;
					CacheHelper.getRoomMappingCacheBean().delete(playUserClient.getId(), orgi) ;
					CacheHelper.getGamePlayerCacheBean().delete(playUserClient.getId(), orgi);
					/*if (playUserClient.getId().equals(playUser.getId())) {
						temp = playUserClient;
					}*/
				}
				//players.remove(temp);
				CacheHelper.getGamePlayerCacheBean().delete(playUser.getRoomid(), playUser.getOrgi());

				/**
				 * 检查，如果房间没   ，就可以解散房间了
				 */
			//// TODO: 2018/3/16 zcl 这个逻辑暂时去掉
			/*	if(playUser!= null && !StringUtils.isBlank(playUser.getRoomid())){
					GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(playUser.getRoomid(), orgi) ;
					if(gameRoom.getMaster().equals(playUser.getId())){
						*//**
						 * 解散房间，应该需要一个专门的 方法来处理，别直接删缓存了，这样不好！！！
						 *//*
						BMDataContext.getGameEngine().dismissRoom(gameRoom, userid, orgi);
					}
				}*/


			}
		}
	}
	public static Subsidy subsidyPlayerClient(String userid , String orgi) {
		Subsidy subsidy = null ;
		PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userid, orgi) ;
		if(playUser!=null){
			GameConfig gameConfig = (GameConfig) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.getGameConfig(orgi) , orgi);
			if(gameConfig!=null && gameConfig.isSubsidy()) {
				/**
				 * 启用了 破产补助功能，需要校验改玩家当天是否还有申请破产补助的资格 ， 无论是否有资格，都需要给玩家一个回复消息，
				 * 如果有申请资格，需要查询破产补助记录表，按天，则直接补助，并通知玩家 PVA信息更新，如果没有资格，则更新PVA信息，并给出提示消息
				 */
				
				SubsidyESRepository subsidyRes = BMDataContext.getContext().getBean(SubsidyESRepository.class) ;
				int times = subsidyRes.countByPlayeridAndOrgiAndDay(userid, orgi, UKTools.getDay())  ;
				if(times < gameConfig.getSubtimes()) { //允许补助
					subsidy = new Subsidy();
					subsidy.setCreatetime(new Date());
					subsidy.setDay(UKTools.getDay());
					subsidy.setPlayerid(userid);
					subsidy.setOrgi(orgi);
					subsidy.setCommand(BMDataContext.CommandMessageType.SUBSIDY.toString());
					subsidy.setFrequency(times+1);
					UKTools.published(subsidy, subsidyRes);
				}
			}
		}
		return subsidy;
	}
	/**
	 * 创建一个AI玩家
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static PlayUserClient create(PlayUser player,String playertype) {
		return create(player, null , null , playertype) ;
	}
	/**
	 * 开始游戏，根据玩法创建游戏 对局
	 * @return
	 */
	public static Board playGame(List<PlayUserClient> playUsers , GameRoom gameRoom , String banker , int cardsnum){
		Board board = null ;
		GamePlayway gamePlayWay = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi()) ;
		if(gamePlayWay!=null){
			ChessGame chessGame = games.get(gamePlayWay.getCode());
			if(chessGame!=null){
				board = chessGame.process(playUsers, gameRoom, gamePlayWay , banker, cardsnum);
			}
		}
		return board;
	}
	
	/**
	 * 创建一个普通玩家
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static PlayUserClient create(PlayUser player , IP ipdata , HttpServletRequest request ) throws IllegalAccessException, InvocationTargetException{
		return create(player, ipdata, request, BMDataContext.PlayerTypeEnum.NORMAL.toString()) ;
	}
	
	public static byte[] reverseCards(byte[] cards) {  
		byte[] target_cards = new byte[cards.length];  
		for (int i = 0; i < cards.length; i++) {  
			// 反转后数组的第一个元素等于源数组的最后一个元素：  
			target_cards[i] = cards[cards.length - i - 1];  
		}  
		return target_cards;  
	}  
	
	/**
	 * 注册用户
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static PlayUserClient create(PlayUser player , IP ipdata , HttpServletRequest request , String playertype){
		PlayUserClient playUserClient = null ;
		if(player!= null){
    		if(StringUtils.isBlank(player.getUsername()+"")){
    			//player.setUsername("Guest_"+Base62.encode(UKTools.getUUID().toLowerCase()));
    		}
    		if(!StringUtils.isBlank(player.getPassword())){
    			player.setPassword(UKTools.md5(player.getPassword()));
    		}else{
    			player.setPassword(UKTools.md5(RandomKey.genRandomNum(6)));//随机生成一个6位数的密码 ，备用
    		}
    		player.setPlayertype(playertype);	//玩家类型
    		player.setCreatetime(new Date());
    		player.setUpdatetime(new Date());
    		player.setLastlogintime(new Date());
    		
    		BrowserClient client = UKTools.parseClient(request) ;
    		player.setOstype(client.getOs());
    		player.setBrowser(client.getBrowser());
    		if(request!=null){
	    		String usetAgent = request.getHeader("User-Agent") ;
	    		if(!StringUtils.isBlank(usetAgent)){
	    			if(usetAgent.length() > 255){
	    				player.setUseragent(usetAgent.substring(0,250));
	    			}else{
	    				player.setUseragent(usetAgent);
	    			}
	    		}
    		}
    		if(ipdata!=null){
	    		player.setRegion(ipdata.getRegion());
				player.setCountry(ipdata.getCountry());
				player.setProvince(ipdata.getProvince());
				player.setCity(ipdata.getCity());
				player.setIsp(ipdata.getIsp());
    		}
			
    		
    		player.setOrgi(BMDataContext.SYSTEM_ORGI);
    		AiConfig aiConfig = CacheConfigTools.getAiConfig(player.getOrgi()) ;
    		
			if(BMDataContext.PlayerTypeEnum.AI.toString().equals(playertype) && aiConfig != null){
				player.setGoldcoins(aiConfig.getInitcoins());
    			player.setCards(aiConfig.getInitcards());
    			player.setDiamonds(aiConfig.getInitdiamonds());
			}else{
	    		AccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI) ;
	    		if(config!=null){
	    			player.setGoldcoins(config.getInitcoins());
	    			player.setCards(config.getInitcards());
	    			player.setDiamonds(config.getInitdiamonds());
	    		}
			}
    		
    		if(!StringUtils.isBlank(player.getId())){
    			playUserClient  = new PlayUserClient() ;
    			try {
					BeanUtils.copyProperties(playUserClient , player);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
    		}
    	}
		return playUserClient ;
	}
	
	/**
	 * 获取游戏全局配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<GamePlayway> playwayConfig(String gametype,String orgi){
		List<GamePlayway> gamePlayList = (List<GamePlayway>) CacheHelper.getSystemCacheBean().getCacheObject(gametype+"."+BMDataContext.ConfigNames.PLAYWAYCONFIG.toString(), orgi) ;
		if(gamePlayList == null){
			gamePlayList = BMDataContext.getContext().getBean(GamePlaywayRepository.class).findByOrgiAndTypeid(orgi, gametype , new Sort(Sort.Direction.ASC, "sortindex")) ;
			CacheHelper.getSystemCacheBean().put(gametype+"."+BMDataContext.ConfigNames.PLAYWAYCONFIG.toString() , gamePlayList , orgi) ;
		}
		return gamePlayList ;
	}
	/**
	 * 获取房卡游戏的自定义配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<GamePlaywayGroup> playwayGroupsConfig(String orgi){
		List<GamePlaywayGroup> gamePlaywayGroupsList = (List<GamePlaywayGroup>) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.ConfigNames.PLAYWAYGROUP.toString(), orgi) ;
		if(gamePlaywayGroupsList == null){
			gamePlaywayGroupsList = BMDataContext.getContext().getBean(GamePlaywayGroupRepository.class).findByOrgi(orgi, new Sort(Sort.Direction.ASC, "sortindex")) ;
			CacheHelper.getSystemCacheBean().put(BMDataContext.ConfigNames.PLAYWAYGROUP.toString() , gamePlaywayGroupsList , orgi) ;
		}
		return gamePlaywayGroupsList ;
	}

	/**
	 * 获取房卡游戏的自定义配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<GamePlaywayGroupItem> playwayGroupItemConfig(String orgi){
		List<GamePlaywayGroupItem> gamePlaywayGroupsList = (List<GamePlaywayGroupItem>) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.ConfigNames.PLAYWAYGROUPITEM.toString(), orgi) ;
		if(gamePlaywayGroupsList == null){
			gamePlaywayGroupsList = BMDataContext.getContext().getBean(GamePlaywayGroupItemRepository.class).findByOrgi(orgi, new Sort(Sort.Direction.ASC, "sortindex")) ;
			CacheHelper.getSystemCacheBean().put(BMDataContext.ConfigNames.PLAYWAYGROUPITEM.toString() , gamePlaywayGroupsList , orgi) ;
		}
		return gamePlaywayGroupsList ;
	}



	/**
	 * 
	 * @param gametype
	 * @param orgi
	 */
	public static void cleanPlaywayCache(String gametype,String orgi){
		CacheHelper.getSystemCacheBean().delete(gametype+"."+BMDataContext.ConfigNames.PLAYWAYCONFIG.toString(), orgi) ;
	}
	/**
	 * 封装Game信息，基于缓存操作
	 * @param gametype
	 * @return
	 */
	public static List<BeiMiGame> games(String gametype){
		List<BeiMiGame> beiMiGameList = new ArrayList<BeiMiGame>(); //构造游戏列表
		if(!StringUtils.isBlank(gametype)){
			/**
			 * 找到游戏配置的 模式 和玩法，如果多选，则默认进入的是 大厅模式，如果是单选，则进入的是选场模式
			 */
			String[] games = gametype.split(",") ;
			for(String game : games){
				BeiMiGame beiMiGame = new BeiMiGame();
				for(SysDic sysDic : BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC)){
					if(sysDic.getId().equals(game)){
						beiMiGame.setName(sysDic.getName());
						beiMiGame.setId(sysDic.getId());
						beiMiGame.setCode(sysDic.getCode());
						
						List<SysDic> gameModelList = BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC, game) ;
						for(SysDic gameModel : gameModelList){
							Type type = new Type(gameModel.getId(), gameModel.getName() , gameModel.getCode()) ;
							beiMiGame.getTypes().add(type) ;
							List<GamePlayway> gamePlaywayList = playwayConfig(gameModel.getId(), gameModel.getOrgi()) ;

							List<GamePlaywayGroup> gamePlaywayGroups = playwayGroupsConfig(gameModel.getOrgi()) ;
							List<GamePlaywayGroupItem> gamePlaywayGroupItems = playwayGroupItemConfig(gameModel.getOrgi()) ;


							for(GamePlayway gamePlayway : gamePlaywayList){
								Playway playway = new Playway(gamePlayway.getId(), gamePlayway.getName() , gamePlayway.getCode(), gamePlayway.getScore() , gamePlayway.getMincoins(), gamePlayway.getMaxcoins(), gamePlayway.isChangecard() , gamePlayway.isShuffle()) ;
								playway.setLevel(gamePlayway.getTypelevel());

								playway.setGroups(new ArrayList<GamePlaywayGroup>());
								playway.setItems(new ArrayList<GamePlaywayGroupItem>());

								for(GamePlaywayGroup group : gamePlaywayGroups){
									if(group.getPlaywayid().equals(gamePlayway.getId())){
										playway.getGroups().add(group) ;
									}
								}

								for(GamePlaywayGroupItem item : gamePlaywayGroupItems){
									if(item.getPlaywayid().equals(gamePlayway.getId())){
										playway.getItems().add(item) ;
									}
								}

								playway.setSkin(gamePlayway.getTypecolor());
								playway.setMemo(gamePlayway.getMemo());
                                playway.setRoomtitle(gamePlayway.getRoomtitle());
								playway.setFree(gamePlayway.isFree());
								playway.setExtpro(gamePlayway.isExtpro());
								type.getPlayways().add(playway) ;
							}
						}
						beiMiGameList.add(beiMiGame) ;
					}
				}
			}
		}
		return beiMiGameList ;
	}


	public static List<Byte> recommandCards(Player player ,byte[] cards,String code){

		List<Byte> recommendCard = new ArrayList<Byte>();
		for(int i = -7 ; i < 27;i++){
			MJCardMessage message = processLaiyuanMJCard(player,cards,(byte)(i*4),true,null,code);
			if(message.isHu()){
				recommendCard.add((byte)(i*4));
			}
		}
		return recommendCard;

	}


	/**
	 *
	 * @param player
	 * @param cards
	 * @param takecard
	 * @param collections
	 * @param deal 处理自己抓牌逻辑
     * @return
     */
	public static MJCardMessage processLaiyuanMJCard(Player player ,byte[] cards , byte takecard ,boolean deal,
													 List<List<Byte>> collections,String code) {

		logger.info("userId:{},参与校验手牌为",player.getPlayuser());
		System.out.println();
		for(byte card : cards){
			System.out.print(card+",");
		}
		System.out.print("  碰杠牌: ");
		if(CollectionUtils.isNotEmpty(player.getActions())){
			for(Action action : player.getActions()) {
				System.out.print(" ["+action.getAction()+" "+action.getCard()+"] ");
			}
		}
		System.out.println();
		logger.info("useId:{} 进牌为 card:{}",player.getPlayuser(),takecard);
		System.out.println("===================");
		MJCardMessage mjCard = new MJCardMessage();
		mjCard.setCommand("action");
		mjCard.setUserid(player.getPlayuser());

		/*if(cards == null && (cards.length+takecard)<14){
			return mjCard;
		}*/

		Map<Integer, Integer> data = new HashMap<Integer, Integer>();
		List<Byte> huns = new ArrayList<Byte>();
		Map<Integer, Byte> hunMap = new HashMap<Integer,Byte>();
		Map<Integer, List<Byte>> mapCards = new HashMap<Integer,List<Byte>>();
		if (player.getPowerfullArray() != null) {
			for (byte b : player.getPowerfullArray()) {
				hunMap.put(b / 4, b);
			}
		}
		for(byte card : cards){
			if(hunMap.containsKey(card/4)){
				huns.add(card);
				continue;
			}
			GameWinCheck.exceCategory(card,mapCards);
			if(data.get(card/4) == null){
				data.put(card/4,1);
			}else{
				data.put(card/4,data.get(card/4)+1);
			}
		}
		if("majiang".equals(code) || CollectionUtils.isEmpty(player.getCoverCards())) {

			//查看碰的里边有杠的没有
			Map<Byte,Boolean> isExits = new HashMap<Byte,Boolean>();
			if(CollectionUtils.isNotEmpty(player.getActions())){
				for(Action action : player.getActions()){
					if( BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
						if(action.getCard()/4== takecard/4 && deal){
							mjCard.setCard(takecard);
							mjCard.setGang(true);
							isExits.put((byte)(action.getCard()/4),true);
						}
					}
				}
			}
				//碰杠
			for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
				//有碰可能有杠
				if (entry.getKey() == takecard / 4) {
					if (entry.getValue() == 2 && !deal && cards.length - huns.size() > 2) {
						mjCard.setCard(takecard);
						mjCard.setPeng(true);
					} else if (entry.getValue() == 3) {
						mjCard.setCard(takecard);
						mjCard.setGang(true);
						if(!isExits.containsKey(takecard/4) && !deal) {
							mjCard.setPeng(true);
						}
					}
				}
			}
		}else{
			kougangpeng(player,takecard,data,mjCard,deal);
		}

		// 处理手牌
		if(hunMap.containsKey(takecard/4)) {
			huns.add(takecard);
		}else {
			GameWinCheck.exceCategory(takecard, mapCards);
		}
		List<Byte> allCards = new ArrayList<Byte>();
		for(byte card : cards){
			allCards.add(card);
		}
		allCards.add(takecard);

		if(collections == null) {
			collections = new ArrayList<List<Byte>>();
		}


		if("majiang".equals(code)) {
			if (!GameWinCheck.haveEightAndQueBefore(player, allCards, true, hunMap)) {
				logger.info(" 牌面前校验不通过 playerId:{}", player.getPlayuser());
				mjCard.setHu(false);
				return mjCard;
			}
		}else{
			if (!GameWinCheck.haveEightAndQueBefore(player, allCards, false, hunMap)) {
				logger.info(" 牌面前校验不通过 playerId:{}", player.getPlayuser());
				mjCard.setHu(false);
				return mjCard;
			}
		}

		boolean isHu = false;
		// 七小对
		if(GameWinCheck.sevenPairCheck(player,mapCards,huns)){
			logger.info(" 七小对校验通过 playerId:{}", player.getPlayuser());
			Collections.sort(allCards);
			collections.add(allCards);
			isHu = true;
		}else if(GameWinCheck.pairWinAlgorithmSingle(mapCards, huns, collections)) {
			isHu = true;
		}

		if("majiang".equals(code)) {
			if (!isHu || !GameWinCheck.haveEightAndQueEnd(player, hunMap, null, collections, true)) {
				mjCard.setHu(false);
				player.setWin(false);
				logger.info("牌面后校验不通过 playerId:{}", player.getPlayuser());
			} else {
				logger.info("=========进行加牌操作==================");
				player.setCollections(collections);
				mjCard.setCard(takecard);
				//player.setWin(true);
				System.out.println("========================");
				mjCard.setHu(true);
				logger.info("牌面后校验通过 playerId:{}", player.getPlayuser());
			}
		}else{
			if (!isHu || !GameWinCheck.haveEightAndQueEnd(player, hunMap, null, collections, false)) {
				mjCard.setHu(false);
				//player.setWin(false);
				logger.info("牌面后校验不通过 playerId:{}", player.getPlayuser());
			} else {
				logger.info("=========进行加牌操作==================");
				player.setCollections(collections);
				//player.setWin(true);
				mjCard.setCard(takecard);
				System.out.println("========================");
				mjCard.setHu(true);
				logger.info("牌面后校验通过 playerId:{}", player.getPlayuser());
			}
		}
		return mjCard;
	}


	public static MJCardMessage processLaiyuanMJCardResult(Player player ,byte[] cards , byte takecard ,boolean deal,
													 List<List<Byte>> collections,String code) {

		logger.info("userId:{},参与校验手牌为",player.getPlayuser());
		System.out.println();
		for(byte card : cards){
			System.out.print(card+",");
		}
		System.out.print("  碰杠牌: ");
		if(CollectionUtils.isNotEmpty(player.getActions())){
			for(Action action : player.getActions()) {
				System.out.print(" ["+action.getAction()+" "+action.getCard()+"] ");
			}
		}
		System.out.println();
		logger.info("useId:{} 进牌为 card:{}",player.getPlayuser(),takecard);
		System.out.println("===================");
		MJCardMessage mjCard = new MJCardMessage();
		mjCard.setCommand("action");
		mjCard.setUserid(player.getPlayuser());

		/*if(cards == null && (cards.length+takecard)<14){
			return mjCard;
		}*/

		Map<Integer, Integer> data = new HashMap<Integer, Integer>();
		List<Byte> huns = new ArrayList<Byte>();
		Map<Integer, Byte> hunMap = new HashMap<Integer,Byte>();
		Map<Integer, List<Byte>> mapCards = new HashMap<Integer,List<Byte>>();
		if (player.getPowerfullArray() != null) {
			for (byte b : player.getPowerfullArray()) {
				hunMap.put(b / 4, b);
			}
		}
		for(byte card : cards){
			if(hunMap.containsKey(card/4)){
				huns.add(card);
				continue;
			}
			GameWinCheck.exceCategory(card,mapCards);
			if(data.get(card/4) == null){
				data.put(card/4,1);
			}else{
				data.put(card/4,data.get(card/4)+1);
			}
		}
		if("majiang".equals(code) || CollectionUtils.isEmpty(player.getCoverCards())) {

			//查看碰的里边有杠的没有
			Map<Byte,Boolean> isExits = new HashMap<Byte,Boolean>();
			if(CollectionUtils.isNotEmpty(player.getActions())){
				for(Action action : player.getActions()){
					if( BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
						if(action.getCard()/4== takecard/4 && deal){
							mjCard.setCard(takecard);
							mjCard.setGang(true);
							isExits.put((byte)(action.getCard()/4),true);
						}
					}
				}
			}
			//碰杠
			for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
				//有碰可能有杠
				if (entry.getKey() == takecard / 4) {
					if (entry.getValue() == 2 && !deal && cards.length - huns.size() > 2) {
						mjCard.setCard(takecard);
						mjCard.setPeng(true);
					} else if (entry.getValue() == 3) {
						mjCard.setCard(takecard);
						mjCard.setGang(true);
						if(!isExits.containsKey(takecard/4) && !deal) {
							mjCard.setPeng(true);
						}
					}
				}
			}
		}else{
			kougangpeng(player,takecard,data,mjCard,deal);
		}

		// 处理手牌
		if(hunMap.containsKey(takecard/4)) {
			huns.add(takecard);
		}else {
			GameWinCheck.exceCategory(takecard, mapCards);
		}
		List<Byte> allCards = new ArrayList<Byte>();
		for(byte card : cards){
			allCards.add(card);
		}
		allCards.add(takecard);

		if(collections == null) {
			collections = new ArrayList<List<Byte>>();
		}


		if("majiang".equals(code)) {
			if (!GameWinCheck.haveEightAndQueBefore(player, allCards, true, hunMap)) {
				logger.info(" 牌面前校验不通过 playerId:{}", player.getPlayuser());
				mjCard.setHu(false);
				return mjCard;
			}
		}else{
			if (!GameWinCheck.haveEightAndQueBefore(player, allCards, false, hunMap)) {
				logger.info(" 牌面前校验不通过 playerId:{}", player.getPlayuser());
				mjCard.setHu(false);
				return mjCard;
			}
		}

		boolean isHu = false;
		// 七小对
		if(GameWinCheck.sevenPairCheck(player,mapCards,huns)){
			logger.info(" 七小对校验通过 playerId:{}", player.getPlayuser());
			Collections.sort(allCards);
			collections.add(allCards);
			isHu = true;
		}else if(CollectionUtils.isNotEmpty(GameWinCheck.pairWinAlgorithm(mapCards, huns, collections))) {
			isHu = true;
		}

		if("majiang".equals(code)) {
			if (!isHu || !GameWinCheck.haveEightAndQueEnd(player, hunMap, null, collections, true)) {
				mjCard.setHu(false);
				player.setWin(false);
				logger.info("牌面后校验不通过 playerId:{}", player.getPlayuser());
			} else {
				logger.info("=========进行加牌操作==================");
				player.setCollections(collections);
				mjCard.setCard(takecard);
				//player.setWin(true);
				System.out.println("========================");
				mjCard.setHu(true);
				logger.info("牌面后校验通过 playerId:{}", player.getPlayuser());
			}
		}else{
			if (!isHu || !GameWinCheck.haveEightAndQueEnd(player, hunMap, null, collections, false)) {
				mjCard.setHu(false);
				//player.setWin(false);
				logger.info("牌面后校验不通过 playerId:{}", player.getPlayuser());
			} else {
				logger.info("=========进行加牌操作==================");
				player.setCollections(collections);
				//player.setWin(true);
				mjCard.setCard(takecard);
				System.out.println("========================");
				mjCard.setHu(true);
				logger.info("牌面后校验通过 playerId:{}", player.getPlayuser());
			}
		}
		return mjCard;
	}




	private static void kougangpeng(Player player, byte takecard ,Map<Integer, Integer> data,MJCardMessage mjCard,boolean deal) {


		// 扣空的情况在上边解决了和混子的打法一起解决了
		if (CollectionUtils.isEmpty(player.getCoverCards())) {
			return;
		}

		Map<Byte,Boolean> isExits = new HashMap<Byte,Boolean>();
		//查看碰的里边有杠的没有
		if(CollectionUtils.isNotEmpty(player.getActions())){
			for(Action action : player.getActions()){
				if( BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
					if(action.getCard()/4== takecard/4 && deal){
						mjCard.setCard(takecard);
						mjCard.setGang(true);
						isExits.put((byte)(action.getCard()/4),true);
					}
				}
			}
		}


		Map<Integer, Integer> coverCardsData = new HashMap<Integer, Integer>();
		for (Byte b : player.getCoverCards()) {
			if (coverCardsData.containsKey(b / 4)) {
				coverCardsData.put(b / 4, coverCardsData.get(b / 4) + 1);
			} else {
				coverCardsData.put(b / 4, 1);
			}
		}


		//碰杠 需要处理 手牌不能被碰掉，杠的牌可以碰。。
		for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
			if (entry.getKey() == takecard / 4) {
				if (entry.getValue() == 2 && !deal) {
					Integer isNeedNum = 2 - (coverCardsData.get(takecard / 4) == null?0:coverCardsData.get(takecard / 4));
					if (isNeedNum <= 0 || (player.getCardsArray().length - player.getCoverCards().size()) > isNeedNum) {
						mjCard.setCard(takecard);
						mjCard.setPeng(true);
					}
				} else if (entry.getValue() == 3) {
					Integer isNeedNum = 3 - (coverCardsData.get(takecard / 4) == null?0:coverCardsData.get(takecard / 4));
					if (isNeedNum <= 0 || (player.getCardsArray().length - player.getCoverCards().size()) > isNeedNum) {
						mjCard.setCard(takecard);
						mjCard.setGang(true);
						if(!isExits.containsKey(takecard/4) && !deal) {
							mjCard.setPeng(true);
						}
					}
				}
			}
		}
	}




	public static void main(String[] args) {
		long start = System.nanoTime();
		//-24,-25,-26     5,39,43,64,68,71,94,96,103,67,
		//byte[] cards = new byte[]{0,1,2,40,41,44,45, 48,49,59};
		//byte[] cards = new byte[]{10,28,30,32,56,64,15};
		//byte[] cards = new byte[]{5,11,13,44,47,55,66,70,75,81,83,92,94};
		byte[] cards = new byte[]{0,1,4,5,12,16,20,24,56,60,-20,-21,-22};
		//byte[] cards = new byte[]{44,45,48,49,52,53};
		byte takecard =-23;

	/*	List<Byte> list = new ArrayList<Byte>();
		for(byte b : cards){
			list.add(b);
		}

		GameWinCheck.newOrderHandler(list);*/



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
		byte[] powerfull = new byte[4];
		powerfull[0] = 0;
		powerfull[1] = 1;
		powerfull[2] = 4;
		powerfull[3] = 5;
		/*powerfull[1] = 53;*/
		player.setPowerfull(powerfull);
		List<Action> actions = new ArrayList<Action>();
	//	Action playerAction = new Action("adfaf" , BMDataContext.PlayerAction.GANG.toString() , (byte)3);
	//	actions.add(playerAction);
		//player.setActions(actions);

		Action playerAction2 = new Action("adfaf2" , BMDataContext.PlayerAction.PENG.toString() , (byte)31);
		actions.add(playerAction2);
		//player.setActions(actions);

		Action playerAction3 = new Action("adfaf4" , BMDataContext.PlayerAction.PENG.toString() , (byte)60);
		//actions.add(playerAction3);

		//player.setActions(actions);


		player.setCards(cards);
		/*Action playerAction3 = new Action("adfaf3" , BMDataContext.PlayerAction.PENG.toString() , (byte)96);
		actions.add(playerAction);
		player.setActions(actions);
		player.setCards(cards);*/

		List<List<Byte>> collections = new ArrayList<List<Byte>>();
		MJCardMessage mjCardMessage = GameUtils.processLaiyuanMJCardResult(player, cards, takecard,false,collections,"majiang");
		//MJCardMessage mjCardMessage = GameUtils.processLaiyuanMJCard(player, cards, takecard,false,collections,"majiang");
		System.out.println(JSONObject.toJSONString(mjCardMessage));

		if(mjCardMessage.isHu()){
			logger.info("胡牌啦");
			for(List<Byte> cds : collections){
				System.out.println("=============================");
				for(Byte card : cds){
					System.out.print(card+",");
				}
				System.out.println();
				System.out.println("=============================");
			}
			System.out.println("+++++++++++++++++++++++++");
			for(Byte card : player.getCardsArray()){
				System.out.print(card+",");
			}
			System.out.println();
			System.out.println("++++++++++++++++++++");
		}else{
			logger.info("没有胡牌");
		}

	}


	private static void checkHunHu(Map<Integer, Byte> data,byte[] cards,MJCardMessage mjCard,byte takecard,Map<Integer, Byte> hunMap,
								   int hunNum,List<Byte>resultCards,Map<Integer,Integer> que) {

		ArrayList<Byte> others = new ArrayList<Byte>();
		ArrayList<Byte> pairs = new ArrayList<Byte>();
		ArrayList<Byte> kezi = new ArrayList<Byte>();
		ArrayList<Byte> huns = new ArrayList<Byte>();


		if (hunNum == 0) {
			return;
		}

		for (byte temp : cards) {
			int key = temp / 4;            //字典编码 乘以9是因为一门9张
			if (hunMap.containsKey(key)) {
				huns.add(temp); //把混子取出来 单独存放
				continue;
			}
			generateData(data, key, pairs, others, kezi, temp);
		}

		//处理手牌
		int keyTemp = takecard / 4;            //字典编码 乘以9是因为一门9张
		if (hunMap.containsKey(keyTemp)) {
			huns.add(takecard); //把混子取出来 单独存放
		} else {
			generateData(data, keyTemp, pairs, others, kezi, takecard);
		}


		if (others.size() == 0) {
			if ((pairs.size() / 2 - 1) <= hunNum) {
				mjCard.setHu(true);
				if(CollectionUtils.isEmpty(resultCards)){
					return;
				}
				if(CollectionUtils.isNotEmpty(kezi)) {
					for (Byte kz : kezi) {
						resultCards.add(kz);
					}
				}
				if(CollectionUtils.isNotEmpty(pairs)){
					for (Byte pair : pairs) {
						resultCards.add(pair);
					}
				}
				return;
			} else {
				// TODO: 2018/3/24 其他情况待定
				if(CollectionUtils.isNotEmpty(resultCards)){
					resultCards.clear();
				}
			}
		} else if (pairs.size() > 2) {    //对子的牌大于>2张，否则肯定是不能胡的
			//检查对子里 是否有额外多出来的 牌，如果有，则进行移除
			//对对子进行拆分， 进行三个组合拼装
			List<Byte> temp = cloneList(others);
			for (int i = 0; i < pairs.size(); i++) {
				if (i % 2 == 0) {
					temp.add(pairs.get(i));
				}
			}
			//print(temp);
			processOther(temp,resultCards);
			//print(temp);
			for (int i = 0; i < pairs.size(); i++) {
				if (i % 2 == 1) {
					temp.add(pairs.get(i));
				}
			}
			//print(temp);
			processOther(temp,resultCards);
			//print(temp);
			/**
			 * 检查 temp
			 */
			/**  处理连对情况
			 * 最后一次，检查所有的值都是 2，就胡了   就是对对进行拆分，重新组合
			 */
			if (temp.size() == 2 && getKey(temp.get(0)) == getKey(temp.get(1))) {
				mjCard.setHu(true);
				if(CollectionUtils.isEmpty(resultCards)){
					return ;
				}
				resultCards.add(others.get(0));
				resultCards.add(others.get(1));
				if(CollectionUtils.isEmpty(kezi)){
					return;
				}
				if(CollectionUtils.isNotEmpty(kezi)) {
					for (Byte kz : kezi) {
						resultCards.add(kz);
					}
				}
				return;
			} else {    //还不能胡？
				if (hunProcessOthers((ArrayList)temp, huns,resultCards,null)) {
					mjCard.setHu(true);
					return;
				}else{
					if(CollectionUtils.isNotEmpty(resultCards)){
						resultCards.clear();
					}
				}
			}
		} else if (pairs.size() == 0) {
			// 三个里边找对子
			for (int i= 0 ; i< kezi.size() ; i++) {
				others.add(kezi.get(i));
				processOther(others,resultCards);
				if (others.size() == 0) {
					mjCard.setHu(true);
					if(CollectionUtils.isNotEmpty(resultCards)) {
						for (int j = i + 1; j < kezi.size(); j++) {
							resultCards.add(kezi.get(j));
						}
					}
					return;
				} else {
					// 匹配不到就移除，直到匹配上了
					others.remove(kezi.get(i));
					if(CollectionUtils.isNotEmpty(resultCards)){
						resultCards.add(kezi.get(i));
					}
				}
			}
			if (hunProcessOthers(others, huns,resultCards,que )) {
				mjCard.setHu(true);
			}else{
				if(CollectionUtils.isNotEmpty(resultCards)){
					resultCards.clear();
				}
			}
		}

	}

	private static void print(byte[] b){
		System.out.println("===================");
		for(byte _b : b){
			System.out.print(_b/4);
		}
		for(byte _b : b){
			System.out.print(_b);
		}
		System.out.println("===================");
	}

	private static void print(List<Byte> b){
		Collections.sort(b);
		System.out.println();
		System.out.println("===================");
		for(byte _b : b){
			System.out.print(_b/4+",");
		}
		System.out.println();
		for(byte _b : b){
			System.out.print(_b+",");
		}
		System.out.println();
		System.out.println("===================");


	}


	private static List<Byte> cloneList(List<Byte>src){

		if(src == null || src.size() == 0){
			return null;
		}
		List<Byte> des = new ArrayList<Byte>();
		for(byte b : src){
			des.add(b);
		}
		return des;
	}


	/**
	 *
	 * @param others
	 * @param huns
	 * @param resultCards
     * @return
     */
	private static boolean hunProcessOthers(ArrayList<Byte> others,ArrayList<Byte> huns,List<Byte>resultCards,Map<Integer,Integer> que){

		processOther(others,resultCards);
		if(others.size() == 1 && huns.size() >= 1){
			if(CollectionUtils.isNotEmpty(resultCards)){
				resultCards.addAll(others);
				resultCards.addAll(huns);
			}
			return true;
		}
		Collections.sort(others);
		int hunsize = huns.size();
		List<Byte> temp = new ArrayList<Byte>();
		for(int i = 0 ; i< others.size() && others.size() >= (i+2) && hunsize > 0;i++){
			if((others.get(i) / 36) == (others.get(i+1) / 36)&&Math.abs((others.get(i)/4 ) - (others.get(i+1)/4))<=2&&Math.abs((others.get(i)/4 ) - (others.get(i+1)/4))>0){
				temp.add(others.get(i));
				temp.add(others.get(i+1));
				byte _b = huns.remove(--hunsize);
				que.put(others.get(i) / 36,que.get(others.get(i)/36)+1);
				if(CollectionUtils.isNotEmpty(resultCards)){
					resultCards.addAll(temp);
					resultCards.add(_b);
				}
				i = i + 1;
			}else if((others.get(i) / 36) == (others.get(i+1) / 36)&&Math.abs((others.get(i)/4 ) - (others.get(i+1)/4))==0){
				i = i + 1;
			}
		}
		others.removeAll(temp);
		temp.clear();

		for(int i = 0 ; i< others.size() && others.size() >= (i+2) && hunsize > 0;i++){
			if((others.get(i) / 36) == (others.get(i+1) / 36)&&Math.abs((others.get(i)/4 ) - (others.get(i+1)/4)) == 0){
				temp.add(others.get(i));
				temp.add(others.get(i+1));
				byte _b = huns.remove(--hunsize);
				que.put(others.get(i) / 36,que.get(others.get(i)/36)+1);
				if(CollectionUtils.isNotEmpty(resultCards)){
					resultCards.addAll(temp);
					resultCards.add(_b);
				}
				i = i + 1;
			}
		}
		others.removeAll(temp);
		if(others.size() == 0){
			if(hunsize > 0){
				resultCards.addAll(huns);
			}
			for(Map.Entry<Integer,Integer> entry : que.entrySet()){
				if(entry.getValue() + hunsize >=8 ){
					return true;
				}
			}
			return false;
		}else if(others.size() == 1 && hunsize >= 1){

			//通过距离计算
		}else if(others.size() <= hunsize){
			if(CollectionUtils.isNotEmpty(resultCards)){
				resultCards.addAll(others);
			}
			if(hunsize > 0){
				resultCards.addAll(huns);
			}
			return true;
		}else if(others.size() == 2 && getKey(others.get(0)) == getKey(others.get(1))){
			if(CollectionUtils.isNotEmpty(resultCards)){
				resultCards.addAll(others);
			}
			if(hunsize > 0){
				resultCards.addAll(huns);
			}
			return true;
		}

		return false;
	}


	private static void commonValidate(MJCardMessage mjCard ,List<Byte> pairs,List<Byte> others,List<Byte> kezi,List<Byte> resultCards){

		if(others.size() == 0){
			if(pairs.size() == 2 || pairs.size() == 14){//有一对，胡
				mjCard.setHu(true);
				if(CollectionUtils.isEmpty(resultCards)){
					return;
				}
				if(CollectionUtils.isNotEmpty(kezi)) {
					for (Byte kz : kezi) {
						resultCards.add(kz);
					}
				}
				if(CollectionUtils.isNotEmpty(pairs)){
					for (Byte pair : pairs) {
						resultCards.add(pair);
					}
				}
			}else{	//然后分别验证 ，只有一种特殊情况，的 3连对，可以组两个顺子，也可以胡 ， 其他情况就呵呵了
				if(CollectionUtils.isNotEmpty(resultCards)){
					resultCards.clear();
				}
			}
		}else if(pairs.size() > 2){	//对子的牌大于>2张，否则肯定是不能胡的
			//检查对子里 是否有额外多出来的 牌，如果有，则进行移除
			//对对子进行拆分， 进行三个组合拼装
			for(int i=0 ; i<pairs.size() ; i++){
				if(i%2==0){
					others.add(pairs.get(i)) ;
				}
			}
			processOther(others,resultCards);
			for(int i=0 ; i<pairs.size() ; i++){
				if(i%2==1){
					others.add(pairs.get(i)) ;
				}
			}
			processOther(others,resultCards);

			/**
			 * 检查 others
			 */
			/**  处理连对情况
			 * 最后一次，检查所有的值都是 2，就胡了   就是对对进行拆分，重新组合
			 */
			if(others.size() == 2 && getKey(others.get(0)) == getKey(others.get(1))){
				mjCard.setHu(true);
				if(CollectionUtils.isEmpty(resultCards)){
					return ;
				}
				resultCards.add(others.get(0));
				resultCards.add(others.get(1));
				if(CollectionUtils.isEmpty(kezi)){
					return;
				}
				if(CollectionUtils.isNotEmpty(kezi)) {
					for (Byte kz : kezi) {
						resultCards.add(kz);
					}
				}
			}else{	//还不能胡？
				if(CollectionUtils.isNotEmpty(resultCards)){
					resultCards.clear();
				}
			}
		}else if(pairs.size() == 0){
			// 三个里边找对子
			for(int i = 0 ; i < kezi.size() ; i++){
				others.add(kezi.get(i)) ;
				processOther(others,resultCards);
				// 用刻子消除单个张牌，如果全部消除，标示胡牌，如果没有消息走else 标示没有胡牌
				if(others.size() == 0){
					mjCard.setHu(true);
					if(CollectionUtils.isNotEmpty(resultCards)) {
					     for(int j = i+1;j<kezi.size();j++){
							 resultCards.add(kezi.get(j));
						 }
					}
					break ;
				}else{
					// 匹配不到就移除，直到匹配上了
					if(others.remove(kezi.get(i)) && CollectionUtils.isNotEmpty(resultCards)){
						resultCards.add(kezi.get(i));
					}
				}
			}
			if(!mjCard.isHu() && CollectionUtils.isNotEmpty(resultCards)){
				resultCards.clear();
			}
		}
	}


	private static void generateData(Map<Integer, Byte> data,int key,List<Byte> pairs,List<Byte> others,List<Byte> kezi,byte temp) {
		if (data.get(key) == 1) {
			others.add(temp);
		} else if (data.get(key) == 2) {
			pairs.add(temp);
		} else if (data.get(key) == 3) {
			kezi.add(temp);
		}
	}


	/**
	 * 麻将的出牌判断，杠碰吃胡
	 * @param cards
	 * @param deal	是否抓牌
	 * @return
	 */
	public static MJCardMessage processMJCard(Player player,byte[] cards , byte takecard , boolean deal,List<Byte> resultCards) {
		MJCardMessage mjCard = new MJCardMessage();
		mjCard.setCommand("action");
		mjCard.setUserid(player.getPlayuser());
		Map<Integer, Byte> data = new HashMap<Integer, Byte>();
		boolean que = false;
		if (cards.length > 0) {
			for (byte temp : cards) {
				int value = (temp % 36) / 4;            //牌面值
				int rote = temp / 36;                //花色
				int key = value + 9 * rote;        //生成去重KEY
				if (rote == player.getColor()) {
					que = true;
				}
				if (data.get(key) == null || rote == player.getColor()) {
					data.put(key, (byte) 1);
				} else {
					data.put(key, (byte) (data.get(key) + 1));
				}

				if (data.get(key) == 4 && deal == true) {    //自己发牌的时候，需要先判断是否有杠牌
					mjCard.setGang(true);
					mjCard.setCard(temp);
				}
			}
			/**
			 * 检查是否有 杠碰
			 */
			int value = (takecard % 36) / 4;
			int key = value + 9 * (takecard / 36);
			Byte card = data.get(key);
			if (card != null) {
				if (card == 2 && deal == false) {
					//碰
					mjCard.setPeng(true);
					mjCard.setCard(takecard);
				} else if (card == 3) {
					//明杠
					mjCard.setGang(true);
					mjCard.setCard(takecard);
				}
			}

			/**
			 * 检查是否有弯杠 , 碰过 AND 自己抓了一张碰过的牌
			 */
			int rote = takecard / 36;
			if (deal == true && rote != player.getColor()) {
				for (Action action : player.getActions()) {
					if (action.getCard() == takecard && action.getAction().equals(BMDataContext.PlayerAction.PENG.toString())) {
						//
						mjCard.setGang(true);
						break;
					}
				}
			}
			/**
			 * 后面胡牌判断使用  把新发的一张牌补充进去
			 */
			if (data.get(key) == null) {
				data.put(key, (byte) 1);
			} else {
				data.put(key, (byte) (data.get(key) + 1));
			}
		}
		if (que == false) {
			/**
			 * 检查是否有 胡 , 胡牌算法，先移除 对子
			 */
			List<Byte> pairs = new ArrayList<Byte>();
			List<Byte> others = new ArrayList<Byte>();
			List<Byte> kezi = new ArrayList<Byte>();
			/**
			 * 处理玩家手牌
			 */
			for (byte temp : cards) {
				int key = (((temp % 36) / 4) + 9 * (int) (temp / 36));            //字典编码 乘以9是因为一门9张
				generateData(data, key, pairs, others, kezi, temp);
			}
			/**
			 * 处理一个单张  在上边 已经将单张加入到 data 里边去了
			 */
			int key = (((takecard % 36) / 4) + 9 * (int) (takecard / 36));            //字典编码
			generateData(data, key, pairs, others, kezi, takecard);

			/**
			 * 是否有胡
			 */
			processOther(others,resultCards);
			commonValidate(mjCard, pairs, others, kezi,resultCards);
		}
		if (mjCard.isHu()) {
			mjCard.setCard(takecard);
			for (byte temp : cards) {
				System.out.print(temp + ",");
			}
			System.out.println(takecard);
		}
		return mjCard;
	}

	private static void processOther(List<Byte> others,List<Byte> resultCards) { // 处理单张牌的逻辑
		Collections.sort(others);
		for (int i = 0; i < others.size() && others.size() > (i + 2); ) {
			byte color = (byte) (others.get(i) / 36);                            //花色
			byte key = getKey(others.get(i));
			byte nextcolor = (byte) (others.get(i) / 36);        //todo貌似有逻辑错误，这里应该是下一张排,         // 花色
			byte nextkey = getKey(others.get(i + 1));
			if (color == nextcolor && nextkey == key + 1) {
				nextcolor = (byte) (others.get(i + 2) / 36);                            //花色
				nextkey = getKey(others.get(i + 2));
				if (color == nextcolor && nextkey == key + 2) {        //数字，移除掉
					if (CollectionUtils.isEmpty(resultCards)) {
						others.remove(i + 2);
						others.remove(i + 1);
						others.remove(i);
					} else {
						resultCards.add(others.remove(i + 2));
						resultCards.add(others.remove(i + 1));
						resultCards.add(others.remove(i));
					}
				} else {
					i = i + 2;
				}
			} else {
				i = i + 1;    //下一步
			}
		}
	}
	
	public static byte getKey(byte card){
		byte value = (byte) ((card%36) / 4) ;			//牌面值
		int rate = card / 36 ;							//花色
		byte key = (byte) (value + 9 * rate) ;			//字典编码
		return key ;
	}
	
	/**
	 * 麻将的出牌判断，杠碰吃胡
	 * @param cards
	 * @return
	 */
	public static Map<Integer,Object> getGangCard(byte[] cards,List<Action> actions){
		Map<Integer, Byte> data = new HashMap<Integer, Byte>();
		logger.info("获取杠手牌开始");
		System.out.println();
		for(byte card : cards){
			System.out.print(card+",");
		}
		System.out.println();
		logger.info("获取杠手牌完成");
		logger.info("获取杠牌开始");
		System.out.println();
		if(CollectionUtils.isNotEmpty(actions)) {
			for (Action action : actions) {
				System.out.print(action.getCard() + ",");
			}
		}
		System.out.println();
		logger.info("获取杠牌完成");

	/*	List<Byte> tempList = new ArrayList<Byte>();
		Map<Integer,String> tempMap = new HashMap<Integer,String>();
		if(CollectionUtils.isNotEmpty(actions)) {
			for(Action action : actions) {
				if(BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
					tempList.add((byte)(action.getCard()/4*4));
					tempList.add((byte)(action.getCard()/4*4+1));
					tempList.add((byte)(action.getCard()/4*4+2));
					tempMap.put(action.getCard()/4*4,action.getSrcUserId());
				}
			}
		}*/
	/*	if(tempList.size() > 0){
			byte[] newCards = new byte[cards.length + tempList.size()];
			System.arraycopy(cards,0,newCards,0,cards.length);
			for(int i = 0;i<tempList.size();i++){
				newCards[cards.length+i] = tempList.get(i);
			}
			cards = newCards;
		}*/
		Map<Integer,Object> map = new HashMap<Integer,Object>();

		for(byte temp : cards){
			int value = temp / 4 ;			//牌面值
			if(data.get(value) == null){
				data.put(value , (byte)1) ;
			}else{
				data.put(value, (byte)(data.get(value)+1)) ;
			}
			if(data.get(value) == 4){	//自己发牌的时候，需要先判断是否有杠牌
				map.put(1,true);
				map.put(2,temp);
				return map;
			}
		}
/*
		for(byte temp : cards){
			int value = (temp%36) / 4 ;			//牌面值
			int rote = temp / 36 ;				//花色
			int key = value + 9 * rote ;		//
			if(data.get(key) == null){
				data.put(key , (byte)1) ;
			}else{
				data.put(key, (byte)(data.get(key)+1)) ;
			}
			if(data.get(key) == 4){	//自己发牌的时候，需要先判断是否有杠牌
				map.put(1,true);
				map.put(2,temp);
				break ;
			}
		}
*/


		List<Byte> tempList = new ArrayList<Byte>();
		Map<Integer,String> tempMap = new HashMap<Integer,String>();
		if(CollectionUtils.isNotEmpty(actions)) {
			for(Action action : actions) {
				if(BMDataContext.PlayerAction.PENG.toString().equals(action.getAction())){
					tempList.add((byte)(action.getCard()/4*4));
					tempList.add((byte)(action.getCard()/4*4+1));
					tempList.add((byte)(action.getCard()/4*4+2));
					tempMap.put((action.getCard()/4)*4,action.getSrcUserId());
				}
			}
		}
		if(CollectionUtils.isEmpty(tempList)){
			return map;
		}
		for(byte temp : tempList){
			int value = temp / 4 ;			//牌面值
			if(data.get(value) == null){
				data.put(value , (byte)1) ;
			}else{
				data.put(value, (byte)(data.get(value)+1)) ;
			}
			if(data.get(value) == 3 && data.get(value) >= 1){	//自己发牌的时候，需要先判断是否有杠牌
				map.put(1,false);
				map.put(2,temp);
				map.put(6,tempMap.get((temp/4)*4));
				break ;
			}
		}
		return map;
	}
	/**
	 * 定缺方法，计算最少的牌  //如果用户没有选缺 系统自动选缺
	 * @param cards
	 * @return
	 */
	public static int selectColor(byte[] cards){
		Map<Integer, Byte> data = new HashMap<Integer, Byte>();
		for(byte temp : cards){
			int key = temp / 36 ;				//花色
			if(data.get(key) == null){
				data.put(key , (byte)1) ;
			}else{
				data.put(key, (byte)(data.get(key)+1)) ;
			}
		}
		int color = 0 , cardsNum = 0 ;
		if(data.get(0)!=null){
			cardsNum = data.get(0) ;
			if(data.get(1) == null){
				color = 1 ;
			}else{
				if(data.get(1) < cardsNum){
					cardsNum = data.get(1) ;
					color = 1 ;
				}
				if(data.get(2)==null){
					color = 2 ;
				}else{
					if(data.get(2) < cardsNum){
						cardsNum = data.get(2) ;
						color = 2 ;
					}
				}
			}
		}
		return color ;
	}
}
