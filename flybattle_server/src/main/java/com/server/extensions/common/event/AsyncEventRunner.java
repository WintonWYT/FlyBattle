package com.server.extensions.common.event;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wuyingtan on 2016/12/12.
 */
public class AsyncEventRunner<K, V> {
    private LinkedBlockingQueue<K> keyQueue = new LinkedBlockingQueue<>();
    private Map<K, ValueQueueWraper<V>> keyValueMap = new ConcurrentHashMap<>();
    private String name;
    private IProcessor<K, V> processor;
    private Thread[] workers;
    private int warnSize;
    private volatile boolean running;

    public AsyncEventRunner(int workerNum, int warnSize, String name, IProcessor processor) {
        this.name = name;
        this.warnSize = warnSize;
        this.processor = processor;
        this.running = false;
        this.workers = new Thread[workerNum];
        for (int i = 0; i < workerNum; i++) {
            String threadName = String.format("%s-Pool%d-Thread%d", name, workerNum, i + 1);
            workers[i] = new Thread(new workRunner(), threadName);
            workers[i].setDaemon(true);
        }
    }

    public void accept(K k, V v) {
        ValueQueueWraper<V> queueWraper = keyValueMap.get(k);
        if (queueWraper == null) {
            queueWraper = new ValueQueueWraper();
            ValueQueueWraper<V> oldValue = keyValueMap.putIfAbsent(k, queueWraper);
            if (oldValue != null) {
                queueWraper = oldValue;
            }
        }

        boolean add = true;
        synchronized (queueWraper) {
            int size = queueWraper.valueQueue.size();
            if (size == 0 && !queueWraper.hasAddedValue) {
                if (keyQueue.offer(k)) {
                    queueWraper.valueQueue.add(v);
                    queueWraper.hasAddedValue = true;
                } else {
                    add = false;
                }
            } else {
                if (size > 0 && size % warnSize == 0) {
                    //log
                }
                queueWraper.valueQueue.add(v);
            }
        }

    }

    public void remove(K k) {
        keyValueMap.remove(k);
    }

    public synchronized void Start() {
        if (running) return;
        running = true;
        for (Thread worker : workers) {
            worker.start();
        }
    }

    public synchronized void ShowDown() {
        if (!running) return;
        running = false;
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }

    private void process() {
        while (running) {
            try {
                K key = keyQueue.take();
                ValueQueueWraper<V> value = keyValueMap.get(key);
                if (value != null) {
                    V task;
                    synchronized (value) {
                        task = value.valueQueue.poll();
                    }
                    if (task != null) {
                        processor.process(key, task);
                    }
                    synchronized (value) {
                        resetValueQueue(key, value);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetValueQueue(K key, ValueQueueWraper<V> value) {
        value.hasAddedValue = false;
        int size = value.valueQueue.size();
        if (size > 0) {
            keyQueue.offer(key);
            value.hasAddedValue = true;
        }
    }


    private class workRunner implements Runnable {
        @Override
        public void run() {
            process();
        }
    }

    private class ValueQueueWraper<V> {
        public LinkedList<V> valueQueue = new LinkedList<>();
        public boolean hasAddedValue = false;
    }
}
