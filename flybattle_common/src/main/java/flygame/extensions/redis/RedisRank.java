package flygame.extensions.redis;


public class RedisRank {

    public final int rank;
    public final String member;
    public final int score;

    public RedisRank(int rank, String member, int score) {
        this.rank = rank;
        this.member = member;
        this.score = score;
    }
}
