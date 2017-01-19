package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;
import com.server.protobuf.PlayerInfo;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyingtan on 2016/12/22.
 */
//非线程安全且与battleField有较高的耦合性
public enum BattlefieldPool {
    INSTANCE;
    //设置最大大小
    private final static int MAX_SIZE = GameConfig.MAX_ROOM_SIZE;
    //池的大小
    private int size = 0;
    //可使用的roomId
    private final LinkedList<Integer> freeRoomId = new LinkedList<>();
    //可使用的room
    private final LinkedList<Battlefield> roomFreeList = new LinkedList<>();
    //所有的room
    private final List<Battlefield> roomAllList = new ArrayList<>();

    private final Map<Integer, Future> battleFuture = new HashMap<>();
    private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(GameConfig.MAX_BATTLE_SIZE);

    public synchronized PlayerInfo joinRoom(long userId, String userName, Position pos) {
        Battlefield toJoin = getValidRoom();

        PlayerInfo info = new PlayerInfo();
        info.uid = toJoin.addUserObject(userId, userName, pos);
        info.roomId = toJoin.getFieldId();
        info.uname = userName;
        info.pos = pos.pos;
        if (toJoin.isFull()) {
            roomFreeList.remove(toJoin);
        }
        //开始战斗
        if (!toJoin.isStart()) {
            startBattle(toJoin.getFieldId());
        }
        return info;
    }

    public synchronized void leaveRoom(int roomId, long userId, int uid) {
        Battlefield room = roomAllList.get(roomId);
        room.removeUserObject(userId, uid);
        if (room.isEmpty()) {
            endBattle(roomId);
            roomAllList.remove(room);
            roomFreeList.remove(room);
            freeRoomId.add(room.getFieldId());
            return;
        }
        if (!roomFreeList.contains(room)) {
            roomFreeList.add(room);
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
        if (roomFreeList.size() == 0) {
            creatRoom();
        }
        room = roomFreeList.getFirst();
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
            //待处理
            if (size > MAX_SIZE) {
                return;
            }
        } else {
            roomId = freeRoomId.poll();
        }
        Battlefield room = new Battlefield(roomId);
        roomAllList.add(room);
        roomFreeList.add(room);
    }

    private boolean startBattle(int roomId) {
        Battlefield room = getRoomById(roomId);
        if (room.beginSend()) {
            Future future = scheduledService.scheduleAtFixedRate(room, 0, GameConfig.BATTLE_SYNC_TIME, TimeUnit.MILLISECONDS);
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
