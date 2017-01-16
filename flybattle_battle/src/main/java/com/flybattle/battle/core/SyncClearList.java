package com.flybattle.battle.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuyingtan on 2017/1/3.
 */

/**
 * 实现线程安全的同步清除List,在获取并清除数据时持有锁的时候，也能进行添加数据
 * 这种设计有待优化，为实现特定功能，暂定如此
 *
 * @param <T>
 */
public class SyncClearList<T> {
    private final List<T> dataList = new ArrayList<>();
    //相当于缓冲区
    private final List<T> toAddList = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void add(T data) {
        final ReentrantLock lock = this.lock;
        synchronized (toAddList) {
            if (lock.tryLock()) {
                try {
                    if (!toAddList.isEmpty()) {
                        dataList.addAll(toAddList);
                        toAddList.clear();
                    }
                    dataList.add(data);
                } finally {
                    lock.unlock();
                }
            } else {
                toAddList.add(data);
            }
        }
    }

    public List<T> getAllDataAndClear() {
        final ReentrantLock lock = this.lock;
        List<T> res = new ArrayList<>();
        lock.lock();
        try {
            if (!dataList.isEmpty()) {
                res.addAll(dataList);
                dataList.clear();
            }
            return res;
        } finally {
            lock.unlock();
        }
    }
}
