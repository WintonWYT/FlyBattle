package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.EnergyBlockInfo;

/**
 * Created by wuyingtan on 2017/1/11.
 */
public class UpdateBlockResp {
    @Protobuf(required = true)
    public EnergyBlockInfo energyBlockInfo;
}
