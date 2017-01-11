package flygame.common.db;

public interface IdNameMarker<T> {
	int getId(T o);

	void setName(T o, String name);
}
