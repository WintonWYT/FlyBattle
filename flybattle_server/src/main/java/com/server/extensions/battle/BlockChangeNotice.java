package com.server.extensions.battle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyingtan on 2017/1/11.
 */
public enum BlockChangeNotice {
    INSTANCE;
    private int size = 100;
    //时间单位为秒
    private static final long BUILD_TIME = 5;
    private ScheduledExecutorService noticeService = Executors.newScheduledThreadPool(size);

    public void noticeBlockChange(int roomId, EnergyBlock energyBlock) {
        noticeService.schedule(() -> sendBlockNotic(roomId, energyBlock), BUILD_TIME, TimeUnit.SECONDS);
    }

    public void sendBlockNotic(int roomId, EnergyBlock block) {
        block.setIsUsed(false);
        BattleExtension.SendUpdateBlockPos(roomId, block);
    }
}
