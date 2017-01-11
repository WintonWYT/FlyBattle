package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2017/1/11.
 */
public class UpdateBlockReq {
    @Protobuf(required = true)
    public int eid;
}
