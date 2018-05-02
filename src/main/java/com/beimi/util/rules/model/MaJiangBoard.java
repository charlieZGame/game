package com.beimi.util.rules.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson.JSONObject;
import com.beimi.backManager.HouseCardHandlerService;
import com.beimi.model.GameResultSummary;
import com.beimi.rule.HuValidate;
import com.beimi.rule.ReturnResult;
import com.beimi.util.GameWinCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.core.engine.game.model.Summary;
import com.beimi.core.engine.game.model.SummaryPlayer;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * 牌局，用于描述当前牌局的内容 ， 
 * 1、随机排序生成的 当前 待起牌（麻将、德州有/斗地主无）
 * 2、玩家 手牌
 * 3、玩家信息
 * 4、当前牌
 * 5、当前玩家
 * 6、房间/牌桌信息
 * 7、其他附加信息
 * 数据结构内存占用 78 byte ， 一副牌序列化到 数据库 占用的存储空间约为 78 byt， 数据库字段长度约为 20
 *
 * @author iceworld
 *
 */
public class MaJiangBoard extends Board implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6143646772231515350L;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String,Integer> answer = new ConcurrentHashMap<String,Integer>();

	private boolean isFPEnd;
	private int number; //扣的打法发到第几轮了
	//控制循环吃碰胡
	private Map<String,MJCardMessage> huController = new HashMap<String,MJCardMessage>();

	//控制 能糊不糊 这一轮都不能糊
	private Map<String,Boolean> cycleController = new HashMap<String,Boolean>();

	private boolean handlerDoIt;

	/**
	 * 翻底牌 ， 斗地主
	 */
	@Override
	public byte[] pollLastHands() {
		return ArrayUtils.subarray(this.getCards(), this.getCards().length - 3, this.getCards().length);
	}

	/**
	 * 暂时不做处理，根据业务规则修改，例如：底牌有大王翻两倍，底牌有小王 翻一倍，底牌是顺子 翻两倍 ====
	 */
	@Override
	public int calcRatio() {
		return 1;
	}

	@Override
	public TakeCards takeCards(Player player, String playerType, TakeCards current) {
		return new TakeMaJiangCards(player);
	}


	/**
	 * 找到玩家
	 *
	 * @param userid
	 * @return
	 */
	public Player player(String userid) {
		Player target = null;
		for (Player temp : this.getPlayers()) {
			if (temp.getPlayuser().equals(userid)) {
				target = temp;
				break;
			}
		}
		return target;
	}

	/**
	 * 找到玩家的 位置
	 *
	 * @param userid
	 * @return
	 */
	public int index(String userid) {
		int index = 0;
		for (int i = 0; i < this.getPlayers().length; i++) {
			Player temp = this.getPlayers()[i];
			if (temp.getPlayuser().equals(userid)) {
				index = i;
				break;
			}
		}
		return index;
	}


	/**
	 * 找到下一个玩家
	 *
	 * @param index
	 * @return
	 */
	public Player next(int index) {
		Player catchPlayer = null;
		if (index == 0 && this.getPlayers()[index].isRandomcard()) {    //fixed
			index = this.getPlayers().length - 1;
		}
		for (int i = index; i >= 0; i--) {
			Player player = this.getPlayers()[i];
			if (player.isDocatch() == false) {
				catchPlayer = player;
				break;
			} else if (player.isRandomcard()) {    //重新遍历一遍，发现找到了地主牌的人，终止查找
				break;
			} else if (i == 0) {
				i = this.getPlayers().length;
			}
			logger.info("next play Id:{}", index);
		}
		return catchPlayer;
	}


	public Player nextPlayer(int index) {
		if (index == (this.getPlayers().length - 1)) {
			index = 0;
		} else {
			index = index + 1;
		}
		return this.getPlayers()[index];
	}

	/**
	 * @param player
	 * @return
	 */
	public TakeCards takecard(Player player, boolean allow, byte[] playCards) {
		return new TakeMaJiangCards(player, allow, playCards);
	}

	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 *
	 * @param player
	 * @return
	 */
	public TakeCards takecard(Player player) {
		return new TakeMaJiangCards(player);
	}

	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 *
	 * @param player
	 * @return
	 */
	public TakeCards takecard(Player player, TakeCards last) {
		return new TakeMaJiangCards(player, last);
	}

	@Override
	public boolean isWin() {
		boolean win = false;
		if (this.getLast() != null && this.getLast().getCardsnum() == 0) {//出完了
			win = true;
		}
		return win;
	}

	@Override
	public TakeCards takeCardsRequest(GameRoom gameRoom, Board board, Player player, String orgi, boolean autoa, byte[] playCards) {
		/**
		 * 第一步就是先移除 计时器 ， 玩家通过点击页面上 牌面出牌的 需要移除计时器，并根据 状态 进行下一个节点
		 */
		CacheHelper.getExpireCache().remove(gameRoom.getRoomid());

		TakeCards takeCards = null;
		if (board.getDeskcards().size() <= 14) {//出完了
			logger.info("出完糊的方式");
			GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.ALLCARDS.toString(), 0);    //通知结算
		} else {
			takeCards = board.takecard(player, true, playCards);

			if (takeCards != null) {        //通知出牌
				logger.info("construct take cards finish");
				takeCards.setCardsnum(player.getCardsArray().length);

				board.setLast(takeCards);

				board.getNextplayer().setTakecard(true);

				CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());    //更新缓存数据

				if (takeCards.getCards().length == 1) {
					takeCards.setCard(takeCards.getCards()[0]);
				}
				ActionTaskUtils.sendEvent("takecards", takeCards, gameRoom);

				player.setHistory(ArrayUtils.add(player.getHistoryArray(), takeCards.getCard()));

				/**
				 * 判断是否胡牌 / 杠牌 / 碰 / 吃 ， 如果有，则发送响应的通知给其他玩家，如果没，下一个玩家 抓牌
				 *
				 * //todo 每打一张牌，需要判断下一家是否有吃碰糊的情况
				 *
				 */
				boolean hasAction = false;
				Player[] playersTemp = new Player[board.getPlayers().length-1];
				int n = 0;
				for (int i = 0;i<board.getPlayers().length ; i++){
					if(player.getPlayuser().equals(board.getPlayers()[i].getPlayuser())){
						for(int j = (i==3 ? 0 : i + 1); j != i ; j = (j == 3 ? 0 : j + 1) ){
							playersTemp[n] = board.getPlayers()[j];
							n++;
						}
					}
				}

				for (Player temp : playersTemp) {
					/**
					 * 玩法要求， 如果当前玩家有定缺，则当前出牌在和 缺门 的花色相同的情况下，禁止 杠碰吃胡
					 */
					//// TODO: 2018/3/26 ZCL 这个在涞源麻将中规则不成立
				/*	if (temp.getColor() == takeCards.getCard() / 36) {
						continue;
					}*/
					GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), orgi);

					/**
					 * 检查是否有 杠碰吃胡的 状况 如果这轮有没有糊 则不允许糊
					 */


					if (!temp.getPlayuser().equals(player.getPlayuser())) {

						logger.info("参与校验的牌为 card:{}", takeCards.getCard());
						MJCardMessage mjCard = checkMJCard(temp, takeCards.getCard(), false, gamePlayway.getCode());
						logger.info("whether having gang chi hu mjCard:{}", mjCard);
						if (mjCard.isGang() || mjCard.isPeng() || mjCard.isChi() || mjCard.isHu()) {
							/**
							 * 通知客户端 有杠碰吃胡了
							 */
							logger.info("通知客户端吃碰胡 peng:{}", mjCard.isPeng());

							try {
								if(((MaJiangBoard)board).getCycleController().containsKey(temp.getPlayuser()) &&
										((MaJiangBoard)board).getCycleController().get(temp.getPlayuser()) && mjCard.isHu()){
									logger.info("userId:{},mjCard:{},cycleController 存在未处理cycleController:{}",temp.getPlayuser(),mjCard,cycleController);

									//此处不应该break,应该continue 校验其他的用户
									//break;
									continue;

								}

								hasAction = true;
								logger.info("userId:{} 继续",temp.getPlayuser());
								synchronized (huController) {
									huController.put(temp.getPlayuser(),mjCard);
									logger.info("huController 添加 userId:{},data:{}，huController:{}",temp.getPlayuser(),mjCard,huController);
									if (huController.size() >= 2) {
										huController.wait();
										if (handlerDoIt) {
											break;
										}
										handlerDoIt = false;
										ActionTaskUtils.sendEvent(temp.getPlayuser(), mjCard);
									} else {
										handlerDoIt = false;
										ActionTaskUtils.sendEvent(temp.getPlayuser(), mjCard);
									}
								}
								if (handlerDoIt) {
									huController.clear();
									break;
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							//} else {
							//ActionTaskUtils.sendEvent(temp.getPlayuser(), mjCard);
							//}
						} else {
							player.setRecoveryHistory(ArrayUtils.add(player.getHistoryArray(), takeCards.getCard()));
							mjCard.setCommand("ting");
							//提示胡牌暂时去掉
							//mjCard.setRecommendCards(GameUtils.recommandCards(temp, temp.getCardsArray(),gamePlayway.getCode()));
							ActionTaskUtils.sendEvent(temp.getPlayuser(), mjCard);
						}
					}
				}


				/**
				 * 无杠碰吃
				 */
				if (hasAction == false) {
					board.dealRequest(gameRoom, board, orgi, false, null);
				} else {
					// 如果有吃碰杠，需要用户主动去打牌,不需要叫状态机处理
					//GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.DEAL.toString(), 5);    //有杠碰吃，等待5秒后发牌
				}
			} else {
				takeCards = new TakeMaJiangCards();
				takeCards.setAllow(false);
			}
		}
		return takeCards;
	}

	/**
	 * 检查玩家是否有杠碰吃胡动作
	 *
	 * @param player 玩家
	 * @param card   牌
	 * @param deal   是否抓牌
	 * @return
	 */
	public MJCardMessage checkMJCard(Player player, byte card, boolean deal,String code) {
		//MJCardMessage mjCard = GameUtils.processMJCard(player, player.getCardsArray(), card, deal);
		//暂时使用带混糊发
		MJCardMessage mjCard = GameUtils.processLaiyuanMJCard(player, player.getCardsArray(),card,deal,null,code);
		mjCard.setDeal(deal);
		mjCard.setTakeuser(player.getPlayuser());
		return mjCard;
	}

	/**
	 * 如果没有吃碰杠 则取新牌 进行下一步操作
	 */
	public void dealRequest(GameRoom gameRoom, Board board, String orgi, boolean reverse, String nextplayer) {
		Player next = board.nextPlayer(board.index(board.getNextplayer().getNextplayer()));
		//过胡情况
		next.setWin(false);
		next.setCollections(null);
		if (!StringUtils.isBlank(nextplayer)) {
			next = board.player(nextplayer);
		}
		if (next != null) {
			board.setNextplayer(new NextPlayer(next.getPlayuser(), false));
			Byte newCard = null;
			if (reverse == true) {    //杠牌 ， 从最后一张开始  //// TODO: 2018/3/23 ZCL 取牌逻辑
				newCard = board.getDeskcards().remove(board.getDeskcards().size() - 1);
			} else {
				newCard = board.getDeskcards().remove(0);
			}
			if(((MaJiangBoard)board).getCycleController().containsKey(next.getPlayuser()) && ((MaJiangBoard)board).getCycleController().get(next.getPlayuser())){
				logger.info("userId:{} 存在糊控制",next.getPlayuser());
				((MaJiangBoard)board).getCycleController().remove(next.getPlayuser());
			}
			logger.info("new card:{}", newCard);
			GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), orgi) ;
			MJCardMessage mjCard = checkMJCard(next, newCard, true,gamePlayway.getCode());
			boolean hasAction = false;
			if (mjCard.isGang() || mjCard.isPeng() || mjCard.isChi() || mjCard.isHu()) {
				/**
				 * 通知客户端 有杠碰吃胡了
				 */

			logger.info("userd:{} 自己取牌吃碰胡 mjCard:{}",next.getPlayuser(),mjCard);
				hasAction = true;
				ActionTaskUtils.sendEvent(next.getPlayuser(), mjCard);
			}

			next.setCards(ArrayUtils.add(next.getCardsArray(), newCard));

			/**
			 * 抓牌 , 下一个玩家收到的牌里会包含 牌面，其他玩家的则不包含牌面  //todo ZCL主要是更新牌面
			 */
			for (Player temp : board.getPlayers()) {
				if (temp.getPlayuser().equals(next.getPlayuser())) {
					ActionTaskUtils.sendEvent("dealcard", temp.getPlayuser(), new DealCard(next.getPlayuser(), board.getDeskcards().size(), temp.getColor(), newCard, hasAction));
				} else {
					ActionTaskUtils.sendEvent("dealcard", temp.getPlayuser(), new DealCard(next.getPlayuser(), board.getDeskcards().size()));
				}
			}
		}


		CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());

		/**
		 * 下一个出牌的玩家
		 */
		this.playcards(board, gameRoom, next, orgi);
	}

	/**
	 * 下一个玩家 出牌
	 */
	public void playcards(Board board, GameRoom gameRoom, Player player, String orgi) {
		/**
		 * 牌出完了就算赢了
		 */
		logger.info("roomId:{},userId:{}",gameRoom.getId(), player.getPlayuser());
		PlayUserClient nextPlayUserClient = ActionTaskUtils.getPlayUserClient(gameRoom.getId(), player.getPlayuser(), orgi);
		logger.info("下一个玩家出牌， 玩家的信息为 player:{}", nextPlayUserClient == null ? null : nextPlayUserClient.getUsername());
		if (BMDataContext.PlayerTypeEnum.NORMAL.toString().equals(nextPlayUserClient.getPlayertype()) && !player.isHu()) {
			GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.PLAYCARDS.toString(), 8000);    //应该从 游戏后台配置参数中获取 , 当前玩家未胡牌或听牌（听牌以后也不允许换牌）
		} else {
			GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.PLAYCARDS.toString(), 1);    //应该从游戏后台配置参数中获取
		}
	}

	/**
	 * 棋牌结束 开始结算
	 *
	 * @param board
	 * @param gameRoom
	 * @param playway
	 * @return
	 */
	@Override
	public Summary summary(Board board, GameRoom gameRoom, GamePlayway playway) {
		Summary summary = new Summary(gameRoom.getId(), board.getId(), board.getRatio(), board.getRatio() * playway.getScore());
		List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
		boolean gameRoomOver = false;    //解散房间
		if("1".equals(playway.getCode())){
			logger.info("走涞源混");
			laiYuanHunSummary(gameRoom,board,players,summary,playway,gameRoomOver);
		}else if("2".equals(playway.getCode())){
			logger.info("走扣大将");
			laiYuanKouSummary(board,players,summary,playway);
		}else{
			logger.info("走默认路径判断糊");
			laiYuanHunSummary(gameRoom,board,players,summary,playway,gameRoomOver);
			//defaultSummary(board,players,summary,playway,gameRoomOver);
		}

		return summary;
	}


	@Transactional
	public void houseCardHandler(GameRoom gameRoom, GamePlayway playway,Board board,List<PlayUserClient> players,List<ReturnResult> returnResults){

		try {
			if (gameRoom == null || playway == null) {
				return;
			}
			HouseCardHandlerService cardHandlerService = BMDataContext.getContext().getBean("houseCardHandlerService", HouseCardHandlerService.class);
			cardHandlerService.cardHandler(gameRoom,players,returnResults);
			cardHandlerService.saveUserFlow(gameRoom, board, players,returnResults);
		}catch (Exception e){
			logger.error("保存历史数据异常 returnResults:{}",returnResults,e);
		}
	}




	private byte cardConvert(byte[]src,byte[] b){
		for(int i = 0 ;i<src.length-1 ; i++){
			b[i] = src[i];
		}
		return src[src.length - 1];
	}

	private void laiYuanHunSummary(GameRoom gameRoom,Board board, List<PlayUserClient> players,Summary summary,GamePlayway playway,boolean gameRoomOver) {

		List<ReturnResult> returnResults = null;
		for (Player player : board.getPlayers()) {
			PlayUserClient playUser = getPlayerClient(players, player.getPlayuser());
			SummaryPlayer summaryPlayer = new SummaryPlayer(player.getPlayuser(), playUser.getUsername() + "", board.getRatio(), board.getRatio() * playway.getScore(), false, player.getPlayuser().equals(board.getBanker()));

			logger.info("汇总结果");
			logger.info("player:{} 牌数 size:{}", player.getPlayuser(), player.getCardsArray().length);
			if (!player.isWin()) {
				logger.info("roomId:{} 整理棋牌为没赢", board.getRoom());
				List<GameResultSummary> gameResultChecks = GameWinCheck.playerSummary(player, null);
				if (CollectionUtils.isEmpty(gameResultChecks)) {
					GameResultSummary gameResultSummary = new GameResultSummary();
					gameResultChecks.add(gameResultSummary);
				}
				if (player.getCardsArray() != null) {
					for (int i = 0; i < player.getCardsArray().length - 1; i++) {
						for (int j = i + 1; j < player.getCardsArray().length; j++) {
							if (player.getCardsArray()[i] > player.getCardsArray()[j]) {
								byte temp = player.getCardsArray()[i];
								player.getCardsArray()[i] = player.getCardsArray()[j];
								player.getCardsArray()[j] = temp;
							}
						}
					}
				}
				gameResultChecks.get(0).setOthers(player.getCardsArray()); //未出完的牌
				summaryPlayer.setGameResultChecks(gameResultChecks);
			} else {
				gameRoom.setLastwinner(player.getPlayuser());
				logger.info("userId:{},roomId:{} 整理棋牌为赢", board.getRoom());
				byte[] cardsTemp = new byte[player.getCardsArray().length - 1];
				System.arraycopy(player.getCardsArray(), 0, cardsTemp, 0, cardsTemp.length);
				GameUtils.processLaiyuanMJCardResult(player, cardsTemp, player.getCardsArray()[cardsTemp.length], false, player.getCollections(), playway.getCode());
				for (List<Byte> cards : player.getCollections()) {
					System.out.println("赢家的牌信息start");
					for (Byte b : cards) {
						System.out.print(b + ",");
					}
					System.out.println("赢家的牌信息end");
				}
				returnResults = scoreSummary(board.getPlayers(), summary, gameRoom, board.getBanker());
				ReturnResult returnResult = null;
				for (ReturnResult rr : returnResults) {
					if (rr.isWin()) {
						returnResult = rr;
					}
				}
				List<GameResultSummary> gameResultChecks = GameWinCheck.playerSummary(player, returnResult.getCollections());
				summaryPlayer.setGameResultChecks(gameResultChecks);

				summaryPlayer.setScore(returnResult.getScore());
				summaryPlayer.setDesc(returnResult.getDesc());
			}
			summaryPlayer.setWin(player.isWin());
			summary.getPlayers().add(summaryPlayer);
			if (((MaJiangBoard) board).getAnswer() != null) {
				((MaJiangBoard) board).getAnswer().clear();
			}
		}

		for (SummaryPlayer splayer : summary.getPlayers()) {
			for(ReturnResult returnResult : returnResults) {
				if (splayer.getUserid().equals(returnResult.getUserId())){
					splayer.setScore(returnResult.getScore());
					splayer.setDesc(returnResult.getDesc());
				}
			}
		}

		houseCardHandler(gameRoom, playway, board, players,returnResults);
		summary.setGameRoomOver(gameRoomOver);    //有玩家破产，房间解散
		logger.info("发送总结信息为 summary:{}", JSONObject.toJSONString(summary));
		/**
		 * 上面的 Player的 金币变更需要保持 数据库的日志记录 , 机器人的 金币扣完了就出局了
		 */
	}

	private List<ReturnResult> scoreSummary(Player[] players,Summary summarys,GameRoom gameRoom,String bank) {
		logger.info("tid:{}开始算分操作");

		if (players == null || players.length == 0 ) {
			logger.info("tid:{} 信息不全1");
			return null;
		}
		List<ReturnResult> returnResults = HuValidate.validateHu(players,gameRoom,bank);
		if (CollectionUtils.isEmpty(returnResults)) {
			logger.info("tid:{} 信息不全2");
			return null;
		}

		for (SummaryPlayer summary : summarys.getPlayers()) {
			for (ReturnResult returnResult : returnResults) {
				if (summary.getUserid().equals(returnResult.getUserId())) {
					summary.setDesc(returnResult.getDesc());
				}
			}
		}
		logger.info("tid:{} 算分完成");
		return returnResults;
	}




	private void laiYuanKouSummary(Board board,List<PlayUserClient> players,Summary summary,GamePlayway playway){

	}


	private void defaultSummary(Board board, List<PlayUserClient> players,Summary summary,GamePlayway playway,boolean gameRoomOver) {

		for (Player player : board.getPlayers()) {
			PlayUserClient playUser = getPlayerClient(players, player.getPlayuser());
			SummaryPlayer summaryPlayer = new SummaryPlayer(player.getPlayuser(), playUser.getUsername()+"", board.getRatio(), board.getRatio() * playway.getScore(), false, player.getPlayuser().equals(board.getBanker()));
			/**
			 * 遍历Action ， Action类型 ：1、杠（明/暗）、碰、吃、胡（自摸/瞎胡），被自摸，点炮、点杠、被杠
			 */

			/**
			 * 如果庄被查花猪，则赔给三家，其他三家查花猪，赔给庄 ， 其他玩法规则调用 playwy的计算方法
			 */
			if (player.getPlayuser().equals(board.getBanker())) {

			}
			/**
			 * 查花猪
			 */
			logger.info("汇总结果");
			summaryPlayer.setCards(player.getCardsArray()); //未出完的牌
			summary.getPlayers().add(summaryPlayer);
		}
		summary.setGameRoomOver(gameRoomOver);    //有玩家破产，房间解散
		/**
		 * 上面的 Player的 金币变更需要保持 数据库的日志记录 , 机器人的 金币扣完了就出局了
		 */
	}

	public Map<String, Integer> getAnswer() {
		return answer;
	}

	public void setAnswer(Map<String, Integer> answer) {
		this.answer = answer;
	}

	public boolean isFPEnd() {
		return isFPEnd;
	}

	public void setFPEnd(boolean FPEnd) {
		isFPEnd = FPEnd;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isHandlerDoIt() {
		return handlerDoIt;
	}

	public void setHandlerDoIt(boolean handlerDoIt) {
		this.handlerDoIt = handlerDoIt;
	}

	public Map<String, MJCardMessage> getHuController() {
		return huController;
	}

	public void setHuController(Map<String, MJCardMessage> huController) {
		this.huController = huController;
	}

	public Map<String, Boolean> getCycleController() {
		return cycleController;
	}

	public void setCycleController(Map<String, Boolean> cycleController) {
		this.cycleController = cycleController;
	}


	public static void main(String[] args) {
		Player tempPlayer = new Player("2");
		Player[] players = new Player[4];
		players[0] = new Player("0");
		players[1] = new Player("1");
		players[2] = new Player("2");
		players[3] = new Player("3");
		Player[] playersTemp = new Player[3];
		int n = 0;
		for (int i = 0;i<4 ; i++){
			if(tempPlayer.getPlayuser().equals(players[i].getPlayuser())){
				for(int j = (i==3 ? 0 : i + 1); j != i ; j = (j == 3 ? 0 : j + 1) ){
					playersTemp[n] = players[j];
					n++;
				}
			}
		}
		System.out.println(playersTemp);
	}

}
