package com.beimi.core.engine.game.model;


import com.beimi.model.GameResultSummary;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class SummaryPlayer implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userid ;
	private String username ;
	private String nickName;
	private String photo;
	private int ratio ;
	private int score ;
	private boolean gameover ;//破产了
	private int balance ;	  //玩家账户余额
	private boolean win ;
	private byte[] cards ;
	private String desc = "";  // 赢牌描述
	private List<GameResultSummary> gameResultChecks;
	
	private boolean dizhu ;
	
	public SummaryPlayer(){}
	public SummaryPlayer(String userid , String username , int ratio , int score, boolean win , boolean dizhu){
		this.userid = userid ;
		this.username = username ;
		this.ratio = ratio ; 
		this.score = score ;
		this.win = win ;
		this.dizhu = dizhu ;
	}
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getRatio() {
		return ratio;
	}
	public void setRatio(int ratio) {
		this.ratio = ratio;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public boolean isWin() {
		return win;
	}
	public void setWin(boolean win) {
		this.win = win;
	}
	public String getCards() {
		if(this.cards == null || this.cards.length == 0){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(byte b : this.cards){
			sb.append(",").append(b);
		}
		return sb.substring(1);
	}
	public void setCards(byte[] cards) {
		this.cards = cards;
	}
	public void setCards(Byte[] cards) {
		if(cards == null || cards.length == 0){
			return;
		}
		this.cards = new byte[cards.length];
		System.arraycopy(cards,0,this.cards,0,cards.length);
	}
	public boolean isGameover() {
		return gameover;
	}
	public void setGameover(boolean gameover) {
		this.gameover = gameover;
	}
	public boolean isDizhu() {
		return dizhu;
	}
	public void setDizhu(boolean dizhu) {
		this.dizhu = dizhu;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}

	public List<GameResultSummary> getGameResultChecks() {
		return gameResultChecks;
	}

	public void setGameResultChecks(List<GameResultSummary> gameResultChecks) {
		this.gameResultChecks = gameResultChecks;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}
