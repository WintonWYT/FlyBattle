package com.server.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/26.
 */
public class DamageInfo {
    @Protobuf(required = true)
    public int uid;
    @Protobuf(required = true)
    public int reduceHp;
}
