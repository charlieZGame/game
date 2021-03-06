package com.beimi.util.rules.model;

import com.beimi.core.engine.game.Message;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;

public class JoinRoom implements Message{
	private String command ;
	private PlayUserClient player ;
	private int index ;
	private int maxplayers ;
	private boolean cardroom ;
	private String roomid ;
	private String roomUuid;
	
	public JoinRoom(PlayUserClient player , int index , int maxplayer , GameRoom gameRoom){
		this.player = player;
		this.index = index;
		this.maxplayers = maxplayer ;
		this.cardroom = gameRoom.isCardroom() ;
		this.roomid = gameRoom.getRoomid() ;
		this.roomUuid = gameRoom.getId();
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public PlayUserClient getPlayer() {
		return player;
	}
	public void setPlayer(PlayUserClient player) {
		this.player = player;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getMaxplayers() {
		return maxplayers;
	}
	public void setMaxplayers(int maxplayers) {
		this.maxplayers = maxplayers;
	}

	public boolean isCardroom() {
		return cardroom;
	}

	public void setCardroom(boolean cardroom) {
		this.cardroom = cardroom;
	}

	public String getRoomid() {
		return roomid;
	}

	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

	public String getRoomUuid() {
		return roomUuid;
	}

	public void setRoomUuid(String roomUuid) {
		this.roomUuid = roomUuid;
	}
}
