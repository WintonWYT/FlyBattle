package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.server.extensions.config.ResultCode;

/**
 * Created by wuyingtan on 2016/12/22.
 */
public class SignUpResp extends ResultResp {
    @Protobuf
    public String userName;

    public SignUpResp() {
        super(ResultCode.SUCCESS);
    }
}
