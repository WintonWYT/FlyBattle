package com.server.extensions.battle;

import com.baitian.mobileserver.event.SystemEvent;
import com.baitian.mobileserver.util.ExtensionHelper;
import com.server.extensions.common.Command;
import com.server.extensions.common.ExtensionSupport;
import com.server.extensions.user.User;
import com.server.protobuf.*;
import com.server.protobuf.request.*;
import com.server.protobuf.response.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class BattleExtension extends ExtensionSupport {
    private static final byte EXTENSION_ID = 3;
    //加入房间请求
    private static final byte JOIN_ROOM_REQ = 1;
    //加入房间响应
    private static final byte JOIN_ROOM_RESP = 2;
    //发送同步战斗
    private static final byte SYNC_BATTLE_REQ = 3;
    //返回同步战斗
    private static final byte SYNC_BATTLE_RESP = 4;
    //离开房间请求
    private static final byte LEAVE_ROOM_REQ = 5;
    //离开房间响应
    private static final byte LEAVE_ROOM_RESP = 6;
    //玩家加入房间通知
    private static final byte USER_JOIN_ROOM = 7;
    //玩家离开房间通知
    private static final byte USER_LEAVE_ROOM = 8;
    //玩家攻击同步请求
    private static final byte SYNC_ATTACK_REQ = 9;
    //玩家攻击同步返回
    private static final byte SYNC_ATTACK_RESP = 10;
    //玩家伤害同步请求
    private static final byte SYNC_DEMAGE_REQ = 11;
    //玩家加速请求
    private static final byte ACCE_REQ = 12;
    //玩家加速返回
    private static final byte ACCE_RESP = 13;
    //玩家子弹变更请求
    private static final byte CHANGE_BULLECT_REQ = 14;
    //玩家子弹变更返回
    private static final byte CHANGE_BULLECT_RESP = 15;
    //玩家等级变更请求
    private static final byte LEVEL_CHANGE_REQ = 16;
    //玩家等级变更返回
    private static final byte LEVEL_CHANGE_RESP = 17;
    //玩家吸收能量块请求
    private static final byte USER_ABSORB_BLOCK = 18;
    //能量块位置更新响应
    private static final byte UPDATE_BLOCK_POS = 19;

    @Command(JOIN_ROOM_REQ)
    public void handleJoinRoom(User user) {

        long userId = user.getUserId();
        String userName = user.getUserName();

        BattlefieldManager battlefieldManager = BattlefieldManager.INSTANCE;
        PlayerInfo info = battlefieldManager.joinRoom(userId, userName);
        user.setRoomIdAndUid(info.roomId, info.uid);

        EnterBattleResp response = new EnterBattleResp();
        response.myInfo = info;
        response.otherInfo = battlefieldManager.getOtherPlayerInfoList(info.roomId, info.uid);
        //response.energyBlockInfo = battlefieldManager.getAllEnergyBlock(info.roomId);
        sendJoinRoomResp(user, response);

        List<Long> userList = battlefieldManager.getOtherPlayerIdList(info.roomId, userId);
        sendUserJoinRoom(userList, info);
    }


    @Command(LEAVE_ROOM_REQ)
    public void handleLeaveRoom(User user) {

        int roomId = user.getRoomId();
        long userId = user.getUserId();
        int uid = user.getUid();

        BattlefieldManager.INSTANCE.leaveRoom(roomId, userId, uid);
        user.setRoomIdAndUid(-1, -1);
        sendLeaveRoomResp(user);

        List<Long> userList = BattlefieldManager.INSTANCE.getOtherPlayerIdList(roomId, userId);
        sendUserLeaveRoom(userList, uid);
    }


    @Command(SYNC_BATTLE_REQ)
    public void handleSyncBattle(User user, SyncReq request) {

        int roomId = user.getRoomId();
        PlayerSynInfo myInfo = request.myInfo;
        int uid = myInfo.uid;
        Vec3 pos = myInfo.pos;
        Vec3 dir = myInfo.dir;
        Position position = new Position(pos, dir);
        BattlefieldManager.INSTANCE.updatePosition(roomId, uid, position);
    }

    @Command(SYNC_ATTACK_REQ)
    public void handleSyncAttack(User user, AttackReq req) {
        int roomId = user.getRoomId();
        long userId = user.getUserId();
        List<Long> userList = BattlefieldManager.INSTANCE.getOtherPlayerIdList(roomId, userId);
        AttackResp resp = new AttackResp();
        resp.info = req.info;
        SendSyncAttack(userList, resp);
    }

    @Command(SYNC_DEMAGE_REQ)
    public void handleSyncDamage(User user, DamageReq req) {
        int roomId = user.getRoomId();
        DamageInfo info = new DamageInfo();
        info.uid = req.uid;
        info.reduceHp = req.damage;
        BattlefieldManager.INSTANCE.addDamageInfo(roomId, info);
    }

    @Command(ACCE_REQ)
    public void handleAccelerate(User user, AccelerateReq req) {
        int roomId = user.getRoomId();
        long userId = user.getUserId();
        List<Long> userList = BattlefieldManager.INSTANCE.getOtherPlayerIdList(roomId, userId);
        AccelaerateResp resp = new AccelaerateResp();
        resp.speed = req.speed;
        resp.uid = user.getUid();
        sendMultiResponse(userList, ACCE_RESP, resp);
    }

    @Command(CHANGE_BULLECT_REQ)
    public void handleBullectChange(User user, ChangeBullectReq req) {
        int roomId = user.getRoomId();
        long userId = user.getUserId();
        int uid = user.getUid();
        BattlefieldManager.INSTANCE.setBullectType(roomId, uid, req.bullectType);
        List<Long> userList = BattlefieldManager.INSTANCE.getOtherPlayerIdList(roomId, userId);
        ChangeBullectResp resp = new ChangeBullectResp();
        resp.bullectType = req.bullectType;
        resp.uid = uid;
        sendMultiResponse(userList, CHANGE_BULLECT_RESP, resp);
    }

    private static void sendMultiResponse(List<Long> userIdList, byte cmd, Object resp) {
        if (userIdList.size() == 0) {
            return;
        }
        List<User> users = getUserListById(userIdList);
        ExtensionHelper.sendResponse(users, EXTENSION_ID, cmd, resp);
    }

    @Command(LEVEL_CHANGE_REQ)
    public void handleLevelChange(User user, LevelChangeReq req) {
        int roomId = user.getRoomId();
        long userId = user.getUserId();
        List<Long> userList = BattlefieldManager.INSTANCE.getOtherPlayerIdList(roomId, userId);
        LevelChangeResp resp = new LevelChangeResp();
        resp.level = req.level;
        resp.uid = user.getUid();
        sendMultiResponse(userList, LEVEL_CHANGE_RESP, resp);
    }

    @Command(USER_ABSORB_BLOCK)
    public void handleUserAbsorbBlock(User user, UpdateBlockReq req) {
        int roomId = user.getRoomId();
        int eid = req.eid;
        BattlefieldManager.INSTANCE.updateEnergyBlock(roomId, eid);
    }

    public static void SendUpdateBlockPos(int roomId, EnergyBlock block) {
        List<Long> userIdList = BattlefieldManager.INSTANCE.getAllUserList(roomId);
        List<User> userList = getUserListById(userIdList);
        UpdateBlockResp resp = new UpdateBlockResp();
        resp.energyBlockInfo = new EnergyBlockInfo(block.getEid(), block.getType(), block.getPos());
        ExtensionHelper.sendResponse(userList, EXTENSION_ID, UPDATE_BLOCK_POS, resp);
    }

    public void SendSyncAttack(List<Long> userList, AttackResp resp) {
        sendMultiResponse(userList, SYNC_ATTACK_RESP, resp);
    }

    public static void sendSyncPosition(Long userId, SyncResp resp) {
        User user = ExtensionHelper.getUserById(userId);
        ExtensionHelper.sendResponse(user, EXTENSION_ID, SYNC_BATTLE_RESP, resp);
    }

    public static void sendUserJoinRoom(List<Long> userList, PlayerInfo createPlayerInfo) {
        sendMultiResponse(userList, USER_JOIN_ROOM, new PlayerEnterResp(createPlayerInfo));
    }

    public static void sendUserLeaveRoom(List<Long> userList, int exitUid) {
        sendMultiResponse(userList, USER_LEAVE_ROOM, new PlayerExitResp(exitUid));
    }

    public void sendJoinRoomResp(User user, EnterBattleResp response) {
        ExtensionHelper.sendResponse(user, EXTENSION_ID, JOIN_ROOM_RESP, response);
    }

    public void sendLeaveRoomResp(User user) {
        ExtensionHelper.sendResponse(user, EXTENSION_ID, LEAVE_ROOM_RESP, new ExitBattleResp(true));
    }

    private static List<User> getUserListById(List<Long> userList) {
        List<User> users = new ArrayList<>();
        userList.forEach(userId -> users.add(ExtensionHelper.getUserById(userId)));
        return users;
    }

    //暂时如此处理
    @Override
    protected void handleUserDisconnect(SystemEvent event) {
        User user = (User) event.getUser();
        if (user.getRoomId() == -1) {
            return;
        }
        handleLeaveRoom(user);
    }


}
