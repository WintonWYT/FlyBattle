package com.server.util.chiper;

import flex.messaging.util.StringUtils;

/**
 * Created by wuyingtan on 2016/11/28.
 */
public class ChiperFactory {
    public static IMessageCipher createMessageCipher(String cipher) {
        if (StringUtils.isEmpty(cipher) || cipher.equalsIgnoreCase("default")) {
            return new DefaultMessageCipher();
        } else if (cipher.equals("aes")) {
            return new AesMessageCipher();
        }
        return null;
    }
}
