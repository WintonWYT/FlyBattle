package com.server.util;

import com.baitian.mobileserver.buffer.IoBuffer;

/**
 * Created by wuyingtan on 2016/11/28.
 */
public interface IHandler {
    void handleResponse(byte extId, byte cmd, IoBuffer buffer);
}
