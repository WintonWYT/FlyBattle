package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/9.
 */
public class ResultResp {
    @Protobuf(required = true)
    public int result;
    @Protobuf
    public String reason;

    public ResultResp() {

    }


    public ResultResp(int result) {
        this.result = result;
    }
}
