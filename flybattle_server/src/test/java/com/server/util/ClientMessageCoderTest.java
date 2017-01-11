package com.server.util;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.baitian.mobileserver.buffer.IoBuffer;

import java.io.IOException;

/**
 * Created by wuyingtan on 2016/11/30.
 */
public class ClientMessageCoderTest {
    public static byte[] encode(Object o) {
        if(o == null) {
            return null;
        }
        Codec codec = ProtobufProxy.create(o.getClass());
        try {
            return codec.encode(o);
        } catch (IOException e) {
            throw new IllegalArgumentException("<<ClientMessageCoderTest>> encode error");
        }
    }
    public static <T> T decode(IoBuffer buffer, Class<T> clazz) {
        Codec<T> codec = ProtobufProxy.create(clazz);
        try {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return codec.decode(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
