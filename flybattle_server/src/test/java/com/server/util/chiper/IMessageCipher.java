package com.server.util.chiper;

/**
 * Created by wuyingtan on 2016/11/28.
 */
public interface IMessageCipher {
    public byte[] encryptMsg(byte[] data,int msgSeqNo);
}
