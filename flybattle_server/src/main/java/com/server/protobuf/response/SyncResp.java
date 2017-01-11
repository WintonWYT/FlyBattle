package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.PlayerSynInfo;

import java.util.List;

/**
 * Created by wuyingtan on 2016/12/19.
 */
public class SyncResp {
    @Protobuf
    public List<PlayerSynInfo> playerSynInfos;
}
