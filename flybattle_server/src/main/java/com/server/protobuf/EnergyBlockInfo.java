package com.server.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2017/1/11.
 */
public class EnergyBlockInfo {
    @Protobuf(required = true)
    public int eid;
    @Protobuf(required = true)
    public int type;
    @Protobuf(required = true)
    public Vec3 pos;

    public EnergyBlockInfo() {

    }

    public EnergyBlockInfo(int eid, int type, Vec3 pos) {
        this.eid = eid;
        this.type = type;
        this.pos = pos;
    }
}
