package com.server.extensions;

import com.baitian.mobileserver.logger.ServerLogger;
import com.baitian.mobileserver.request.LoginRequest;
import com.baitian.mobileserver.servercomponent.IServerComponent;
import com.baitian.mobileserver.servercomponent.LoginResult;
import com.baitian.mobileserver.util.ServerConfig;
import com.server.extensions.common.LoginText;
import com.server.extensions.config.ExtConfig;
import com.server.extensions.rpc.server.RpcServiceServer;
import com.server.extensions.user.User;
import com.server.extensions.user.UserManager;
import com.server.protobuf.response.LoginResp;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class MyServerComponent implements IServerComponent {

    @Override
    public void init(String[] strings) {
        try {
            ExtConfig.instance().init();
        } catch (Exception e) {
            ServerLogger.error("<<MyServerComponent>> ExConfig init error", e);
        }

    }

    @Override
    public void beforeExtensionInit() {

    }

    @Override
    public void afterExtensionInit() {
        RpcServiceServer.INSTANCE.startService();
    }

    @Override
    public void beforeExtensionDestory() {

    }

    @Override
    public void afterExtensionDestory() {

    }

    @Override
    public void afterServerStartup() {

    }

    @Override
    public LoginResult doLogin(LoginRequest loginRequest) {
        long userId = Long.parseLong(loginRequest.getAccount());
        String passWord = loginRequest.getPassword();
        User user = UserManager.INSTANCE.getUserById(userId);
        if (user == null) {
            user = new User(userId, String.valueOf(userId), passWord);
            UserManager.INSTANCE.addUser(user);
            return new LoginResult(true, LoginText.LOGIN_SUCCESS, user, new LoginResp(true, user.getUserName()));
        }
        if (user.checkPwd(passWord)) {
            return new LoginResult(true, LoginText.LOGIN_SUCCESS, user, new LoginResp(false, user.getUserName()));
        }
        return new LoginResult(false, LoginText.PASSWORD_WORING, null);
    }

    @Override
    public long getUserId(LoginRequest loginRequest) {
//        long aLong = Long.parseLong(loginRequest.getAccount());
//        return aLong;
        return 0;
    }

    @Override
    public ServerConfig getServerConfig() {
        return ExtConfig.instance().getServerConfig();
    }
}
