package com.flybattle.battle.domain;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class OpCode {
    //加入房间请求
    public static final int JOIN_ROOM_REQ = 301;
    //加入房间响应
    public static final int JOIN_ROOM_RESP = 302;
    //发送同步战斗
    public static final int SYNC_BATTLE_REQ = 303;
    //返回同步战斗
    public static final int SYNC_BATTLE_RESP = 304;
    //离开房间请求
    public static final int LEAVE_ROOM_REQ = 305;
    //离开房间响应
    public static final int LEAVE_ROOM_RESP = 306;
    //玩家加入房间通知
    public static final int USER_JOIN_ROOM = 307;
    //玩家离开房间通知
    public static final int USER_LEAVE_ROOM = 308;
    //玩家攻击同步请求
    public static final int SYNC_ATTACK_REQ = 309;
    //玩家攻击同步返回
    public static final int SYNC_ATTACK_RESP = 310;
    //玩家伤害同步请求
    public static final int SYNC_DEMAGE_REQ = 311;

}
