package com.beimi.util.rules.model;

public class SearchRoomResult implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8077510029073026136L;
	private String id ;		//玩法ID
	private String code ;	//游戏类型
	private String roomid ; //房间ID
	private String result ;	//
	private String jun;
	private String hun;
	private String hunfeng;
	private String hunpiao;
	private String playway;
	private String gamemodel = "room";
	
	
	public SearchRoomResult(){}
	
	public SearchRoomResult(String result){
		this.result = result ;
	}
	
	public SearchRoomResult(String id , String code ,String result){
		this.id = id;
		this.code = code;
		this.result = result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRoomid() {
		return roomid;
	}

	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getJun() {
		return jun;
	}

	public void setJun(String jun) {
		this.jun = jun;
	}

	public String getHun() {
		return hun;
	}

	public void setHun(String hun) {
		this.hun = hun;
	}

	public String getHunfeng() {
		return hunfeng;
	}

	public void setHunfeng(String hunfeng) {
		this.hunfeng = hunfeng;
	}

	public String getHunpiao() {
		return hunpiao;
	}

	public void setHunpiao(String hunpiao) {
		this.hunpiao = hunpiao;
	}

	public String getPlayway() {
		return playway;
	}

	public void setPlayway(String playway) {
		this.playway = playway;
	}

	public String getGamemodel() {
		return gamemodel;
	}

	public void setGamemodel(String gamemodel) {
		this.gamemodel = gamemodel;
	}
}
