package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.protobuf.AttackInfo;

/**
 * Created by wuyingtan on 2016/12/30.
 */
public class AttackResp {
    @Protobuf(required = true)
    public AttackInfo info;
}
