package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;
import com.server.protobuf.DamageInfo;
import com.server.protobuf.EnergyBlockInfo;
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
 * Created by wuyingtan on 2016/12/8.
 */
public enum BattlefieldManager {
    INSTANCE;
    private BattlefieldPool battlefieldPool = BattlefieldPool.INSTANCE;
    private final Map<Integer, Future> battleFuture = new HashMap<>();
    private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(GameConfig.MAX_BATTLE_SIZE);
    private int x = 50;

    //有同步问题
    public synchronized PlayerInfo joinRoom(long userId, String userName) {
        Vec3 pos = initPosition();
        Position position = new Position(pos, null);
        PlayerInfo playInfo = battlefieldPool.joinRoom(userId, userName, position);
        return playInfo;
    }

    private Vec3 initPosition() {
        Vec3 pos = new Vec3();
        pos.x = x;
        x = x + 10;
        pos.y = 50;
        pos.z = 50;
        return pos;
    }

    public List<EnergyBlockInfo> getAllEnergyBlock(int roomId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<EnergyBlock> blocks = room.getAllBlcok();
        List<EnergyBlockInfo> blockInfos = new ArrayList<>();
        blocks.forEach(block -> blockInfos.add(new EnergyBlockInfo(block.getEid(), block.getType(), block.getPos())));
        return blockInfos;
    }

    public void updateEnergyBlock(int roomId, int eid) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.updateEnergyBlcok(eid);
    }

    public List<Long> getAllUserList(int roomId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<Long> userList = room.getAllUser();
        return userList;
    }

    public List<PlayerInfo> getOtherPlayerInfoList(int roomId, int uId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<PlayerInfo> otherUserInfos = room.getOtherUserInfo(uId);
        return otherUserInfos;
    }

    public void setBullectType(int roomId, int uid, int bullectType) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.setBullectType(uid, bullectType);
    }

    public void addDamageInfo(int roomId, DamageInfo info) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.addDamageInfo(info);
    }

    public List<Long> getOtherPlayerIdList(int roomId, long userId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<Long> otherUserIds = room.getOtherPlayerIdList(userId);
        return otherUserIds;
    }

    public synchronized void leaveRoom(int roomId, long userId, int uid) {
        battlefieldPool.leaveRoom(roomId, userId, uid);
    }


    public void updatePosition(int roomId, int uid, Position pos) {
        if (battlefieldPool.isEmpty()) {
            return;
        }
        Battlefield room = battlefieldPool.getRoomById(roomId);
        room.updatePosition(uid, pos);
    }


    public boolean startBattle(int roomId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room.beginSend()) {
            Future future = scheduledService.scheduleAtFixedRate(room, 0, GameConfig.BATTLE_SYNC_TIME, TimeUnit.MILLISECONDS);
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
