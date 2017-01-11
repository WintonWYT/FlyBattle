package com.server.extensions.user;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.baitian.mobileserver.servercomponent.IUser;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class User implements IUser {
    @Protobuf
    private long userId;
    @Protobuf
    private String userName;

    private String userPwd;

    private int roomId;
    private int uid;


    public User(long userId, String userName, String userPwd) {
        this.userId = userId;
        this.userName = userName;
        this.userPwd = userPwd;
        roomId = -1;
        uid = -1;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public boolean checkPwd(String pwd) {
        if (userPwd.equals(pwd)) {
            return true;
        }
        return false;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomIdAndUid(int roomId, int uid) {
        this.roomId = roomId;
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

}
