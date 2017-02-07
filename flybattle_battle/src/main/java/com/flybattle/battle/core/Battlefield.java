package com.flybattle.battle.core;

import com.flybattle.battle.block.BlockPool;
import com.flybattle.battle.block.Block;
import com.flybattle.battle.domain.BattleInfo;
import com.flybattle.battle.util.BattlefieldConfig;
import com.server.protobuf.DamageInfo;
import com.server.protobuf.PlayerInfo;
import com.server.protobuf.PlayerSynInfo;
import com.server.protobuf.response.SyncResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class Battlefield extends Thread {
    private int fieldId;

    private AtomicInteger uid = new AtomicInteger(0);
    //用户uid
    private List<Integer> userUid = new ArrayList<>();
    //存储用户信息
    private List<PlayerInfo> userInfo = new ArrayList<>();
    //最大用户量配置
    private static final int maxRoomSize = BattlefieldConfig.MAX_ROOM_USER_SIZE;
    //用户伤害数据存储
    private SyncClearList<DamageInfo> damageList = new SyncClearList<>();

    //用户血量
    private Map<Integer, Integer> uid2Hp = new ConcurrentHashMap<>();

    private Map<Integer, BattleInfo> objcetId2Postion = new ConcurrentHashMap<>();

    private AtomicBoolean isRun = new AtomicBoolean(false);

    //能量块
    private BlockPool energyBlockPood;


    public Battlefield(int fieldId) {
        this.fieldId = fieldId;
        this.energyBlockPood = new BlockPool(fieldId);
    }


    private int getRoomUsersNum() {
        return userUid.size();
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
     * @param userName
     * @param pos
     * @return uid
     */
    public int addUserObject(String userName, BattleInfo pos) {
        if (isFull()) {
            return -1;
        }
        int uid = addObjct(pos);
        PlayerInfo playerInfo = new PlayerInfo(uid, userName, pos.pos);
        userInfo.add(playerInfo);
        userUid.add(uid);
        uid2Hp.put(uid, BattlefieldConfig.GAME_HP);
        return uid;
    }

    public void removeUserObject(int uid) {
        userUid.remove(uid);
        uid2Hp.remove(uid);
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
     *
     * @param pos
     * @return uid
     */
    private int addObjct(BattleInfo pos) {
        objcetId2Postion.put(uid.incrementAndGet(), pos);
        return uid.get();

    }


    private void removeObject(int uid) {
        objcetId2Postion.remove(uid);
    }

    public boolean updatePosition(int uid, BattleInfo pos) {
        BattleInfo beforePos = objcetId2Postion.get(uid);
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
        for (int uid : userUid) {
            SyncResp response = new SyncResp();
            response.playerSynInfos = getSyncInfo(uid, damageInfos);

            BattleCenter.getInstance().sendSyncPosition(uid, response);
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
            BattleInfo position = objcetId2Postion.get(id);
            synInfo.pos = position.pos;
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


    public List<Integer> getOtherUid(int uid) {
        List<Integer> list = new ArrayList<>();
        userUid.stream().filter(id -> id != uid).forEach(id -> list.add(id));
        return list;
    }

    //此方法是线程安全的
    public void addDamageInfo(DamageInfo damageInfo) {
        uid2Hp.computeIfPresent(damageInfo.uid, (k, v) -> (v - damageInfo.reduceHp));
        damageList.add(damageInfo);
    }

    public void updateEnergyBlcok(int eid) {
        //此方法是线程安全的
        energyBlockPood.updateBlock(eid);
    }

    public List<Block> getAllBlcok() {
        return energyBlockPood.getAllBlock();
    }

    public List<Integer> getAllUid() {
        List<Integer> uidList = new ArrayList<>(userUid);
        return uidList;
    }

    public void setBullectType(int uid, int bullectType) {
        userInfo.stream().filter(info -> info.uid == uid).forEach(info -> info.bulletType = bullectType);
    }

    public void setLevel(int uid, int level) {
        userInfo.stream().filter(info -> info.uid == uid).forEach(info -> info.level = level);
    }
}
