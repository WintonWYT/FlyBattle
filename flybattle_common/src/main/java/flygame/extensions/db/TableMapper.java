package flygame.extensions.db;

import flygame.common.db.tablemap.TableMappers;

public class TableMapper {

	// 以前是常量，现在是配置，为了兼容旧代码，不修改常量命名了。
	public static final int DEFAULT_TABLE_PARTITION_UNIT = TableMappers.main.strategy.defaultTablePartUnit;
	public static final int DB_PARTITION_UNIT = TableMappers.main.strategy.dbPartUnit;
/*	private static final int USER_NAME_TABLE_COUNT = 100;
	private static final int IDCARD_TABLE_COUNT = 100;

	// Tables类常量是遗留问题, 该类变量不应出现在common包中 
	//--------------  遗留问题  begin  ----------------------------------------
	@Deprecated
	public static final String Table_UserClothes = Tables.UserClothes;
	@Deprecated
	public static final String Table_UserFurniture = Tables.UserFurniture;
	@Deprecated
	public static final String Table_BuddyList = Tables.BuddyList;
	@Deprecated
	public static final String Table_HouseVisit = Tables.HouseVisit;
	@Deprecated
	public static final String Table_Task = Tables.Task;
	@Deprecated
	public static final String Table_Scores = Tables.Scores;
	@Deprecated
	public static final String Table_UserHouses = Tables.UserHouses;
	@Deprecated
	public static final String Table_BankBasic = Tables.BankBasic;
	@Deprecated
	public static final String Table_BankFixed = Tables.BankFixed;
	@Deprecated
	public static final String Table_BankDealList = Tables.BankDealList;
	@Deprecated
	public static final String Table_Users = Tables.Users;
	@Deprecated
	public static final String Table_UserProfile = Tables.UserProfile;
	//--------------  遗留问题  end  ----------------------------------------

	public static boolean validateUserId(long userId) {
		return (userId > 0)
				&& (userId < DbManager.getPartDbCount() * DB_PARTITION_UNIT);
	}*/

/*
	public static TableMapOutput map(String table, long userId) {
		return TableMappers.main.map(table, userId);
	}
*/

	public static TableMapOutput map(String table, String sql, long userId) {
		return TableMappers.main.map(table, sql, userId);
	}

	public static TableMapOutput quickMap(long userId, String sql) {
		return TableMappers.main.quickMap(userId, sql);
	}

	public static TableMapOutput quickMap(long userId, String sql, int tableCount) {
		return TableMappers.main.quickMap(userId, sql, tableCount);
	}

	public static TableMapOutput map(long userId, String sql, String... tables) {
		return TableMappers.main.map(userId, sql, tables);
	}

/*	public static String mapTable(String table, long userId) {
		return TableMappers.main.mapTable(table, userId);
	}*/

/*
	public static String mapSql(String table, String sql, long userId) {
		return TableMappers.main.mapSql(table, sql, userId);
	}

	public static String quickMapSql(String sql, long userId) {
		return TableMappers.main.quickMapSql(sql, userId);
	}
*/

/*	public static String quickMapSql(String sql, long userId, int tableCount) {
		return TableMappers.main.quickMapSql(sql, userId, tableCount);
	}*/
	@Deprecated
	public static int getTableCount(String table) {
		return TableMappers.main.getTableCount(table);
	}

/*	public static TableMapOutput mapWithTableIndex(String table, int tableIndex,
												   String sql) {
		return TableMappers.main.mapWithTableIndex(table, tableIndex, sql);
	}*/

/*	public static TableMapOutput mapUserName2Id(String sql, String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}

		int tableIndex = getUserName2IdTableIndex(name);
		return new TableMapOutput(String.format(sql, tableIndex), DbManager
				.instance());
	}

	public static int getUserName2IdTableIndex(String name) {
		return Math.abs(name.toLowerCase().hashCode() % USER_NAME_TABLE_COUNT);
	}


	public static TableMapOutput mapIdCard(String sql, String idCard) {
		if (StringUtils.isEmpty(idCard)) {
			return null;
		}

		int tableIndex = getIdCardTableIndex(idCard);
		// 默认如果访问IdCard系列表，必然以uc库为主库
		return new TableMapOutput(String.format(sql, tableIndex), DbManager.getWorkDb());
	}

	public static int getIdCardTableIndex(String idCard){
		return Math.abs(idCard.toLowerCase().hashCode() % IDCARD_TABLE_COUNT);
	}*/

	public static DbManager mapDb(long userId) {
		return TableMappers.main.mapDb(userId);
	}
}
