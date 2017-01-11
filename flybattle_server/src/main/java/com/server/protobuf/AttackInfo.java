package com.server.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/26.
 */
public class AttackInfo {
    @Protobuf(required = true)
    public int uid;
    @Protobuf(required = true)
    public int type;
    @Protobuf(required = true)
    public Vec3 pos3;
    @Protobuf(required = true)
    public Vec3 dir;
}
