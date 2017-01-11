package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.PlayerInfo;

/**
 * Created by wuyingtan on 2016/12/28.
 */
public class PlayerEnterResp {
    @Protobuf(required = true)
    public PlayerInfo playerInfo;

    public PlayerEnterResp() {

    }

    public PlayerEnterResp(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }
}
