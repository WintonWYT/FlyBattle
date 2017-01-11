package com.server.extensions.user;

import com.baitian.mobileserver.util.ExtensionHelper;
import com.server.extensions.common.Command;
import com.server.extensions.common.ExtensionSupport;
import com.server.protobuf.request.SignUpReq;
import com.server.protobuf.response.SignUpResp;

/**
 * Created by wuyingtan on 2016/12/21.
 */
public class UserExtension extends ExtensionSupport {
    private static final byte EXTENSION_ID = 2;
    //设置用户名字
    private static final byte SET_USER_NAME = 1;
    //回复是否第一次登陆
    private static final byte SEND_BACK_SIGNUP = 2;


    @Command(SET_USER_NAME)
    public void handleSetUserName(User user, SignUpReq request) {
        long userId = user.getUserId();
        String userName = request.name;
        SignUpResp resultResp = UserManager.INSTANCE.setUserName(userId, userName);
        resultResp.userName = userName;
        sendBackSignUp(user, resultResp);
    }

    public static void sendBackSignUp(User user, SignUpResp resp) {
        ExtensionHelper.sendResponse(user, EXTENSION_ID, SEND_BACK_SIGNUP, resp);
    }


}
