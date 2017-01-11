package com.server.protobuf.request;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/21.
 */
public class SignUpReq {
    @Protobuf(required = true)
    public String name;
}
