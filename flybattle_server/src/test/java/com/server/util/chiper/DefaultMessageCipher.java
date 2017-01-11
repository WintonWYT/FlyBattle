package com.server.util.chiper;

/**
 * Created by wuyingtan on 2016/11/28.
 */
public class DefaultMessageCipher implements IMessageCipher {
    public DefaultMessageCipher() {
    }

    @Override
    public byte[] encryptMsg(byte[] data, int msgSeqNo) {
        return data;
    }
}
