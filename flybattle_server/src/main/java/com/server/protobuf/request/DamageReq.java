package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2017/1/3.
 */
public class DamageReq {
    @Protobuf(required = true, order = 1)
    public int uid;
    @Protobuf(required = true, order = 2)
    public int damage;
}
