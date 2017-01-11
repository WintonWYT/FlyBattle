package com.server.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/26.
 */
public class PlayerInfo {
    public int roomId;
    @Protobuf(required = true)
    public int uid;
    @Protobuf(required = true)
    public String uname;
    @Protobuf(required = true)
    public Vec3 pos;
    @Protobuf(required = true)
    public int curHp;
    @Protobuf(required = true)
    public int speed;
    @Protobuf(required = true)
    public int level;
    @Protobuf(required = true)
    public int bulletType;

    public PlayerInfo() {
        init();
    }

    public PlayerInfo(int uid, String uname, Vec3 pos) {
        this.uid = uid;
        this.uname = uname;
        this.pos = pos;
        init();
    }

    private void init() {
        bulletType = 1;
        speed = 5;
        level = 1;
        curHp = 200;
    }
}
