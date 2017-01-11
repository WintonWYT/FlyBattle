package flygame.common.db.tablemap;

import flygame.common.db.SqlCounter;
import flygame.extensions.db.DbManager;
import flygame.extensions.db.TableMapOutput;

import java.util.Arrays;

public class PartTableMapper {
	public final PartTableMapStrategy strategy;
	private final boolean enableSqlCounter;

	public PartTableMapper(PartTableMapStrategy strategy) {
		this(strategy, false);
	}

	public PartTableMapper(PartTableMapStrategy strategy, boolean enableSqlCounter) {
		this.strategy = strategy;
		this.enableSqlCounter = enableSqlCounter;
	}
	@Deprecated
	public TableMapOutput map(String table, long id) {
		String mapTable = mapTable(table, id);
		return new TableMapOutput(mapTable, mapDb(id, table));
	}

	public TableMapOutput map(String table, String sql, long id) {
		if (enableSqlCounter) countSql(sql);
		return new TableMapOutput(String.format(sql, mapTableIndex(table, id)), mapDb(id,
				table));
	}

	public TableMapOutput quickMap(long id, String sql) {
		return new TableMapOutput(quickMapSql(sql, id), mapDb(id, sql));
	}

	public TableMapOutput quickMap(long id, String sql, int tableCount) {
		return new TableMapOutput(quickMapSql(sql, id, tableCount), mapDb(id,
				sql));
	}

	public TableMapOutput map(long id, String sql, String... tables) {
		if (enableSqlCounter) countSql(sql);
		Object[] partIndexList = new Object[tables.length];
		for (int i = 0; i < tables.length; i++) {
			partIndexList[i] = mapTableIndex(tables[i], id);
		}
		return new TableMapOutput(String.format(sql, partIndexList), mapDb(id,
				tables[0]));
	}
	@Deprecated
	public String mapTable(String table, long id) {
		return this.strategy.mapTable(id, table);
	}
	@Deprecated
	public String mapSql(String table, String sql, long id) {
		if (enableSqlCounter) countSql(sql);
		return String.format(sql, mapTableIndex(table, id));
	}

	private String quickMapSql(String sql, long id) {
		if (enableSqlCounter) countSql(sql);
		return String.format(sql, "");
	}

	private String quickMapSql(String sql, long id, int tableCount) {
		if (enableSqlCounter) countSql(sql);
		Object[] partIndexList = new Object[tableCount];
		Arrays.fill(partIndexList, "");
		return String.format(sql, partIndexList);
	}

	private String mapTableIndex(String table, long id) {
		return this.strategy.mapTableIndex(id, table);
	}
	@Deprecated
	public TableMapOutput mapWithTableIndex(String table, int tableIndex,
											String sql) {
		if (enableSqlCounter) countSql(sql);
		int tpUnit = this.getTablePartUnit(table);
		int dbIndex = tableIndex * tpUnit / this.strategy.dbPartUnit;

		return new TableMapOutput(String.format(sql, tableIndex), this.strategy
				.getPartitionDb(this.strategy.partDbPrefix, dbIndex));
	}

	public DbManager mapDb(long id) {
		return mapDb(id, null);
	}

	private DbManager mapDb(long id, String table) {
		return this.strategy.mapDb(id, table);
	}

	public int getTableCount(String table) {
		int tpUnit = this.getTablePartUnit(table);
		return this.strategy.dbPartUnit * DbManager.getPartDbCount() / tpUnit;
	}

	private int getTablePartUnit(String table) {
		int tpUnit;
		if (this.strategy.tablePartUnitMap.containsKey(table)) {
			tpUnit = this.strategy.tablePartUnitMap.get(table);
		} else {
			tpUnit = this.strategy.defaultTablePartUnit;
		}
		return tpUnit;
	}

	private void countSql(String sql) {
		try {
			SqlCounter.instance.putSql(sql);
		} catch (Exception e) {
			// 避免countSql报错了，影响sql执行，所以try...catch
		}
	}
}
