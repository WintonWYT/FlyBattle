package com.flybattle.battle.server;

import com.flybattle.battle.util.ProtobufCoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public enum ChannelManager {
    INSTANCE;
    Logger log = LoggerFactory.getLogger(this.getClass());
    AttributeKey<Integer> key = AttributeKey.newInstance("uid");
    private Map<Integer, ChannelHandlerContext> uid2Channel = new ConcurrentHashMap<>();

    public void addChannel(int uid, ChannelHandlerContext ctx) {
        ctx.attr(key).set(uid);
        uid2Channel.put(uid, ctx);
    }


    public void removeChannel(ChannelHandlerContext ctx) {
        int uid = ctx.attr(key).get();
        uid2Channel.remove(uid);
    }

    public void sendResponse(int uid, int opCode, Object object) {
        ByteBuf buf = getByteBuf(opCode, object);
        ChannelHandlerContext ctx = uid2Channel.get(uid);
        if (ctx != null) {
            ctx.writeAndFlush(buf);
        }
    }

    private ByteBuf getByteBuf(int opCode, Object object) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(opCode);
        try {
            ProtobufCoder.encode(object, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buf;
    }

    public void sendResponse(Collection<Integer> uidList, int opCode, Object object) {
        if (uidList.size() == 0) {
            return;
        }
        ByteBuf buf = getByteBuf(opCode, object);
        for (int uid : uidList) {
            ChannelHandlerContext ctx = uid2Channel.get(uid);
            ctx.writeAndFlush(buf);
        }
    }

}
