package com.beimi.web.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhengchenglei on 2018/4/6.
 */

@Entity
@Table(name = "play_history")
@org.hibernate.annotations.Proxy(lazy = false)
public class PlayHistory implements Serializable {

    private String id ;
    private String userId;
    private Integer roomId;
    private String roomUuid;
    private String nickname;
    private Integer username;
    private String photo;
    private String friendIds;
    private String cardFirends;
    private Integer score;
    private Integer cardNum;
    private String isWin;
    private String createPin;
    private Date createTime;
    private String updatePin;
    private Date updateTime;
    private String yxbj;
    @Transient
    private Object obj;


    @Id
    @Column(length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdatePin() {
        return updatePin;
    }

    public void setUpdatePin(String updatePin) {
        this.updatePin = updatePin;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getYxbj() {
        return yxbj;
    }

    public void setYxbj(String yxbj) {
        this.yxbj = yxbj;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(String friendIds) {
        this.friendIds = friendIds;
    }

    public String getIsWin() {
        return isWin;
    }

    public void setIsWin(String isWin) {
        this.isWin = isWin;
    }

    public String getCreatePin() {
        return createPin;
    }

    public void setCreatePin(String createPin) {
        this.createPin = createPin;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getCardNum() {
        return cardNum;
    }

    public void setCardNum(Integer cardNum) {
        this.cardNum = cardNum;
    }

    public Integer getUsername() {
        return username;
    }

    public void setUsername(Integer username) {
        this.username = username;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCardFirends() {
        return cardFirends;
    }

    public void setCardFirends(String cardFirends) {
        this.cardFirends = cardFirends;
    }

    @Transient
    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getRoomUuid() {
        return roomUuid;
    }

    public void setRoomUuid(String roomUuid) {
        this.roomUuid = roomUuid;
    }
}
