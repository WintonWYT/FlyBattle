package com.server.extensions.common;

import com.baitian.mobileserver.event.CmdEvent;
import com.baitian.mobileserver.extension.AbstractExtension;
import com.baitian.mobileserver.logger.ServerLogger;
import com.baitian.mobileserver.util.ExtensionHelper;
import com.server.extensions.user.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class ExtensionSupport extends AbstractExtension {
    CommandHandler commandHandler = CommandHandler.INSTANCE;

    @Override
    protected void init() {
        super.init();
        creatCommondHandlerMap();
        oninit();
    }

    private void oninit() {

    }

    private void creatCommondHandlerMap() {
        Class<?> clasz = this.getClass();
        Method[] methods = clasz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Command.class)) {
                byte cmd = method.getAnnotation(Command.class).value();
                Class output = method.getReturnType();
                Class[] input = method.getParameterTypes();
                Class inputx = null;
                if (input.length > 1) {
                    inputx = input[1];
                }
                commandHandler.addExtCmd(getExtensionId(), cmd, new CommandData(method, inputx, output));
            }
        }
    }

    @Override
    protected void handleCmdRequest(CmdEvent cmdEvent) {
        byte cmd = cmdEvent.getCmd();
        User user = (User) cmdEvent.getUser();
        if (!beforeRequset(cmd)) {
            return;
        }
        handleRequest(cmd, user, cmdEvent.getParamObj());
        afterRequest();
    }

    private void handleRequest(byte cmd, User user, Object object) {
        // 建议使用annotation来构造cmd-method的映射表
        CommandData data = commandHandler.getCommandData(getExtensionId(), cmd);
        if (data != null) {
            Method m = data.method;
            try {
                Object result;
                if (object != null) {
                    result = m.invoke(this, user, object);
                } else {
                    result = m.invoke(this, user);
                }

                if (result != null && !m.getReturnType().equals(Void.TYPE)) {
                    sendResponse(user, cmd, result);
                }
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                ServerLogger.error("handleRequest error :" + cmd + "/" + m.getName(), t != null ? t : e);
            } catch (Throwable t) {
                ServerLogger.error("handleRequest error :", t);
            }
        }
    }

    private void sendResponse(User user, byte cmd, Object result) {
        ExtensionHelper.sendResponse(user, getExtensionId(), cmd, result);
    }

    private void afterRequest() {

    }

    private boolean beforeRequset(byte cmd) {
        return true;
    }
}
