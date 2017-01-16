package com.flybattle.battle.core;

import com.flybattle.battle.domain.BattleInfo;
import com.flybattle.battle.domain.OpCode;
import com.flybattle.battle.domain.UserBattle;
import com.flybattle.battle.server.ChannelManager;
import com.flybattle.battle.util.Command;
import com.flybattle.battle.util.CommandHandler;
import com.server.protobuf.DamageInfo;
import com.server.protobuf.PlayerInfo;
import com.server.protobuf.Vec3;
import com.server.protobuf.request.*;
import com.server.protobuf.response.*;

import java.util.List;

/**
 * Created by wuyingtan on 2017/1/6.
 */
public class BattleCenter {
    private static final BattleCenter INSTANCE = new BattleCenter();

    private BattleCenter() {
        CommandHandler.INSTANCE.commandSupport(this.getClass());
    }

    public static BattleCenter getInstance() {
        return INSTANCE;
    }

    @Command(OpCode.JOIN_ROOM_REQ)
    public EnterBattleResp handleJoinRoom(PlayerInfo info) {
        String uname = info.uname;
        info = BattleManager.INSTANCE.joinRoom(uname);
        UserBattle userBattle = new UserBattle();
        userBattle.setUid(info.uid);
        userBattle.setRoomId(info.roomId);
        userBattle.setUname(uname);
        UserBattleManager.INSTANCE.addUserBattle(userBattle);

        EnterBattleResp resp = new EnterBattleResp();
        resp.myInfo = info;
        resp.otherInfo = BattleManager.INSTANCE.getOtherPlayerInfoList(info.roomId, info.uid);
        resp.energyBlockInfo = BattleManager.INSTANCE.getAllEnergyBlock(info.roomId);
        List<Integer> uidList = BattleManager.INSTANCE.getOtherUidList(info.roomId, info.uid);
        sendUserJoinRoom(uidList, info);
        return resp;

    }

    @Command(OpCode.SYNC_ATTACK_REQ)
    public void handleSyncAttack(AttackReq req) {
        int uid = req.info.uid;
        UserBattle user = UserBattleManager.INSTANCE.getUserBattle(uid);
        List<Integer> uidList = BattleManager.INSTANCE.getOtherUidList(user.getRoomId(), user.getUid());
        AttackResp resp = new AttackResp();
        resp.info = req.info;
        sendSyncAttack(uidList, resp);
    }

    @Command(OpCode.SYNC_BATTLE_REQ)
    public void handleSyncBattle(SyncReq req) {
        int uid = req.myInfo.uid;
        UserBattle user = UserBattleManager.INSTANCE.getUserBattle(uid);
        Vec3 pos = req.myInfo.pos;
        Vec3 dir = req.myInfo.dir;
        BattleInfo info = new BattleInfo(pos, dir);
        BattleManager.INSTANCE.updatePosition(user.getRoomId(), uid, info);
    }

    @Command(OpCode.SYNC_DEMAGE_REQ)
    public void handleSyncDemage(DamageReq req) {
        int uid = req.uid;
        UserBattle user = UserBattleManager.INSTANCE.getUserBattle(uid);
        DamageInfo info = new DamageInfo();
        info.uid = uid;
        info.reduceHp = req.damage;
        BattleManager.INSTANCE.addDamageInfo(user.getRoomId(), info);
    }

    @Command(OpCode.LEAVE_ROOM_REQ)
    public void handleLeaveRoom(PlayerInfo info) {
        int roomId = info.roomId;
        int uid = info.uid;
        List<Integer> uidList = BattleManager.INSTANCE.getOtherUidList(roomId, uid);
        BattleManager.INSTANCE.leaveRoom(roomId, uid);
        UserBattleManager.INSTANCE.removeUserBattle(uid);
        senLeaveRoomResp(uid);
        sendUserLeaveRoom(uidList, uid);
    }

    @Command(OpCode.ACCE_REQ)
    public void handleAccelerate(AccelerateReq req) {
        int uid = req.uid;
        UserBattle user = UserBattleManager.INSTANCE.getUserBattle(uid);
        List<Integer> otherUidList = BattleManager.INSTANCE.getOtherUidList(user.getRoomId(), uid);
        AccelaerateResp resp = new AccelaerateResp();
        resp.speed = req.speed;
        resp.uid = uid;
        ChannelManager.INSTANCE.sendResponse(otherUidList, OpCode.ACCE_RESP, resp);
    }

    @Command(OpCode.CHANGE_BULLECT_REQ)
    public void handleBullectChange(ChangeBullectReq req) {
        int uid = req.uid;
        UserBattle user = UserBattleManager.INSTANCE.getUserBattle(uid);

        BattleManager.INSTANCE.setBullectType(user.getRoomId(), uid, req.bullectType);
        List<Integer> otherUidList = BattleManager.INSTANCE.getOtherUidList(user.getRoomId(), uid);

        ChangeBullectResp resp = new ChangeBullectResp();
        resp.bullectType = req.bullectType;
        resp.uid = uid;

        ChannelManager.INSTANCE.sendResponse(otherUidList, OpCode.CHANGE_BULLECT_RESP, resp);
    }

    @Command(OpCode.LEVEL_CHANGE_REQ)
    public void handleLevelChange(LevelChangeReq req) {
        int uid = req.uid;
        UserBattle user = UserBattleManager.INSTANCE.getUserBattle(uid);

        BattleManager.INSTANCE.setLevel(user.getRoomId(), uid, req.level);
        List<Integer> otherUidList = BattleManager.INSTANCE.getOtherUidList(user.getRoomId(), uid);

        LevelChangeResp resp = new LevelChangeResp();
        resp.uid = uid;
        resp.level = req.level;

        ChannelManager.INSTANCE.sendResponse(otherUidList, OpCode.LEVEL_CHANGE_RESP, resp);
    }


    public void senLeaveRoomResp(int uid) {
        ChannelManager.INSTANCE.sendResponse(uid, OpCode.LEAVE_ROOM_RESP, null);
    }

    public void sendUserJoinRoom(List<Integer> uidList, PlayerInfo info) {
        ChannelManager.INSTANCE.sendResponse(uidList, OpCode.USER_JOIN_ROOM, new PlayerEnterResp(info));
    }

    public void sendUserLeaveRoom(List<Integer> uidList, int exitUid) {
        ChannelManager.INSTANCE.sendResponse(uidList, OpCode.USER_LEAVE_ROOM, new PlayerExitResp(exitUid));
    }

    public static void sendSyncAttack(List<Integer> uidList, AttackResp resp) {
        ChannelManager.INSTANCE.sendResponse(uidList, OpCode.SYNC_ATTACK_RESP, resp);
    }

    public static void sendSyncPosition(int uid, SyncResp response) {
        ChannelManager.INSTANCE.sendResponse(uid, OpCode.SYNC_BATTLE_RESP, response);
    }
}
