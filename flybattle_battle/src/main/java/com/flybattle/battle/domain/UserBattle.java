package com.flybattle.battle.domain;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class UserBattle {
    private int uid;
    private String uname;
    private int roomId;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
