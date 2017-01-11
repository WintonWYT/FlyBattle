package flygame.common.db.tablemap;

import flygame.extensions.db.DbManager;

import java.util.Map;

@Deprecated
public class DefaultPartTableMapStrategy extends PartTableMapStrategy {

	public DefaultPartTableMapStrategy(String partDbPrefix, int dbPartUnit,
									   Map<String, Integer> tablePartUnitMap, int defaultTablePartUnit) {
		super(partDbPrefix, dbPartUnit, tablePartUnitMap, defaultTablePartUnit);
	}

	// 因为对主系统的part库做了特殊的存储，所以overridegetPartitionDb
	@Override
	public DbManager getPartitionDb(String partDbPrefix, int partIndex) {
		return DbManager.getPartitionDb(partIndex);
	}

	@Override
	public String mapTableIndex(long id, String table) {
		int tpUnit;
		if (tablePartUnitMap.containsKey(table)) {
			tpUnit = tablePartUnitMap.get(table);
			return String.valueOf(id / tpUnit);
		} else {
			return "";
		}
	}
}
