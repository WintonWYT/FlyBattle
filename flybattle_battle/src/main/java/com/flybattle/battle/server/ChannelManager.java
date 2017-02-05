package com.flybattle.battle.server;

import com.flybattle.battle.util.ProtobufCoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public enum ChannelManager {
    INSTANCE;
    AttributeKey<Integer> key = AttributeKey.newInstance("uid");
    private Map<Integer, ChannelHandlerContext> uid2Channel = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(200);


    public void addChannel(int uid, ChannelHandlerContext ctx) {
        ctx.attr(key).set(uid);
        uid2Channel.put(uid, ctx);
    }


    public void removeChannel(ChannelHandlerContext ctx) {
        int uid = ctx.attr(key).get();
        uid2Channel.remove(uid);
    }

    public void sendResponse(int uid, int opCode, Object object) {
        executor.execute(() -> {
            ByteBuf buf = getByteBuf(opCode, object);
            ChannelHandlerContext ctx = uid2Channel.get(uid);
            if (ctx != null) {
                ctx.writeAndFlush(buf);
            }
        });
    }

    private ByteBuf getByteBuf(int opCode, Object object) {
        ByteBuf buf = Unpooled.buffer();
        try {
            ProtobufCoder.encode(opCode,object, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buf;
    }

    public void sendResponse(Collection<Integer> uidList, int opCode, Object object) {
        if (uidList.size() == 0) {
            return;
        }
        executor.execute(() -> {
            ByteBuf buf = getByteBuf(opCode, object);
            for (int uid : uidList) {
                ChannelHandlerContext ctx = uid2Channel.get(uid);
                if (ctx != null) {
                    ctx.writeAndFlush(buf);
                }
            }
        });
    }

}
