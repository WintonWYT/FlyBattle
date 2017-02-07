package com.server.extensions.battle;

import com.server.extensions.config.GameConfig;

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
    private static final long BUILD_TIME = GameConfig.BLOCK_RESET_TIME;
    private ScheduledExecutorService noticeService = Executors.newScheduledThreadPool(size);

    public void noticeBlockChange(int roomId, Block block) {
        noticeService.schedule(() -> sendBlockNotic(roomId, block), BUILD_TIME, TimeUnit.SECONDS);
    }

    private void sendBlockNotic(int roomId, Block block) {
        block.setIsUsed(false);
        BattleExtension.SendUpdateBlockPos(roomId, block);
    }
}
