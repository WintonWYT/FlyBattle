package com.server.util.chiper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by wuyingtan on 2016/11/28.
 */
public class AesMessageCipher implements IMessageCipher {
    private static final String SEED = "F23D4A0444E4EB0B6B4972802C5CC002";

    private Cipher encrCipher;

    AesMessageCipher() {
        try {
            byte[] enCodeFormat = hexStringToBytes(SEED);
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            this.encrCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            this.encrCipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 十六进制字符串转换成byte数组
     *
     * @param hexString
     *            the hex string
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
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    @Override
    public byte[] encryptMsg(byte[] data, int msgSeqNo) {
        try {
            return encrCipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
