package com.server.extensions.battle;

import com.server.protobuf.Vec3;

/**
 * Created by wuyingtan on 2017/1/9.
 */
//能量块状态的逻辑需要实时同步
public class EnergyBlock {
    private int eid;
    private Vec3 pos;
    private boolean isUsed;
    private int type;

    public EnergyBlock(int eid, Vec3 pos) {
        this.eid = eid;
        this.pos = pos;
        this.isUsed = false;
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

    public boolean isUsed() {
        return isUsed;
    }

    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
