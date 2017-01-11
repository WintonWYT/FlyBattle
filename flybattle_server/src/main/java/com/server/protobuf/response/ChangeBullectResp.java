package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2017/1/10.
 */
public class ChangeBullectResp {
    @Protobuf(required = true)
    public int uid;
    @Protobuf(required = true)
    public int bullectType;
}
