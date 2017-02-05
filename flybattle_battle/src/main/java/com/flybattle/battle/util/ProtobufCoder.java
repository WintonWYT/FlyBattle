package com.flybattle.battle.util;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import io.netty.buffer.ByteBuf;

/**
 * Created by wuyingtan on 2017/1/5.
 */
public class ProtobufCoder {


    @SuppressWarnings("unchecked")
    public static void encode(int opCode, Object obj, ByteBuf buffer) throws Exception {

        if (obj == null) {
            buffer.writeInt(0);
            return;
        }

        Codec codec = ProtobufProxy.create(obj.getClass());
        int size = codec.size(obj);
        buffer.writeInt(size + 8);
        buffer.writeInt(opCode);
        buffer.writeInt(size);
        byte[] bytes = codec.encode(obj);
        buffer.writeBytes(bytes);

    }

    public static Object decode(int opCode, ByteBuf buffer) throws Exception {
        if (buffer.readableBytes() == 0) {
            return null;
        }
        CommandData cd = CommandHandler.INSTANCE.getCommandData(opCode);
        Class requestClass = cd.inputClass;
        if (requestClass == null || Void.class.equals(requestClass.getClass())) {
            return null;
        }
        Codec codec = ProtobufProxy.create(requestClass);
        int size = buffer.readInt();
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return codec.decode(bytes);
    }
}
