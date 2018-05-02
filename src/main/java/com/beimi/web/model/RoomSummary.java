package com.beimi.web.model;

import javax.persistence.Entity;


/**
 * Created by zhengchenglei on 2018/4/19.
 */
public class RoomSummary {

    private Integer roomId;

    private Integer num;

    private String date;

    private String nickname;

    private Integer userNo;

    private Integer score;

    private String photo;

    private boolean isCurrentUser;

    private Integer useCards;


    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Integer getUseCards() {
        return useCards;
    }

    public void setUseCards(Integer useCards) {
        this.useCards = useCards;
    }

    public Integer getUserNo() {
        return userNo;
    }

    public void setUserNo(Integer userNo) {
        this.userNo = userNo;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }
}
