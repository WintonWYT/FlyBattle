package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2017/1/10.
 */
public class AccelerateReq {
    @Protobuf(required = true,order = 1)
    public int speed;
}
