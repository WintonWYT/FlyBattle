package com.server.extensions;

import com.baitian.mobileserver.logger.ServerLogger;
import com.server.extensions.config.ExtConfig;
import flygame.common.ApplicationLocal;

/**
 * Created by wuyingtan on 2016/12/19.
 */
public class MyApplicationLocal extends ApplicationLocal {
    @Override
    public void info(String msg) {
        ServerLogger.info(msg);
    }

    @Override
    public void error(String msg) {
        ServerLogger.error(msg);
    }

    @Override
    public void error(String msg, Throwable t) {
        ServerLogger.error(msg, t);
    }

    @Override
    public int getZoneId() {
        return ExtConfig.instance().getZoneId();
    }
}
