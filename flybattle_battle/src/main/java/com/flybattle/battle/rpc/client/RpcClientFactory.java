package com.flybattle.battle.rpc.client;

import com.baitian.rpc.client.Option;
import com.baitian.rpc.client.RpcClientBuilder;
import flygame.rpc.config.RpcConfig;

import java.time.Duration;

/**
 * Created by wuyingtan on 2017/1/12.
 */
public class RpcClientFactory {
    public static <T> T newRpcClient(Class<T> clientType) {
        T result = RpcClientBuilder.newClient(RpcConfig.ZOOKEEPER_SERVER, clientType,
                Option.RESPONSE_TIMEOUT_MILLIS.setValue((long) 1000),
                Option.WRITE_TIMEOUT_MILLIS.setValue((long) 1),
                Option.CONNECT_TIMEOUT.setValue(Duration.ofMillis(100)),
                Option.MAX_CONCURRENCY.setValue(1));
        return result;
    }

    public static <T> T newRpcClientWithOptin(Class<T> clientType, Option<?, ?>... options) {
        T result = RpcClientBuilder.newClient(RpcConfig.ZOOKEEPER_SERVER, clientType,
                options);
        return result;
    }
}
