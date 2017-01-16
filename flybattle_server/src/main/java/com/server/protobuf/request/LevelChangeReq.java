package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2017/1/10.
 */
public class LevelChangeReq {
    @Protobuf(required = true)
    public int uid;
    @Protobuf(required = true, order = 1)
    public int level;
}
