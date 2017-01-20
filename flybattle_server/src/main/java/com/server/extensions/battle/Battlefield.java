package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;
import com.server.protobuf.DamageInfo;
import com.server.protobuf.PlayerInfo;
import com.server.protobuf.PlayerSynInfo;
import com.server.protobuf.Vec3;
import com.server.protobuf.response.SyncResp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wuyingtan on 2016/12/6.
 */
public class Battlefield extends Thread {
    private int fieldId;

    private AtomicInteger uid = new AtomicInteger(0);
    //用户飞机与uid的映射
    private Map<Long, Integer> userId2Uid = new HashMap<>();
    //存储用户信息
    private List<PlayerInfo> userInfo = new ArrayList<>();
    //最大用户量配置
    private static final int maxRoomSize = GameConfig.MAX_ROOM_USER_SIZE;
    //用户伤害数据存储
    private SyncClearList<DamageInfo> damageList = new SyncClearList<>();

    //用户血量
    private Map<Integer, Integer> uid2Hp = new ConcurrentHashMap<>();

    private Map<Integer, Position> objcetId2Postion = new ConcurrentHashMap<>();

    private AtomicBoolean isRun = new AtomicBoolean(false);

    //能量块池
    private BlockPool energyBlockPood;

    public Battlefield(int fieldId) {
        this.fieldId = fieldId;
        this.energyBlockPood = new BlockPool(fieldId);
    }


    public int getRoomUsersNum() {
        return userId2Uid.size();
    }

    public boolean isStart() {
        return isRun.get();
    }

    public boolean isEmpty() {
        return getRoomUsersNum() == 0;
    }

    public boolean isFull() {
        return getRoomUsersNum() == maxRoomSize;
    }

    public boolean beginSend() {
        if (objcetId2Postion.size() >= 1) {
            isRun.set(true);
            return true;
        }
        return false;
    }

    public void stopSend() {
        isRun.set(false);
    }

    /**
     * 用户操作的物体
     *
     * @param userId
     * @param userName
     * @param pos
     * @return uid
     */
    public int addUserObject(long userId, String userName, Position pos) {
        if (isFull()) {
            return -1;
        }
        int uid = addObjct(pos);
        PlayerInfo playerInfo = new PlayerInfo(uid, userName, pos.pos);
        userInfo.add(playerInfo);
        userId2Uid.put(userId, uid);
        uid2Hp.put(uid, GameConfig.GAME_HP);
        return uid;
    }

    public void removeUserObject(long userId, int uid) {
        userId2Uid.remove(userId);
        removeUserInfo(uid);
        removeObject(uid);
    }

    private void removeUserInfo(int uid) {
        for (int i = 0; i < userInfo.size(); i++) {
            PlayerInfo info = userInfo.get(i);
            if (info.uid == uid)
                userInfo.remove(info);
        }
    }

    /**
     * 添加物体
     * 非线程安全，需要注意
     *
     * @param pos
     * @return uid
     */
    public int addObjct(Position pos) {
        objcetId2Postion.put(uid.incrementAndGet(), pos);
        return uid.get();
    }


    public void removeObject(int uid) {
        objcetId2Postion.remove(uid);
    }

    public boolean updatePosition(int uid, Position pos) {
        Position beforePos = objcetId2Postion.get(uid);
        if (beforePos != null) {
            objcetId2Postion.put(uid, pos);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        if (isRun.get()) {
            doSendFrame();
        }
    }

    private void doSendFrame() {
        List<DamageInfo> damageInfos = damageList.getAllDataAndClear();
        for (long userId : userId2Uid.keySet()) {
            int uid = userId2Uid.get(userId);
            SyncResp response = new SyncResp();
            response.playerSynInfos = getSyncInfo(uid, damageInfos);
            BattleExtension.sendSyncPosition(userId, response);
        }

    }


    private List<PlayerSynInfo> getSyncInfo(int uid, List<DamageInfo> damageInfos) {
        List<PlayerSynInfo> infos = new ArrayList<>(objcetId2Postion.size() - 1);
        for (int id : objcetId2Postion.keySet()) {
            if (id == uid) {
                continue;
            }
            PlayerSynInfo synInfo = new PlayerSynInfo();
            synInfo.uid = id;
            Position position = objcetId2Postion.get(id);
            synInfo.pos = position.pos;
            //尚未接收到同步信息时的dir为空
            if (position.dir == null) {
                position.dir = new Vec3();
            }
            synInfo.dir = position.dir;
            synInfo.damage = getDemage(id, damageInfos);
            infos.add(synInfo);
        }
        return infos;
    }

    private List<Integer> getDemage(int uid, List<DamageInfo> damageInfos) {
        List<Integer> infos = new ArrayList<>();
        damageInfos.stream().filter(info -> info.uid == uid).forEach(info -> infos.add(info.reduceHp));
        return infos;
    }


    public int getFieldId() {
        return fieldId;
    }


    public List<PlayerInfo> getOtherUserInfo(int uid) {
        List<PlayerInfo> otherPlayers = new ArrayList<>(userInfo.size() - 1);
        for (PlayerInfo player : userInfo) {
            if (player.uid == uid) {
                continue;
            }
            player.curHp = uid2Hp.get(player.uid);
            otherPlayers.add(player);
        }
        return otherPlayers;
    }

    public List<Long> getOtherPlayerIdList(long userId) {
        Set<Long> userSet = userId2Uid.keySet();
        List<Long> otherUserList = new ArrayList<>(userSet.size() - 1);
        for (Long id : userSet) {
            if (id == userId) {
                continue;
            }
            otherUserList.add(id);
        }
        return otherUserList;
    }

    public void addDamageInfo(DamageInfo damageInfo) {
        uid2Hp.computeIfPresent(damageInfo.uid, (k, v) -> (v - damageInfo.reduceHp));
        damageList.add(damageInfo);
    }

    public List<Long> getAllUser() {
        List<Long> userList = new ArrayList<>(userId2Uid.keySet());
        return userList;
    }

    public void setBullectType(int uid, int bullectType) {
        userInfo.stream().filter(info -> info.uid == uid).forEach(info -> info.bulletType = bullectType);
    }

    public void setLevel(int uid, int level) {
        userInfo.stream().filter(info -> info.uid == uid).forEach(info -> info.level = level);
    }

    public void updateEnergyBlcok(int eid) {
        //此方法是线程安全的
        energyBlockPood.updateBlock(eid);
    }

    public List<EnergyBlock> getAllBlcok() {
        return energyBlockPood.getAllBlcok();
    }
}
