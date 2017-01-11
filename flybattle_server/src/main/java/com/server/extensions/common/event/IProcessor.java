package com.server.extensions.common.event;

/**
 * Created by wuyingtan on 2016/12/12.
 */
public interface IProcessor<K, V> {
    void process(K K, V v);
}
