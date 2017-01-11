package flygame.common.db.tablemap;

import flygame.common.ApplicationLocal;
import flygame.extensions.db.DbManager;

import java.util.Map;

public class PartTableMapStrategy implements TableMapStrategy {

	public final String partDbPrefix;
	public final int dbPartUnit;
	public final int defaultTablePartUnit;
	public final Map<String, Integer> tablePartUnitMap;

	public PartTableMapStrategy(String partDbPrefix, int dbPartUnit,
								Map<String, Integer> tablePartUnitMap) {
		this(partDbPrefix, dbPartUnit, tablePartUnitMap, -1);
	}

	public PartTableMapStrategy(String partDbPrefix, int dbPartUnit,
								Map<String, Integer> tablePartUnitMap, int defaultTablePartUnit) {
		this.partDbPrefix = partDbPrefix;
		this.dbPartUnit = dbPartUnit;
		this.tablePartUnitMap = tablePartUnitMap;
		this.defaultTablePartUnit = defaultTablePartUnit;
	}

	@Override
	public DbManager mapDb(long id, String table) {
		if (id <= 0) {
			String msg = String.format("wrong id:%s call table:%s", id, table);
			ApplicationLocal.instance().error(msg);
			return null;
		}
		int partDbIndex = (int) (id / this.dbPartUnit);
		return getPartitionDb(partDbPrefix, partDbIndex);
	}

	protected DbManager getPartitionDb(String partDbPrefix, int partIndex) {
		return DbManager.getPartitionDb(partIndex);
	}

	public String mapTableIndex(long id, String table) {
		int tpUnit;
		if (tablePartUnitMap.containsKey(table)) {
			tpUnit = tablePartUnitMap.get(table);
			long partIndex = id % dbPartUnit / tpUnit;
			return String.valueOf(partIndex);
		} else {	//没有配置表示没有分表
			return "";
		}
	}

	@Override
	public String mapTable(long id, String table) {
		return table + mapTableIndex(id, table);
	}
}
