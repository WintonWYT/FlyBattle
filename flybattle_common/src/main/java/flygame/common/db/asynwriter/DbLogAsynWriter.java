package flygame.common.db.asynwriter;

import java.util.List;

public interface DbLogAsynWriter<T> {
	public void flush(List<T> msgList);
}
