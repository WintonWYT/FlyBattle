package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/27.
 */
public class ExitBattleResp {
    @Protobuf(required = true)
    public boolean isSuccess;

    public ExitBattleResp() {

    }

    public ExitBattleResp(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
