package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.AttackInfo;

/**
 * Created by wuyingtan on 2016/12/30.
 */
public class AttackReq {
    @Protobuf(required = true, order = 1)
    public AttackInfo info;
}
