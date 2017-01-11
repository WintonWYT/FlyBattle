package com.flybattle.battle.block;

import com.server.protobuf.Vec3;

/**
 * Created by wuyingtan on 2017/1/9.
 */
public class EnergyBlock {
    private int eid;
    private Vec3 pos;
    public EnergyBlock(int eid,Vec3 pos){
        this.eid = eid;
        this.pos = pos;
    }

    public int getEid() {
        return eid;
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }
}
