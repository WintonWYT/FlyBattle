package flygame.extensions.utils;

public interface ExcelObjectBuilder<T>
{
	public T build(ExcelResultSet ers);
}