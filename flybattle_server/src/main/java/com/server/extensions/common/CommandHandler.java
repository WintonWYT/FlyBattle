package com.server.extensions.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public enum CommandHandler {
    INSTANCE;

    private Map<Integer, CommandData> extCmds = new HashMap<>();

    public void addExtCmd(byte extensionId, byte cmd, CommandData commandData) {
        extCmds.put(getExtCmd(extensionId, cmd), commandData);
    }

    public CommandData getCommandData(byte extensionId, byte cmd) {
        return extCmds.get(getExtCmd(extensionId, cmd));
    }

    public static int getExtCmd(byte extensionId, byte cmd) {
        return (extensionId << Byte.SIZE) | cmd;
    }


}
