package flygame.extensions.db;

public interface IParamsBuilder<T> {
	public Object[] buildParams(T t);
}
