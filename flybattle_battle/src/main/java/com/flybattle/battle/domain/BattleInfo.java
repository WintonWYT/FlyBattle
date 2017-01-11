package com.flybattle.battle.domain;

import com.server.protobuf.Vec3;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class BattleInfo {
    public com.server.protobuf.Vec3 pos;
    public Vec3 dir;
    public int speed;

    public BattleInfo(Vec3 pos, Vec3 dir, int speed) {
        this.pos = pos;
        this.dir = dir;
        this.speed = speed;
    }

    public BattleInfo(Vec3 pos, Vec3 dir) {
        this.pos = pos;
        this.dir = dir;
    }
}
