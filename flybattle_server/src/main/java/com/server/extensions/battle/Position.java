package com.server.extensions.battle;

import com.server.protobuf.Vec3;

/**
 * Created by wuyingtan on 2016/12/27.
 */
public class Position {
    public Vec3 pos;
    public Vec3 dir;


    public Position(Vec3 pos, Vec3 dir) {
        this.pos = pos;
        this.dir = dir;
    }
}
