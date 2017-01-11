package flygame.extensions.redis;

import flygame.common.ApplicationLocal;
import flygame.extensions.utils.DateUtil;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * 采用Redis SortedSet实现的实时排行榜
 * redis的sortedSet支持的double类型，其中只有后52位是整数型，所以有如下设计：
 * 1. 前面24位用于表示真实score值,后面28存时间差值(秒)
 * 2. 24位score最大值支持1600W
 * 3. 28位整数值只能表示8年时间长
 */
public class RedisRankService {

    private static final int MAX_SCORE = 1 << 24;
    private static final int BIT = 28;
    private static final int YEAR = 8;
    private static long[] TIME_RANGE;

    static {
        Calendar c = Calendar.getInstance();
        c.setTime(DateUtil.START_DATE);
        long start = c.getTimeInMillis();
        c.add(Calendar.YEAR, YEAR);
        long end = c.getTimeInMillis();
        TIME_RANGE = new long[] {start, end};
    }

    private RedisCluster cluster = RedisCluster.getRedisCluster(RedisClusterName.CACHE);

    private static final RedisRankService instance = new RedisRankService();

    private RedisRankService() {}

    public static RedisRankService instance() {
        return instance;
    }

    /**
     *
     * @param key
     * @param member
     * @param incr
     * @param early true越早上榜排名越高 false反之
     * @return
     */
    public int increase(String key, String member, int incr, boolean early) {
        String rankKey = buildRankKey(key);
        Double zscore = cluster.zscore(rankKey, member);
        if(zscore == null) {
            zscore = 0D;
        }
        int origin = decomposeScore(zscore.longValue());
        int score = origin + incr;
        long newIncr = composeScore(score, early) - zscore.longValue();
        cluster.zincrby(rankKey, newIncr, member);
        return score;
    }

    public int increase(String key, String member, int incr) {
        return this.increase(key, member, incr, true);
    }

    /***
     *
     * @param key key
     * @param member member
     * @return 排名从1开始
     */
    public int getRank(String key, String member) {
        Long zrank = cluster.zrank(buildRankKey(key), member);
        if(zrank == null) {
            return 0;//不在排名中
        }
        return zrank.intValue() + 1;
    }

    /**
     *
     * @param key
     * @param member
     * @return 分数值
     */
    public int getScore(String key, String member) {
        Double zscore = cluster.zscore(buildRankKey(key), member);
        return zscore == null? 0: decomposeScore(zscore.longValue());
    }

    /***
     *
     * @param key
     * @param fromRank 从1开始, include
     * @param toRank include
     * @return
     */
    public List<RedisRank> getRankList(String key, final int fromRank, final int toRank) {
        int start = fromRank - 1;
        int end = toRank - 1;
        Set<Tuple> tuples = cluster.zrevrangeWithScores(buildRankKey(key), start, end);
        List<RedisRank> rankList = new ArrayList<>(tuples.size());
        int i = 0;
        for(Tuple tuple: tuples) {
            rankList.add(buildRedisRank(tuple, fromRank + i));
            i += 1;
        }
        return rankList;
    }

    private RedisRank buildRedisRank(Tuple tuple, int rank) {
        return new RedisRank(rank, tuple.getElement(), decomposeScore((long) tuple.getScore()));
    }

    private int decomposeScore(long score) {
        return (int) (score >> BIT);
    }

    private long composeScore(long score, boolean early) {
        if(score >= MAX_SCORE) {
            throw new UnsupportedOperationException("<<RedisRankService>> composeScore overflow");
        }
        return (score << BIT) + getOffsetSeconds(early);
    }

    private static int getOffsetSeconds(boolean early) {
        long offset;
        if(early) {
            offset = TIME_RANGE[1] - System.currentTimeMillis();
        } else {
            offset = System.currentTimeMillis() - TIME_RANGE[0];
        }
        return (int) (offset / 1000);
    }

    private static String buildRankKey(String key) {
        return String.format("r:%d:%s", ApplicationLocal.instance().getZoneId(), key);
    }

}
