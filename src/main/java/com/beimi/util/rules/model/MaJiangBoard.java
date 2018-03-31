package com.beimi.util.rules.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.beimi.model.GameResultSummary;
import com.beimi.util.GameWinCheck;
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
	public TakeCards takeCardsRequest(GameRoom gameRoom, Board board, Player player, String orgi, boolean auto, byte[] playCards) {
		/**
		 * 第一步就是先移除 计时器 ， 玩家通过点击页面上 牌面出牌的 需要移除计时器，并根据 状态 进行下一个节点
		 */
		CacheHelper.getExpireCache().remove(gameRoom.getRoomid());

		TakeCards takeCards = null;
		if (board.getDeskcards().size() == 0) {//出完了
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

				player.setHistory(ArrayUtils.add(player.getHistory(), takeCards.getCard()));

				/**
				 * 判断是否胡牌 / 杠牌 / 碰 / 吃 ， 如果有，则发送响应的通知给其他玩家，如果没，下一个玩家 抓牌
				 *
				 * //todo 每打一张牌，需要判断下一家是否有吃碰糊的情况
				 *
				 */
				boolean hasAction = false;
				for (Player temp : board.getPlayers()) {
					/**
					 * 玩法要求， 如果当前玩家有定缺，则当前出牌在和 缺门 的花色相同的情况下，禁止 杠碰吃胡
					 */
				//// TODO: 2018/3/26 ZCL 这个在涞源麻将中规则不成立
				/*	if (temp.getColor() == takeCards.getCard() / 36) {
						continue;
					}*/

					/**
					 * 检查是否有 杠碰吃胡的 状况
					 */
					if (!temp.getPlayuser().equals(player.getPlayuser())) {

						logger.info("参与校验的牌为 card:{}",takeCards.getCard());
						MJCardMessage mjCard = checkMJCard(temp, takeCards.getCard(),false);
						logger.info("whether having gang chi hu mjCard:{}",mjCard);
						if (mjCard.isGang() || mjCard.isPeng() || mjCard.isChi() || mjCard.isHu()) {
							/**
							 * 通知客户端 有杠碰吃胡了
							 */
							logger.info("通知客户端吃碰胡 peng:{}",mjCard.isPeng());
							hasAction = true;
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
					GameUtils.getGame(gameRoom.getPlayway(), orgi).change(gameRoom, BeiMiGameEvent.DEAL.toString(), 5);    //有杠碰吃，等待5秒后发牌
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
	public MJCardMessage checkMJCard(Player player, byte card, boolean deal) {
		//MJCardMessage mjCard = GameUtils.processMJCard(player, player.getCardsArray(), card, deal);
		//暂时使用带混糊发
		MJCardMessage mjCard = GameUtils.processLaiyuanMJCard(player, player.getCardsArray(),card,deal,null);
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
			logger.info("new card:{}", newCard);
			MJCardMessage mjCard = checkMJCard(next, newCard, true);
			boolean hasAction = false;
			if (mjCard.isGang() || mjCard.isPeng() || mjCard.isChi() || mjCard.isHu()) {
				/**
				 * 通知客户端 有杠碰吃胡了
				 */
				hasAction = true;
				ActionTaskUtils.sendEvent(next.getPlayuser(), mjCard);
			}else{
				mjCard.setRecommendCards(GameUtils.recommandCards(next, next.getCardsArray()));
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
			laiYuanHunSummary(board,players,summary,playway,gameRoomOver);
		}else if("2".equals(playway.getCode())){
			logger.info("走扣大将");
			laiYuanKouSummary(board,players,summary,playway);
		}else{
			logger.info("走默认路径判断糊");
			laiYuanHunSummary(board,players,summary,playway,gameRoomOver);
			//defaultSummary(board,players,summary,playway,gameRoomOver);
		}

		return summary;
	}

	private byte cardConvert(byte[]src,byte[] b){
		for(int i = 0 ;i<src.length-1 ; i++){
			b[i] = src[i];
		}
		return src[src.length - 1];
	}

	private void laiYuanHunSummary(Board board, List<PlayUserClient> players,Summary summary,GamePlayway playway,boolean gameRoomOver){
		for (Player player : board.getPlayers()) {
			PlayUserClient playUser = getPlayerClient(players, player.getPlayuser());
			SummaryPlayer summaryPlayer = new SummaryPlayer(player.getPlayuser(), playUser.getUsername(), board.getRatio(), board.getRatio() * playway.getScore(), false, player.getPlayuser().equals(board.getBanker()));
			logger.info("汇总结果");
			logger.info("player:{} 牌数 size:{}", player.getPlayuser(), player.getCardsArray().length);

			//	MJCardMessage mjCard = GameUtils.processLaiyuanMJCard(player, b, card, false,null);
			if (!player.isWin()) {
				logger.info("roomId:{} 整理棋牌为没赢", board.getRoom());
				summaryPlayer.setCards(player.getCardsArray()); //未出完的牌
			} else {
				logger.info("roomId:{} 整理棋牌为赢", board.getRoom());
				List<GameResultSummary> gameResultChecks = GameWinCheck.playerSunmary(player, player.getCollections());
				summaryPlayer.setGameResultChecks(gameResultChecks);
			}

			summaryPlayer.setWin(player.isWin());
			summary.getPlayers().add(summaryPlayer);
			player.setWin(false);
			player.setCollections(null);
		}
		summary.setGameRoomOver(gameRoomOver);    //有玩家破产，房间解散
		logger.info("发送总结信息为 summary:{}", JSONObject.toJSONString(summary));
		/**
		 * 上面的 Player的 金币变更需要保持 数据库的日志记录 , 机器人的 金币扣完了就出局了
		 */

	}

	private void laiYuanKouSummary(Board board,List<PlayUserClient> players,Summary summary,GamePlayway playway){

	}


	private void defaultSummary(Board board, List<PlayUserClient> players,Summary summary,GamePlayway playway,boolean gameRoomOver) {

		for (Player player : board.getPlayers()) {
			PlayUserClient playUser = getPlayerClient(players, player.getPlayuser());
			SummaryPlayer summaryPlayer = new SummaryPlayer(player.getPlayuser(), playUser.getUsername(), board.getRatio(), board.getRatio() * playway.getScore(), false, player.getPlayuser().equals(board.getBanker()));
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

}
