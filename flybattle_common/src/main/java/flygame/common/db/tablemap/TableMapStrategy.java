package flygame.common.db.tablemap;

import flygame.extensions.db.DbManager;

public interface TableMapStrategy {
	DbManager mapDb(long id, String table);

	String mapTable(long id, String table);
}
