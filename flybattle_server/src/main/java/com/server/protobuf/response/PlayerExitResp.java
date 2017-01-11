package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/28.
 */
public class PlayerExitResp {
    @Protobuf(required = true)
    public int uid;

    public PlayerExitResp() {

    }

    public PlayerExitResp(int uid) {
        this.uid = uid;
    }

}
