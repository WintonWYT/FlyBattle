package com.flybattle.battle.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public enum CommandHandler {
    INSTANCE;

    private Map<Integer, CommandData> extCmds = new HashMap<>();

    public void addExtCmd(int opCode, CommandData commandData) {
        extCmds.put(opCode, commandData);
    }

    public CommandData getCommandData(int opCode) {
        return extCmds.get(opCode);
    }

    public void commandSupport(Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Command.class)) {
                int opCode = method.getAnnotation(Command.class).value();
                Class inputType = method.getParameterTypes()[0];
                CommandData commandData = new CommandData(method, inputType, method.getReturnType());
                addExtCmd(opCode, commandData);
            }
        }
    }


}
