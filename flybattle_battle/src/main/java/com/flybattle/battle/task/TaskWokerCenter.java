package com.flybattle.battle.task;

import com.flybattle.battle.util.BattleConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuyingtan on 2017/1/6.
 */
public enum TaskWokerCenter {
    INSTANCE;
    //TODO 有内存撑爆的嫌疑待优化
    private ExecutorService executor = Executors.newFixedThreadPool(BattleConfig.WORKER_THREAD);

    public void acceptTask(ITask task) {
        executor.execute(task);
    }

    public void shutDown() {
        executor.shutdown();
    }

    public void shutDownNow() {
        List<Runnable> taskLeft = executor.shutdownNow();
    }

}
