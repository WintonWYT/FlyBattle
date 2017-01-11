package com.flybattle.battle.server;

import com.flybattle.battle.domain.OpCode;
import com.flybattle.battle.task.TaskDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by wuyingtan on 2017/1/4.
 */
public class BattleHandler extends ChannelHandlerAdapter {

    public static final int MSG_HEAD_LEN = 4;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
        System.out.println("welcome  " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (buf.readableBytes() < 4) {
            return;
        }
        int size = buf.readInt();
        boolean hasSizeBytes = true;
        while (buf.readableBytes() >= size) {
            hasSizeBytes = false;
            demultiplexAndDispatch(size, buf, ctx);
            if (buf.readableBytes() < 4) {
                break;
            } else {
                size = buf.readInt();
                hasSizeBytes = true;
            }
        }

        if (hasSizeBytes) {
            buf.readerIndex(buf.readerIndex() - MSG_HEAD_LEN);
        }

    }

    private void demultiplexAndDispatch(int size, ByteBuf buf, ChannelHandlerContext ctx) {
        final int netSize = size - MSG_HEAD_LEN;
        int opCode = buf.readInt();
        ByteBuf newBuf = Unpooled.buffer();
        newBuf.writeBytes(buf, netSize);
        switch (opCode) {
            case OpCode.JOIN_ROOM_REQ:
                TaskDispatcher.handleFirstJoin(opCode, newBuf, ctx);
                break;
            default:
                TaskDispatcher.handleOpCode(opCode, newBuf);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        System.out.println(ctx.channel().remoteAddress() + " leave");
        ChannelManager.INSTANCE.removeChannel(ctx);
    }

}
