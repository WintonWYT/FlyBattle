package com.flybattle.battle.block;

import com.flybattle.battle.core.BattleCenter;
import com.flybattle.battle.util.BattlefieldConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyingtan on 2017/1/16.
 */
public enum BlockChangeNotice {
    INSTANCE;
    private static final int THREAD_SIZE = 100;

    private static final long REBUILD_TIME = BattlefieldConfig.BLOCK_RESET_TIME;

    private ScheduledExecutorService noticeService = Executors.newScheduledThreadPool(THREAD_SIZE);

    public void noticeBlockChange(int roomId, Block block) {
        noticeService.schedule(() -> sendBlockNotice(roomId, block), REBUILD_TIME, TimeUnit.SECONDS);
    }

    private void sendBlockNotice(int roomId, Block block) {
        block.setIsUsed(false);

        BattleCenter.getInstance().sendUpdateEnergyBlock(roomId, block);

    }
}
