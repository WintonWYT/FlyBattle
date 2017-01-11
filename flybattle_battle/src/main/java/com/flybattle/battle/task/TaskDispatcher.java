package com.flybattle.battle.task;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class TaskDispatcher {

    public static void handleFirstJoin(int opCode, ByteBuf buf, ChannelHandlerContext ctx) {
        TaskWokerCenter.INSTANCE.acceptTask(new StartTask(opCode, buf, ctx));
    }

    public static void handleOpCode(int opCode, ByteBuf buf) {
        TaskWokerCenter.INSTANCE.acceptTask(new OpCodeTask(opCode, buf));
    }
}
