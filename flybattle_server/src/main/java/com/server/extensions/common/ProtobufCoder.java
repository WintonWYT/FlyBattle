package com.server.extensions.common;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.baitian.mobileserver.buffer.IoBuffer;
import com.baitian.mobileserver.coder.IMessageCoder;
import com.baitian.mobileserver.extension.AdminExtension;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class ProtobufCoder implements IMessageCoder {
    CommandHandler commandHandler = CommandHandler.INSTANCE;

    @SuppressWarnings("unchecked")
    public void encode(byte extensionId, byte cmd, Object obj, IoBuffer buffer) throws Exception {
        if (extensionId == 0) {
            AdminExtension.ADMIN_CODER.encode(extensionId, cmd, obj, buffer);
        }

        if (obj == null) {
            buffer.putInt(0);
            return;
        }
        Codec codec = ProtobufProxy.create(obj.getClass());
        if (buffer.isDirect()) {
            byte[] bytes = codec.encode(obj);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        } else {
            int size = codec.size(obj);
            buffer.putInt(size);
            int pos = buffer.position();
            if (buffer.remaining() < size) {
                buffer.expand(pos, size);
            }

            byte[] bytes = codec.encode(obj);
            for (int i = 0; i < size; i++) {
                buffer.array()[i + pos] = bytes[i];
            }
            buffer.position(pos + size);
        }

    }

    public Object decode(byte extensionId, byte cmd, IoBuffer buffer) throws Exception {
        if (extensionId == 0) {
            return AdminExtension.ADMIN_CODER.decode(extensionId, cmd, buffer);
        }

        CommandData cd = commandHandler.getCommandData(extensionId, cmd);
        if (!buffer.hasRemaining() || cd == null) {
            return null;
        }
        Class requestClass = cd.inputClass;
        if (requestClass == null || Void.class.equals(requestClass.getClass())) {
            return null;
        }
        Codec codec = ProtobufProxy.create(requestClass);
        int size = buffer.getInt();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return codec.decode(bytes);
    }
}
