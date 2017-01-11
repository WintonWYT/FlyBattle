package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.PlayerSynInfo;

/**
 * Created by wuyingtan on 2016/12/26.
 */
public class SyncReq {
    @Protobuf(required = true)
    public PlayerSynInfo myInfo;
}
