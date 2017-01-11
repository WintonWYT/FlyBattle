package flygame.extensions.redis;

/**
 * Redis相关工具类
 */
public abstract class RedisUtils {

    private static final char SEMICOLON_SEPARATOR = ':';

    public static String join(Object... params) {
        return join(SEMICOLON_SEPARATOR, params);
    }

    public static String join(char separator, Object... params) {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(params[i]);
        }
        return sb.toString();
    }

}
