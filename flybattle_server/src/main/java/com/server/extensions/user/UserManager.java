package com.server.extensions.user;

import com.server.extensions.config.ResultCode;
import com.server.protobuf.response.SignUpResp;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public enum UserManager {
    INSTANCE;

    public void addUser(User user) {
        UserDao.getInstance().addUser(user.getUserId(), user.getUserName(), user.getUserPwd());
    }

    public User getUserById(long userId) {
        return UserDao.getInstance().getUserById(userId);
    }

    public SignUpResp setUserName(long userId, String userName) {
        int result = UserDao.getInstance().setUserName(userId, userName);
        if (result <= 0) {
            SignUpResp resp = new SignUpResp();
            resp.result = ResultCode.FAIL;
            return resp;
        }
        return new SignUpResp();
    }

}
