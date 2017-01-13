package com.server.extensions.rpc.service;

import flygame.rpc.Iface.ITest;
import org.apache.thrift.TException;

/**
 * Created by wuyingtan on 2017/1/13.
 */
public class TestServiceImpl implements ITest.Iface {
    @Override
    public String test(String send) throws TException {
        return "服务器操作完成" + send;
    }
}
