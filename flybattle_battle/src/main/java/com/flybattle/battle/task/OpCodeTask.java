package com.flybattle.battle.task;

import com.flybattle.battle.core.BattleCenter;
import com.flybattle.battle.util.CommandData;
import com.flybattle.battle.util.CommandHandler;
import com.flybattle.battle.util.ProtobufCoder;
import io.netty.buffer.ByteBuf;

/**
 * Created by wuyingtan on 2017/1/6.
 */
public class OpCodeTask implements ITask {
    private int opCode;
    private ByteBuf buf;

    public OpCodeTask(int opCode, ByteBuf buf) {
        this.opCode = opCode;
        this.buf = buf;
    }

    @Override
    public void run() {
        try {
            Object object = ProtobufCoder.decode(opCode, buf);
            CommandData cd = CommandHandler.INSTANCE.getCommandData(opCode);
            cd.method.invoke(BattleCenter.getInstance(), object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
