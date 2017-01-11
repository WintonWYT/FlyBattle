package flygame.extensions.redis;

import flygame.common.ApplicationLocal;
import flygame.extensions.db.TableMapOutput;
import flygame.extensions.db.TableMapper;


public class NameIds {

    private static RedisCluster cluster = RedisCluster.getRedisCluster(RedisClusterName.CACHE);
    private static final int ZONEID = ApplicationLocal.instance().getZoneId();

    private static final String GET_NAME = "SELECT name FROM Users WHERE userId=?";

    private static final int ONE_DAY = 86400;
    private static final int EXPIRE_SECONDS = 14 * ONE_DAY;//活跃用户
    private static final int TMP_SECONDS = 2 * ONE_DAY;//非活跃用户

    public static long getUserId(String name) {
        String id = cluster.get(nameKey(name));
        if(id == null) {
            return -1L;
        }
        return Long.parseLong(id);
    }

    public static String getName(Long userId) {
        String name = cluster.get(idKey(userId));
        if(name == null) {
            TableMapOutput tmo = TableMapper.quickMap(userId, GET_NAME);
            name = tmo.executeScalarString(new Object[] {userId});
            if(name != null && name.length() > 0) {
                setUserIdName(userId, name, TMP_SECONDS);
            }
        }
        return name;
    }

    public static void setUserIdName(long userId, String name) {
        setUserIdName(userId, name, EXPIRE_SECONDS);
    }

    private static void setUserIdName(long userId, String name, int seconds) {
        cluster.setex(idKey(userId), seconds, name);
        cluster.setex(nameKey(name), seconds, String.valueOf(userId));
    }

    public static void remove(long userId, String name) {
        cluster.del(nameKey(name));
        cluster.del(idKey(userId));
    }

    /**name是针对zone唯一的, 统一都用小写*/
    private static String nameKey(String name) {
        return String.format("nm:%d:%s", ZONEID, name.toLowerCase());
    }

    /**id是全局唯一的*/
    private static String idKey(long id) {
        return String.format("id:%d", id);
    }

}
