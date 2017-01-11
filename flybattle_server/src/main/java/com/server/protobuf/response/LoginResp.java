package com.server.protobuf.response;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

/**
 * Created by wuyingtan on 2016/12/22.
 */
public class LoginResp {
    @Protobuf
    public boolean isFirstTimeLogin;
    @Protobuf
    public String userName;

    public LoginResp() {

    }

    public LoginResp(boolean isFirstTimeLogin, String userName) {
        this.isFirstTimeLogin = isFirstTimeLogin;
        this.userName = userName;
    }
}
