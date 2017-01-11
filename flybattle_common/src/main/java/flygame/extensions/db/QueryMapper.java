package flygame.extensions.db;

import java.util.List;

public interface QueryMapper {
	List<QueryCmd> map(String cmd, Object... params);
}
