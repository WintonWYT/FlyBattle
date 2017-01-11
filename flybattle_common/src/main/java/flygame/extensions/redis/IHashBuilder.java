package flygame.extensions.redis;


public interface IHashBuilder<K, V> {
    V build(K key, String value);
}
