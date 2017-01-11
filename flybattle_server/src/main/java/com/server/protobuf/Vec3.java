package com.server.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class Vec3 {
    @Protobuf(required = true)
    public float x;
    @Protobuf(required = true)
    public float y;
    @Protobuf(required = true)
    public float z;

    public Vec3() {
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
