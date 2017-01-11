package flygame.extensions.redis;

import flygame.common.ApplicationLocal;
import flygame.common.config.ConfReaderFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.stream.Collectors;


public final class RedisCluster {

    private static final Map<String, RedisCluster> clusters = new HashMap<>();

    static {
        loadAll();
    }

    private JedisCluster cluster;

    private RedisCluster(JedisCluster cluster) {
        this.cluster = cluster;
    }

    public static void loadAll() {
        Map<String, String> redisConfigs = loadConfig();
        redisConfigs.forEach(RedisCluster::init);
    }

    private static void init(String name, String urls) {
        HashSet<HostAndPort> hostAndPorts = new HashSet<>();
        for (String url : urls.split(",")) {
            String[] hostPort = url.split(":");
            HostAndPort hostAndPort = new HostAndPort(hostPort[0], Integer.parseInt(hostPort[1]));
            hostAndPorts.add(hostAndPort);
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxIdle(10);
        JedisCluster cluster = new JedisCluster(hostAndPorts, 2000, poolConfig);
        clusters.put(name, new RedisCluster(cluster));
    }

    private static Map<String, String> loadConfig() {
        return ConfReaderFactory.getConfigReader().loadRedisConfig();
    }

    public static RedisCluster getRedisCluster(String name) {
        return clusters.get(name);
    }

    public static void destory() {
        clusters.forEach((k, v) -> v.close());
    }

    private void close() {
        cluster.close();
    }

    private void error(String msg, Throwable t) {
        ApplicationLocal.instance().error(msg, t);
    }

    /****
     * 以下是实现方法, 需要的时候自己实现
     ***/

    public String set(String key, String value, int seconds) {
        return this.setex(key, seconds, value);
    }

    public String setex(String key, int seconds, String value) {
        try {
            return cluster.setex(key, seconds, value);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> setex error", e);
            return null;
        }
    }

    public <V> V get(String key, IStringBuilder<V> builder) {
        String value = get(key);
        return value == null? null: builder.build(value);
    }

    public String get(String key) {
        try {
            return cluster.get(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> get error", e);
            return null;
        }
    }

    public int getInt(String key) {
        String s = get(key);
        return s == null ? 0 : Integer.parseInt(s);
    }

    public long getLong(String key) {
        String s = get(key);
        return s == null ? 0L : Long.parseLong(s);
    }

    public Boolean exists(String key) {
        try {
            return cluster.exists(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> exists error", e);
            return null;
        }
    }

    @Deprecated
    public Long persist(String key) {
        return null;
    }

    @Deprecated
    public String type(String key) {
        return null;
    }

    public long expire(String key, int seconds) {
        try {
            return cluster.expire(key, seconds);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> expire error", e);
            return -1L;
        }
    }

    @Deprecated
    public Long pexpire(String key, long milliseconds) {
        return null;
    }

    public long expireAt(String key, long unixTime) {
        try {
            return cluster.expireAt(key, unixTime);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> expireAt", e);
            return -1L;
        }
    }

    @Deprecated
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return null;
    }

    public long ttl(String key) {
        try {
            return cluster.ttl(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> ttl", e);
            return -1L;
        }
    }

    @Deprecated
    public Boolean setbit(String key, long offset, boolean value) {
        return null;
    }

    @Deprecated
    public Boolean setbit(String key, long offset, String value) {
        return null;
    }

    @Deprecated
    public Boolean getbit(String key, long offset) {
        return null;
    }

    @Deprecated
    public Long setrange(String key, long offset, String value) {
        return null;
    }

    @Deprecated
    public String getrange(String key, long startOffset, long endOffset) {
        return null;
    }

    @Deprecated
    public String getSet(String key, String value) {
        return null;
    }

    @Deprecated
    public Long setnx(String key, String value) {
        return null;
    }

    @Deprecated
    public Long decrBy(String key, long integer) {
        return null;
    }

    @Deprecated
    public Long decr(String key) {
        return null;
    }

    public long incrBy(String key, long integer) {
        try {
            return cluster.incrBy(key, integer);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> incrBy error", e);
        }
        return -1L;
    }

    @Deprecated
    public Double incrByFloat(String key, double value) {
        return null;
    }

    public long incr(String key) {
        try {
            return cluster.incr(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> incr error", e);
            return -1L;
        }
    }

    @Deprecated
    public Long append(String key, String value) {
        return null;
    }

    @Deprecated
    public String substr(String key, int start, int end) {
        return null;
    }

    public <T extends IHashConvert> long hset(String key, T t) {
        return this.hset(key, t.getKey(), t.getValue());
    }

    public long hset(String key, String field, String value) {
        try {
            return cluster.hset(key, field, value);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hset error", e);
        }
        return -1L;
    }

/*    public <T> String hget(String key, T t) {
        if(t instanceof String) {
            return this.hget(key, String.class.cast(t));
        }
        return this.hget(key, t.toString());
    }*/

    public <K, V> V hget(String key, K field, IHashBuilder<K, V> builder) {
        assertKey(field);
        String string = this.hget(key, field.toString());
        if(string == null) {
            return null;
        }
        return builder.build(field, string);
    }

    public String hget(String key, String field) {
        try {
            return cluster.hget(key, field);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hget error", e);
            return null;
        }
    }

    public int hgetInt(String key, String field) {
        String s = hget(key, field);
        return s == null ? 0 : Integer.parseInt(s);
    }

    @Deprecated
    public Long hsetnx(String key, String field, String value) {
        return null;
    }

    public <T extends IHashConvert> String hmset(String key, List<T> list) {
        Map<String, String> hash = list.stream()
                .collect(Collectors.toMap(IHashConvert::getKey, IHashConvert::getValue));
        return this.hmset(key, hash);
    }

    public String hmset(String key, Map<String, String> hash) {
        try {
            return cluster.hmset(key, hash);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hmset error", e);
            return null;
        }
    }

    public <K, V> List<V> hmget(String key, List<K> fields, IHashBuilder<K, V> builder) {
        List<String> strings = this.hmget(key, toStrings(fields));
        List<V> results = new ArrayList<>(fields.size());
        for(int i = 0; i < fields.size(); i++) {
            String value = strings.get(i);
            if(value == null) {
                continue;
            }
            results.add(builder.build(fields.get(i), value));
        }
        return results;
    }

    public <T> List<String> hmget(String key, List<T> fields) {
        return this.hmget(key, toStrings(fields));
    }

    public List<String> hmget(String key, String... fields) {
        try {
            return cluster.hmget(key, fields);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hmget error", e);
            return new ArrayList<>(0);
        }
    }

    public long hincrBy(String key, String field, long value) {
        try {
            return cluster.hincrBy(key, field, value);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hincrBy error", e);
            return -1L;
        }
    }

    public Boolean hexists(String key, String field) {
        try {
            return cluster.hexists(key, field);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hexists error", e);
            return null;
        }
    }

    public long hdel(String key, String... field) {
        try {
            return cluster.hdel(key, field);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hdel error", e);
            return -1L;
        }
    }

    public long hlen(String key) {
        try {
            return cluster.hlen(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hlen error", e);
            return -1L;
        }
    }

    @Deprecated
    public Set<String> hkeys(String key) {
        return null;
    }

    @Deprecated
    public List<String> hvals(String key) {
        return null;
    }

    public Map<String, String> hgetAll(String key) {
        try {
            return cluster.hgetAll(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> hgetAll error", e);
            return new HashMap<>(0);
        }
    }

    public <V> List<V> hgetAll(String key, IHashBuilder<String, V> builder) {
        Map<String, String> stringMap = this.hgetAll(key);
        return stringMap.entrySet().stream()
                .map(entry -> builder.build(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Long rpush(String key, String... string) {
        try {
            return cluster.rpush(key, string);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> rpush error", e);
            return -1L;
        }
    }

    public Long lpush(String key, String... string) {
        try {
            return cluster.lpush(key, string);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> lpush error", e);
            return -1L;
        }
    }

    public Long llen(String key) {
        try {
            return cluster.llen(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> llen error", e);
            return -1L;
        }
    }

    public List<String> lrange(String key, long start, long end) {
        try {
            return cluster.lrange(key, start, end);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> lrange error", e);
            return new ArrayList<>(0);
        }
    }

    @Deprecated
    public String ltrim(String key, long start, long end) {
        return null;
    }

    public String lindex(String key, long index) {
        try {
            return cluster.lindex(key, index);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> lindex error", e);
            return null;
        }
    }

    public String lset(String key, long index, String value) {
        try {
            return cluster.lset(key, index, value);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> lset error", e);
            return null;
        }
    }

    public Long lrem(String key, long count, String value) {
        try {
            return cluster.lrem(key, count, value);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> lrem error", e);
            return -1L;
        }
    }

    public String lpop(String key) {
        try {
            return cluster.lpop(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> lpop error", e);
            return null;
        }

    }

    public String rpop(String key) {
        try {
            return cluster.rpop(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> rpop error", e);
            return null;
        }
    }

    public Long sadd(String key, String... member) {
        try {
            return cluster.sadd(key, member);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> sadd error", e);
            return -1L;
        }
    }

    public Set<String> smembers(String key) {
        try {
            return cluster.smembers(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> smembers error", e);
            return null;
        }
    }


    public Long srem(String key, String... member) {
        try {
            return cluster.srem(key, member);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> srem error", e);
            return -1L;
        }
    }

    @Deprecated
    public String spop(String key) {
        return null;
    }

    @Deprecated
    public Set<String> spop(String key, long count) {
        return null;
    }


    public Long scard(String key) {
        try {
            return cluster.scard(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> scard error", e);
            return -1L;
        }
    }

    @Deprecated
    public Boolean sismember(String key, String member) {
        return null;
    }


    public String srandmember(String key) {
        try {
            return cluster.srandmember(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> srandmember error", e);
            return null;
        }
    }


    public List<String> srandmember(String key, int count) {
        try {
            return cluster.srandmember(key, count);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> srandmember error", e);
            return new ArrayList<>(0);
        }
    }

    @Deprecated
    public Long strlen(String key) {
        return null;
    }

    public Long zadd(String key, double score, String member) {
        try {
            return cluster.zadd(key, score, member);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> zadd error", e);
            return -1L;
        }
    }

    @Deprecated
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return null;
    }

    @Deprecated
    public Set<String> zrange(String key, long start, long end) {
        return null;
    }

    public Long zrem(String key, String... member) {
        try {
            return cluster.zrem(key, member);
        } catch (JedisException e) {
            error("<<RedisCluster>> zrem error", e);
            return null;
        }
    }

    public Double zincrby(String key, double score, String member) {
        try {
            return cluster.zincrby(key, score, member);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> zincrBy error", e);
            return null;
        }
    }

    public Long zrank(String key, String member) {
        try {
            return cluster.zrank(key, member);
        } catch (JedisException e) {
            this.error("<<RedisCluster>> zrank error", e);
            return -1L;
        }
    }

    public Long zrevrank(String key, String member) {
        try {
            return cluster.zrevrank(key, member);
        } catch (JedisException e) {
            this.error("<<RedisCluster>> zrevrank error", e);
            return -1L;
        }
    }

    @Deprecated
    public Set<String> zrevrange(String key, long start, long end) {
        return null;
    }

    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        try {
            return cluster.zrangeWithScores(key, start, end);
        } catch (JedisException e) {
            this.error("<<RedisCluster>> zrangeWithScores error", e);
            return null;
        }
    }

    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        try {
            return cluster.zrevrangeWithScores(key, start, end);
        } catch (JedisException e) {
            error("<<RedisCluster>> zrevrangeWithScores error", e);
            return new HashSet<>(0);
        }
    }

    public Long zcard(String key) {
        try {
            return cluster.zcard(key);
        } catch (JedisException e) {
            error("<<RedisCluster>> zcard error", e);
            return null;
        }
    }

    public Double zscore(String key, String member) {
        try {
            return cluster.zscore(key, member);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> zscore error", e);
            throw new JedisException(e);
        }
    }

    @Deprecated
    public List<String> sort(String key) {
        return null;
    }

    @Deprecated
    public List<String> sort(String key, SortingParams sortingParameters) {
        return null;
    }

    @Deprecated
    public Long zcount(String key, double min, double max) {
        return null;
    }

    @Deprecated
    public Long zcount(String key, String min, String max) {
        return null;
    }

    @Deprecated
    public Set<String> zrangeByScore(String key, double min, double max) {
        return null;
    }

    @Deprecated
    public Set<String> zrangeByScore(String key, String min, String max) {
        return null;
    }

    @Deprecated
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return null;
    }

    @Deprecated
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return null;
    }

    @Deprecated
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Deprecated
    public Long zremrangeByRank(String key, long start, long end) {
        return null;
    }

    @Deprecated
    public Long zremrangeByScore(String key, double start, double end) {
        return null;
    }

    @Deprecated
    public Long zremrangeByScore(String key, String start, String end) {
        return null;
    }

    @Deprecated
    public Long zlexcount(String key, String min, String max) {
        return null;
    }

    @Deprecated
    public Set<String> zrangeByLex(String key, String min, String max) {
        return null;
    }

    @Deprecated
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return null;
    }

    @Deprecated
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return null;
    }

    @Deprecated
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return null;
    }

    @Deprecated
    public Long zremrangeByLex(String key, String min, String max) {
        return null;
    }

    @Deprecated
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return null;
    }

    @Deprecated
    public Long lpushx(String key, String... string) {
        return null;
    }

    @Deprecated
    public Long rpushx(String key, String... string) {
        return null;
    }

    @Deprecated
    public List<String> blpop(String arg) {
        return null;
    }

    @Deprecated
    public List<String> blpop(int timeout, String key) {
        return null;
    }

    @Deprecated
    public List<String> brpop(String arg) {
        return null;
    }

    @Deprecated
    public List<String> brpop(int timeout, String key) {
        return null;
    }

    public Long del(String key) {
        try {
            return cluster.del(key);
        } catch (JedisException e) {
            ApplicationLocal.instance().error("<<RedisCluster>> del error", e);
            return -1L;
        }
    }

    @Deprecated
    public String echo(String string) {
        return null;
    }

    @Deprecated
    public Long move(String key, int dbIndex) {
        return null;
    }

    @Deprecated
    public Long bitcount(String key) {
        return null;
    }

    @Deprecated
    public Long bitcount(String key, long start, long end) {
        return null;
    }

    @Deprecated
    public ScanResult<Map.Entry<String, String>> hscan(String key, int cursor) {
        return null;
    }

    @Deprecated
    public ScanResult<String> sscan(String key, int cursor) {
        return null;
    }

    @Deprecated
    public ScanResult<Tuple> zscan(String key, int cursor) {
        return null;
    }

    @Deprecated
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return null;
    }

    @Deprecated
    public ScanResult<String> sscan(String key, String cursor) {
        return null;
    }

    @Deprecated
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return null;
    }

    @Deprecated
    public Long pfadd(String key, String... elements) {
        return null;
    }

    @Deprecated
    public long pfcount(String key) {
        return 0;
    }

    private static <T> String[] toStrings(List<T> list) {
        String[] strings = new String[list.size()];
        for(int i = 0; i < list.size(); i++) {
            if(i == 0) {
                assertKey(list.get(i));
            }
            strings[i] = list.get(i).toString();
        }
        return strings;
    }

    private static <T> void assertKey(T key) {
        boolean ok = key instanceof String || key instanceof Number;
        if(!ok) {
            throw new AssertionError();
        }
    }

    private static <T> T convertKey(String key, Class<T> cls) {
        return cls.cast(key);
    }

}
