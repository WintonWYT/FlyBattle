package com.flybattle.battle.core;

import com.flybattle.battle.block.EnergyBlock;
import com.flybattle.battle.domain.BattleInfo;
import com.server.protobuf.DamageInfo;
import com.server.protobuf.EnergyBlockInfo;
import com.server.protobuf.PlayerInfo;
import com.server.protobuf.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public enum BattleManager {
    INSTANCE;
    private BattlefieldPool battlefieldPool = BattlefieldPool.INSTANCE;

    private int x = 50;

    public PlayerInfo joinRoom(String userName) {
        Vec3 pos = initPosition();
        BattleInfo position = new BattleInfo(pos, null);
        PlayerInfo playInfo = battlefieldPool.joinRoom(userName, position);
        return playInfo;
    }

    public void leaveRoom(int roomId, int uid) {
        battlefieldPool.leaveRoom(roomId, uid);
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

    public List<Integer> getOtherUidList(int roomId, int uid) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<Integer> otherUidList = room.getOtherUid(uid);
        return otherUidList;
    }

    public List<Integer> getAllUid(int roomId) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        List<Integer> uidList = room.getAllUid();
        return uidList;
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

    public void setLevel(int roomId, int uid, int level) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.setLevel(uid, level);
    }

    public void addDamageInfo(int roomId, DamageInfo info) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.addDamageInfo(info);
    }


    public void updatePosition(int roomId, int uid, BattleInfo pos) {
        Battlefield room = battlefieldPool.getRoomById(roomId);
        if (room == null) {
            return;
        }
        room.updatePosition(uid, pos);
    }

}
