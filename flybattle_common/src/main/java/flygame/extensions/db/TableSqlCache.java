package flygame.extensions.db;

import java.util.concurrent.ConcurrentHashMap;

public class TableSqlCache {

	private static ConcurrentHashMap<String, String[]> sqlMap;

	static {
		sqlMap = new ConcurrentHashMap<String, String[]>();
	}

	public static String getSql(String sql, String table, int partIndex) {
		String[] sqlList;
		if (!sqlMap.contains(sql)) {
			sqlList = new String[100];
			sqlMap.put(sql, sqlList);
		} else {
			sqlList = sqlMap.get(sql);
		}

		String resultSql;
		if (sqlList[partIndex] != null) {
			resultSql = sqlList[partIndex];
		} else {
			resultSql = String.format(sql, partIndex);
			sqlList[partIndex] = resultSql;
		}

		return resultSql != null ? resultSql : String.format(sql, partIndex);
	}
}
