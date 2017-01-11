package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;
import com.server.protobuf.PlayerInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    public PlayerInfo joinRoom(long userId, String userName, Position pos) {
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
            BattlefieldManager.INSTANCE.startBattle(toJoin.getFieldId());
        }
        return info;
    }

    public void leaveRoom(int roomId, long userId, int uid) {
        Battlefield room = roomAllList.get(roomId);
        room.removeUserObject(userId, uid);
        if (room.isEmpty()) {
            BattlefieldManager.INSTANCE.endBattle(roomId);
            roomAllList.remove(room);
            roomFreeList.remove(room);
            freeRoomId.add(room.getFieldId());
            return;
        }
        if (!roomFreeList.contains(room)) {
            roomFreeList.add(room);
        }
    }

    public Battlefield getRoomById(int roomId) {
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


    public boolean isEmpty() {
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


}
