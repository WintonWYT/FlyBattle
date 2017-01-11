package com.server.extensions;

import com.server.extensions.config.ExtConfig;
import flygame.common.ApplicationLocal;

/**
 * Created by wuyingtan on 2016/12/19.
 */
public class MyApplicationLocal extends ApplicationLocal {
    @Override
    public void info(String msg) {

    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void error(String msg, Throwable t) {

    }

    @Override
    public int getZoneId() {
        return ExtConfig.instance().getZoneId();
    }
}
