package com.server.extensions.rpc.server;

import com.baitian.rpc.server.Protocol;
import com.baitian.rpc.server.RpcServer;
import com.baitian.rpc.server.RpcServerBuilder;
import com.baitian.rpc.server.Serialization;
import flygame.rpc.config.AppConfig;
import flygame.rpc.config.AppServicesInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by wuyingtan on 2017/1/12.
 */
public enum RpcServiceServer {
    INSTANCE;
    RpcServerBuilder serverBuilder = new RpcServerBuilder();
    RpcServer server;
    String registry;

    public void init() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        AppConfig instance = AppConfig.instance("configs/rpcConfig.xml", classLoader);
        registry = instance.getRegistry();
        int port = instance.getPort();
        String protocol = instance.getProtocol();
        int numWorkers = instance.getNumWorkers();
        int requestTimeoutMillis = instance.getRequestTimeoutMillis();
        serverBuilder.port(port, Protocol.valueOf(protocol));
        if (numWorkers != 0) {
            serverBuilder.numWorkers(numWorkers);
        }
        if (requestTimeoutMillis != 0) {
            serverBuilder.defaultRequestTimeoutMillis(requestTimeoutMillis);
        }
    }

    public void start() {
        List<AppServicesInfo> appServices;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        AppConfig instance = AppConfig.instance("configs/rpcConfig.xml", classLoader);
        appServices = instance.getAppServicesInfo();

        appServices.forEach(service -> {
            try {
                serverBuilder.serviceAt(service.getServiceName(), Class.forName(service.getIface()), Class.forName(service.getImplClzName()).newInstance(),
                        Serialization.valueOf(service.getSerialization()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (StringUtils.isEmpty(registry)) {
            throw new RuntimeException("no registry info! plz check!");
        }
        server = serverBuilder.build(registry);
        server.start();
    }

    public void startService() {
        init();
        start();
    }

    public void stop() {
        server.stop();
    }
}
