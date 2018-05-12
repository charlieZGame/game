package com.beimi.util.rules.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beimi.core.engine.game.Message;

public class Player implements Message,java.io.Serializable , Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public Player(String id){
		this.playuser = id ;
		
	}
	private String playuser ;	//userid对应
	private byte[] cards ;	//玩家手牌，顺序存储 ， 快速排序（4个Bit描述一张牌，玩家手牌 麻将 13+1/2 = 7 byte~=long）
	private byte[] history = new byte[]{};//出牌历史 ， 特权可看
	private byte[] recoveryHistory; //历史左面牌
	private byte info ;		//复合信息存储，用于存储玩家位置（2^4,占用4个Bit，最大支持16个玩家）（是否在线1个Bit），是否庄家/地主（1个Bit），是否当前出牌玩家（1个Bit）（是否机器人1个Bit）
	private boolean randomcard ;	//起到地主牌的人
	private boolean docatch ;	//抢过庄（地主）
	private boolean recatch ;	//补抢
	private int deskcards ;	//剩下多少张牌
	
	private boolean hu ;	//已经胡过牌了
	private boolean end ;	//血战的时候，标记 结束
	
	private String command ;
	
	private boolean selected = true ;	//已经选择 花色
	private int color ;		//定缺 花色   0  : wan , 1:tong , 2 :tiao
	
	private boolean accept ;	//抢地主 : 过地主
	private boolean banker ;	//庄家
	private byte[] played ;	//杠碰吃胡

	private byte[] powerfull; //混子

	private List<Byte> coverCards; //扣住的牌

	private Integer coverySize;

	private boolean isWin;

	private boolean isZm;

	private String targetUser; //点炮用户

	private Integer piao = 0; // 当前用户飘几
	private List<List<Byte>>collections;

	private String nickname;

	private List<Action> actions = new ArrayList<Action>();

	public byte[] getCardsArray() {
		return cards;
	}

	public String getCards(){
		if(this.cards == null || this.cards.length == 0){
			return "";
		}
		Arrays.sort(this.cards);
		StringBuilder sb = new StringBuilder();
		for(byte _b : this.cards){
			sb.append(",").append(_b);
		}
		return sb.substring(1);
	}

	public void setCards(byte[] cards) {
		this.cards = cards;
	}

	public byte getInfo() {
		return info;
	}

	public void setInfo(byte info) {
		this.info = info;
	}

	public byte[] getPlayed() {
		return played;
	}

	public void setPlayed(byte[] played) {
		this.played = played;
	}

	public byte[] getHistoryArray() {
		return history;
	}
	public String getHistory() {
		if(this.history == null || this.history.length == 0){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(byte b : this.history){
			sb.append(",").append(b);
		}
		return sb.substring(1);
	}

	public void setHistory(byte[] history) {
		this.history = history;
	}

	public String getPlayuser() {
		return playuser;
	}

	public void setPlayuser(String playuser) {
		this.playuser = playuser;
	}

	public boolean isRandomcard() {
		return randomcard;
	}

	public void setRandomcard(boolean randomcard) {
		this.randomcard = randomcard;
	}

	public boolean isDocatch() {
		return docatch;
	}

	public void setDocatch(boolean docatch) {
		this.docatch = docatch;
	}
	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	@Override
    public Player clone(){
        try {
			return (Player) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
    }

	public boolean isRecatch() {
		return recatch;
	}

	public void setRecatch(boolean recatch) {
		this.recatch = recatch;
	}

	public boolean isBanker() {
		return banker;
	}

	public void setBanker(boolean banker) {
		this.banker = banker;
	}

	public int getDeskcards() {
		return deskcards;
	}

	public void setDeskcards(int deskcards) {
		this.deskcards = deskcards;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean isHu() {
		return hu;
	}

	public void setHu(boolean hu) {
		this.hu = hu;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public String getPowerfull() {
		if(this.powerfull == null || this.powerfull.length == 0 ){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i< this.powerfull.length;i++){
			sb.append(",").append(this.powerfull[i]);
		}
		return sb.substring(1);
	}

	public byte[] getPowerfullArray(){
		return this.powerfull;
	}

	public void setPowerfull(byte[] powerfull) {
		this.powerfull = powerfull;
	}

	public List<List<Byte>> getCollections() {
		if(this.collections == null){
			this.collections = new ArrayList<List<Byte>>();
		}
		return this.collections;
	}

	public void setCollections(List<List<Byte>> collections) {
		this.collections = collections;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean win) {
		isWin = win;
	}

	public List<Byte> getCoverCards() {
		return coverCards;
	}

	public void setCoverCards(List<Byte> coverCards) {
		this.coverCards = coverCards;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void clear(){
		coverCards = new ArrayList<Byte>();
		collections = new ArrayList<List<Byte>>();
	}

	public Integer getCoverySize() {
		return coverySize;
	}

	public void setCoverySize(Integer coverySize) {
		this.coverySize = coverySize;
	}


	public String getRecoveryHistory() {
		if (this.recoveryHistory == null || this.recoveryHistory.length == 0) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.recoveryHistory.length; i++) {
			sb.append(",").append(this.recoveryHistory[i]);
		}
		return sb.substring(1);
	}


	public byte[] getRecoveryHistoryArray() {
		return recoveryHistory;
	}

	public void setRecoveryHistory(byte[] recoveryHistory) {
		this.recoveryHistory = recoveryHistory;
	}

	public boolean isZm() {
		return isZm;
	}

	public void setZm(boolean zm) {
		isZm = zm;
	}

	public String getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(String targetUser) {
		this.targetUser = targetUser;
	}

	public Integer getPiao() {
		return piao;
	}

	public void setPiao(Integer piao) {
		this.piao = piao;
	}
}
