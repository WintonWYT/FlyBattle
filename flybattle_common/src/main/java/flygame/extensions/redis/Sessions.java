package flygame.extensions.redis;

import java.nio.charset.Charset;
import java.util.Base64;


public class Sessions {

    private static final int LOGIN_EXPIRE_SECONDS = 120 * 60;

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");
    private RedisCluster redisCluster = RedisCluster.getRedisCluster(RedisClusterName.CACHE);
    private IRedisSerializer serializer;

    private static final Sessions instance = new Sessions();

    private Sessions() {
        serializer = new JdkSerializer();
    }

    public static Sessions getInstance() {
        return instance;
    }

    public void putSession(String sessionId, LoginSign loginSign) {
        String key = buildKey(sessionId);
        String value = byteToString(serializer.serialize(loginSign));
        redisCluster.set(key, value, LOGIN_EXPIRE_SECONDS);
    }

    public LoginSign getLoginSign(String sessionId) {
        String key = buildKey(sessionId);
        return redisCluster.get(key, value -> {
            if (value != null) {
                return serializer.deserialize(stringToByte(value), LoginSign.class);
            } else {
                return null;
            }
        });
    }

    private String byteToString(byte[] bytes) {
        byte[] datas = Base64.getEncoder().encode(bytes);
        return new String(datas, CHARSET);
    }

    private byte[] stringToByte(String text) {
        return Base64.getDecoder().decode(text);
    }

    private String buildKey(String sessionId) {
        return RedisUtils.join("ses", sessionId);
    }

}
