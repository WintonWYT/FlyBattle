package com.server.extensions;

import com.baitian.mobileserver.logger.ServerLogger;
import com.baitian.mobileserver.validator.DefaultMessageValidator;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by wuyingtan on 2016/11/25.
 */
public class MyMessageValidator extends DefaultMessageValidator {
    private volatile Cipher decrCipher;

    public MyMessageValidator() {
        this.decrCipher = createDecrCipher();
    }

    private Cipher createDecrCipher() {
        Cipher cipher = null;
        try {
            String seed = "F23D4A0444E4EB0B6B4972802C5CC002";
            byte[] enCodeFormat = hexStringToBytes(seed);
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
        } catch (NoSuchAlgorithmException e) {
            ServerLogger.error("init MyMessageValidator error:", e);
        } catch (NoSuchPaddingException e) {
            ServerLogger.error("init MyMessageValidator error:", e);
        } catch (InvalidKeyException e) {
            ServerLogger.error("init MyMessageValidator error:", e);
        }
        return cipher;
    }

    @Override
    public int nextMsgSeqNo(int now, long userId, int sessionId) {
        long next = 0;//(long)now * 2L + 1L;
        return next >= 2147483647L ? 0 : (int) next;
    }
//    @Override
//    public byte[] decryptMsg(byte[] data, int msgSeqNo) throws InvalidRequestException {
//        try {
//            return decrCipher.doFinal(data); // 解密
//        } catch (IllegalBlockSizeException e) {
//            ServerLogger.error("decryptMsg error:", e);
//        } catch (BadPaddingException e) {
//            ServerLogger.error("decryptMsg error:", e);
//        }
//        throw new InvalidRequestException();
//    }

    /**
     * 十六进制字符串转换成byte数组
     *
     * @param hexString the hex string
     * @return byte[]
     */
    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
