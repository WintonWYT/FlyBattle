package com.flybattle.battle.server;

import com.flybattle.battle.domain.OpCode;
import com.flybattle.battle.task.TaskDispatcher;
import com.flybattle.battle.util.BattleLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by wuyingtan on 2017/1/4.
 */
public class BattleHandler extends ChannelInboundHandlerAdapter {

    public static final int MSG_HEAD_LEN = 4;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        BattleLogger.info("Ip:" + ctx.channel().remoteAddress() + "  login");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        demultiplexAndDispatch(buf, ctx);
    }

    private void demultiplexAndDispatch(ByteBuf buf, ChannelHandlerContext ctx) {
        int opCode = buf.readInt();
        switch (opCode) {
            case OpCode.JOIN_ROOM_REQ:
                TaskDispatcher.handleFirstJoin(opCode, buf, ctx);
                break;
            default:
                TaskDispatcher.handleOpCode(opCode, buf);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        BattleLogger.info(ctx.channel().remoteAddress() + "  leave");
        ChannelManager.INSTANCE.removeChannel(ctx);
    }

}
