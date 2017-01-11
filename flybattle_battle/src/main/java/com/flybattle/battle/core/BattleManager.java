package com.flybattle.battle.core;

import com.flybattle.battle.domain.BattleInfo;
import com.flybattle.battle.util.BattleConfig;
import com.server.protobuf.DamageInfo;
import com.server.protobuf.PlayerInfo;
import com.server.protobuf.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public enum BattleManager {
    INSTANCE;
    private BattlefieldPool battlefieldPool = BattlefieldPool.INSTANCE;
    private final Map<Integer, Future> battleFuture = new HashMap<>();
    private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(BattleConfig.MAX_BATTLE_SIZE);
    private int x = 50;

    //有同步问题
    public synchronized PlayerInfo joinRoom(String userName) {
        Vec3 pos = initPosition();
        BattleInfo position = new BattleInfo(pos, null);
        PlayerInfo playInfo = battlefieldPool.joinRoom(userName, position);
        return playInfo;
    }

    private Vec3 initPosition() {
        Vec3 pos = new Vec3();
        pos.x = x;
        x = x + 20;
        pos.y = 50;
        pos.z = 50;
        return pos;
    }

    public List<Integer> getOtherUidList(int roomId, int uid) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<Integer> otherUidList = room.getOtherUid(uid);
        return otherUidList;
    }

    public List<PlayerInfo> getOtherPlayerInfoList(int roomId, int uId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<PlayerInfo> otherUserInfos = room.getOtherUserInfo(uId);
        return otherUserInfos;
    }

    public void addDamageInfo(int roomId, DamageInfo info) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.addDamageInfo(info);
    }


    public synchronized void leaveRoom(int roomId, int uid) {
        battlefieldPool.leaveRoom(roomId, uid);
    }


    public void updatePosition(int roomId, int uid, BattleInfo pos) {
        if (battlefieldPool.isEmpty()) {
            return;
        }
        Battlefield room = battlefieldPool.getRoomById(roomId);
        room.updatePosition(uid, pos);
    }


    public boolean startBattle(int roomId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room.beginSend()) {
            Future future = scheduledService.scheduleAtFixedRate(room, 0, BattleConfig.BATTLE_SYNC_TIME, TimeUnit.MILLISECONDS);
            battleFuture.put(roomId, future);
            return true;
        }
        return false;
    }

    public void endBattle(int roomId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        room.stopSend();
        battleFuture.get(roomId).cancel(true);
    }

}
