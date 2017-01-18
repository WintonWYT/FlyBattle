package com.flybattle.battle.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by wuyingtan on 2017/1/18.
 */
public class MsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //处理粘包

        if (in.readableBytes() < 4) {
            return;
        }
        int size = in.readInt();
        boolean hasSizeBytes = true;
        while (in.readableBytes() >= size) {
            hasSizeBytes = false;
            ByteBuf result = Unpooled.buffer(size + 4);
            in.readerIndex(in.readerIndex());
            in.readBytes(result, size);
            out.add(result);
            if (in.readableBytes() < 4) {
                break;
            } else {
                size = in.readInt();
                hasSizeBytes = true;
            }
        }

        if (hasSizeBytes) {
            in.readerIndex(in.readerIndex() - 4);
        }
    }
}
