package com.flybattle.battle.core;

import com.flybattle.battle.domain.BattleInfo;
import com.flybattle.battle.util.BattlefieldConfig;
import com.server.protobuf.PlayerInfo;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyingtan on 2017/1/5.
 */
//todo id的回收部分可以优化
public enum BattlefieldPool {
    INSTANCE;
    //设置最大大小
    private final static int MAX_SIZE = BattlefieldConfig.MAX_ROOM_SIZE;
    //池的大小
    private int size = 0;
    //可使用的roomId
    private final LinkedList<Integer> freeRoomId = new LinkedList<>();
    //可使用的room
    private final LinkedList<Battlefield> freeRoomList = new LinkedList<>();
    //所有的room
    private final List<Battlefield> roomAllList = new ArrayList<>();

    private final Map<Integer, Future> battleFuture = new HashMap<>();

    private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(BattlefieldConfig.MAX_BATTLE_SIZE);

    public synchronized PlayerInfo joinRoom(String userName, BattleInfo pos) {
        Battlefield toJoin = getValidRoom();

        PlayerInfo info = new PlayerInfo();
        info.uid = toJoin.addUserObject(userName, pos);
        info.roomId = toJoin.getFieldId();
        info.uname = userName;
        info.pos = pos.pos;


        if (toJoin.isFull()) {
            freeRoomList.remove(toJoin);
        }
        //开始战斗
        if (!toJoin.isStart()) {
            startBattle(toJoin.getFieldId());
        }
        return info;
    }

    public synchronized void leaveRoom(int roomId, int uid) {
        Battlefield room = roomAllList.get(roomId);
        room.removeUserObject(uid);
        if (room.isEmpty()) {
            //结束战斗
            endBattle(roomId);
            roomAllList.remove(room);
            freeRoomList.remove(room);
            freeRoomId.add(room.getFieldId());
            return;
        }
        if (!freeRoomList.contains(room)) {
            freeRoomList.add(room);
        }
    }

    public synchronized Battlefield getRoomById(int roomId) {
        if (isEmpty()) {
            return null;
        }
        return roomAllList.get(roomId);
    }

    private Battlefield getValidRoom() {
        Battlefield room;
        if (freeRoomList.size() == 0) {
            creatRoom();
        }
        room = freeRoomList.getFirst();
        return room;
    }


    private boolean isEmpty() {
        if (roomAllList.size() == 0) {
            return true;
        }
        return false;
    }

    private void creatRoom() {
        int roomId;
        if (freeRoomId.size() == 0) {
            roomId = size;
            size++;
            if (size > MAX_SIZE) {
                return;
            }
        } else {
            roomId = freeRoomId.poll();
        }
        Battlefield room = new Battlefield(roomId);
        roomAllList.add(room);
        freeRoomList.add(room);
    }


    private boolean startBattle(int roomId) {
        Battlefield room = getRoomById(roomId);
        if (room.beginSend()) {
            Future future = scheduledService.scheduleAtFixedRate(room, 0, BattlefieldConfig.BATTLE_SYNC_TIME, TimeUnit.MILLISECONDS);
            battleFuture.put(roomId, future);
            return true;
        }
        return false;
    }

    private void endBattle(int roomId) {
        Battlefield room = getRoomById(roomId);
        room.stopSend();
        battleFuture.get(roomId).cancel(true);
    }

}
