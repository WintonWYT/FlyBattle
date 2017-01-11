package com.server.extensions.battle;

import com.server.protobuf.Vec3;

/**
 * Created by wuyingtan on 2016/12/27.
 */
public class Position {
    public Vec3 pos;
    public Vec3 dir;
//    public Integer speed;
//    public Integer level;
//    public Integer bulletType;

//    public Position(Vec3 pos, Vec3 dir, int speed, int bulletType) {
//        this.pos = pos;
//        this.dir = dir;
////        this.speed = speed;
////        this.bulletType = bulletType;
//    }

    public Position(Vec3 pos, Vec3 dir) {
        this.pos = pos;
        this.dir = dir;
    }
}
