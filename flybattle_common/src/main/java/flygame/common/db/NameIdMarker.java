package flygame.common.db;

public interface NameIdMarker<T> {
	String getName(T o);

	void setId(T o, Integer id);
}
