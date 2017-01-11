package flygame.extensions.db;

import flygame.common.ApplicationLocal;
import flygame.common.db.SqlCounter;
import com.sun.rowset.CachedRowSetImpl;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.*;
import java.util.Date;

// facade and wrapper of DbGroup
// see DbGroup
public class DbManager {

	private final static String GET_LAST_INSERT_ID = "SELECT last_insert_id() AS Id";
	private final static int BATCH_MAX_PARAM_LOG_COUNT = 5;

	private final static int TYPE_INT = 0;
	private final static int TYPE_DATE = 1;
	private final static int TYPE_STRING = 2;
	private final static int TYPE_BOOL = 3;
	private final static int TYPE_LONG = 4;

	private static boolean enableSqlProfile = false;

	final static DbGroupList dbGroupList = new DbGroupList();

	static {
		try {
			enableSqlProfile = ApplicationLocal.instance().enableSqlProfile();
			dbGroupList.init();
			DbAvailableChecker.instance.start();
		} catch (Exception e) {
			logError("init db error:", e);
		}
	}

	public static DbManager instance() {
		return dbGroupList.getWorkDb();
	}

	public static DbManager getWorkDb() {
		return dbGroupList.getWorkDb();
	}

	public static DbManager getLogDb() {
		return dbGroupList.getLogDb();
	}

	public static DbManager getASConfigDb() {
		return dbGroupList.getASConfigDb();
	}

	public static DbManager getDb(String name) {
		return dbGroupList.getDb(name);
	}

	public static int getPartDbCount() {
		return dbGroupList.getPartDbCount();
	}

	public static DbManager getPartitionDb(int partitionIndex) {
		return dbGroupList.getPartitionDb(partitionIndex);
	}

	// reload all 不支持part库新增，见reConfigDb(String name)
	public static void reConfigDb() {
		dbGroupList.reConfigDb();
	}

	public static void reConfigDb(String name) {
		dbGroupList.reConfigDb(name);
	}

	private String name;
	volatile DbGroup dbGroup;
	// 防止在多个区域destory时，重复shutdown
	private boolean isShutDown = false;

	DbManager(String name, DbGroupConfig groupInfo) {
		this.name = name;
		try {
			this.createDbGroup(groupInfo);
		} catch (Exception e) {
			logError("DbManager error() error:", e);
		}
	}

	private void createDbGroup(DbGroupConfig groupInfo) {
		if (groupInfo == null) {
			logError(String.format("Init DbManager error : database %s not found", this.name));
			return;
		}

		this.dbGroup = new DbGroup(this.name, groupInfo);
	}

	void reload(Map<String, DbGroupConfig> dbConfig) {
		if (this.dbGroup != null) {
			this.dbGroup.destroy();
		}

		this.createDbGroup(dbConfig.get(this.name));
	}

	/**
	 * 设置一个标志位，控制下一个(控制范围仅仅到下一个)数据库操作是在主(写)数据库上操作
	 */
	public static void writeOnly() {
		DbGroup.writeOnly();
	}

	public String getName() {
		return this.name;
	}

	/**当前连接到的分库数据库index*/
	public static int[] getZoneDbIndex() {
		return dbGroupList.getZoneDbIndexs();
	}

	public Connection getConnection() {
		return this.dbGroup.getConnection(false);
	}

	public Connection getConnection(boolean readonly) {
		return this.dbGroup.getConnection(readonly);
	}

	public ArrayList<DataRow> executeQuery(String cmd) {
		return executeQuery(cmd, DataRow.DATAROW_TYPE_NAMEANDVALUE);
	}

	// 功能同上一个方法。
	// 不同的是这个方法遇到异常会抛出DbException
	// 不带Ex后缀的方法遇到异常会返回null，不会抛异常。
	// 以下有带Ex和不带的同时出现，都属于这种情况。
	// 如果只有不带Ex的方法（未成对出现），是会抛异常的。例如executeScalarXXX
	// 为什么这么乱？带Ex的方法是后加的，为了纠正以前用返回值表示异常的错误。
	// 保留不带Ex方法是为了兼容旧代码。
	// 建议使用带Ex的方法。
	public ArrayList<DataRow> executeQueryEx(String cmd) {
		return executeQueryEx(cmd, DataRow.DATAROW_TYPE_NAMEANDVALUE);
	}

	public ArrayList<DataRow> executeQuery(String cmd, int dataRowType) {
		return this.executeQuery(cmd, null, dataRowType);
	}

	public ArrayList<DataRow> executeQueryEx(String cmd, int dataRowType) {
		return this.executeQueryEx(cmd, null, dataRowType);
	}

	public ArrayList<DataRow> executeQuery(String cmd, Object[] params) {
		return executeQuery(cmd, params, DataRow.DATAROW_TYPE_NAMEANDVALUE);
	}

	public ArrayList<DataRow> executeQueryEx(String cmd, Object[] params) {
		return executeQueryEx(cmd, params, DataRow.DATAROW_TYPE_NAMEANDVALUE);
	}

	public <T> ArrayList<T> executeQuery_ObjectList(String cmd, ResultObjectBuilder<T> builder) {
		return executeQuery_ObjectList(cmd, null, builder);
	}

	public <T> ArrayList<T> executeQuery_ObjectListEx(String cmd, ResultObjectBuilder<T> builder) {
		return executeQuery_ObjectListEx(cmd, null, builder);
	}

	public Integer executeScalarInt(String cmd) {
		return this.executeScalarInt(cmd, null);
	}

	public Integer executeScalarInt(String cmd, Object[] params) {
		return (Integer) this.executeScalar(cmd, params, TYPE_INT);
	}

	public String executeScalarString(String cmd) {
		return executeScalarString(cmd, null);
	}

	public String executeScalarString(String cmd, Object[] params) {
		return (String) this.executeScalar(cmd, params, TYPE_STRING);
	}

	public Date executeScalarDate(String cmd) {
		return executeScalarDate(cmd, null);
	}

	public Date executeScalarDate(String cmd, Object[] params) {
		return (Date) this.executeScalar(cmd, params, TYPE_DATE);
	}

	public Boolean executeScalarBool(String cmd) {
		return executeScalarBool(cmd, null);
	}

	public Boolean executeScalarBool(String cmd, Object[] params) {
		return (Boolean) this.executeScalar(cmd, params, TYPE_BOOL);
	}

	public Long executeScalarLong(String cmd) {
		return executeScalarLong(cmd, null);
	}

	public Long executeScalarLong(String cmd, Object[] params) {
		return (Long) this.executeScalar(cmd, params, TYPE_LONG);
	}

	public <T> T executeScalarObject(String cmd, ResultObjectBuilder<T> builder) {
		return executeScalarObject(cmd, null, builder);
	}

	public <T> T executeScalarObjectEx(String cmd, ResultObjectBuilder<T> builder) {
		return executeScalarObjectEx(cmd, null, builder);
	}

	public <T> T executeScalarObject(String cmd, Object[] params, ResultObjectBuilder<T> builder) {
		ArrayList<T> list = executeQuery_ObjectList(cmd, params, builder);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	public <T> T executeScalarObjectEx(String cmd, Object[] params, ResultObjectBuilder<T> builder) {
		ArrayList<T> list = executeQuery_ObjectListEx(cmd, params, builder);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	public boolean isRecordExist(String cmd) {
		return this.isRecordExist(cmd, null);
	}

	public boolean isRecordExist(String cmd, Object[] params) {
		boolean result = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection(true);
			stmt = this.prepareStatement(conn, cmd, params);
			rs = stmt.executeQuery();
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {
			logQueryCmdError("isRecordExist", cmd, params, e, true);
		} finally {
			releaseDbResource(rs, stmt, conn);
		}
		return result;
	}

	public int executeCommand(String cmd) {
		return this.executeCommand(cmd, null, null);
	}

	public int executeCommandEx(String cmd) {
		return this.executeCommandEx(cmd, null, null);
	}

	public int executeCommand(String cmd, Connection tranConn) {
		return this.executeCommand(cmd, null, tranConn);
	}

	public int executeCommandEx(String cmd, Connection tranConn) {
		return this.executeCommandEx(cmd, null, tranConn);
	}

	public int executeCommand(String cmd, Object[] params) {
		return this.executeCommand(cmd, params, null);
	}

	public int executeCommandEx(String cmd, Object[] params) {
		return this.executeCommandEx(cmd, params, null);
	}

	private Object executeScalar(String cmd, Object[] params, int type) {
		Object result = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection(true);
			stmt = this.prepareStatement(conn, cmd, params);
			rs = stmt.executeQuery();
			if (rs.next()) {
				switch (type) {
					case TYPE_INT:
						int intVal = rs.getInt(1);
						result = rs.wasNull() ? null : Integer.valueOf(intVal);
						break;
					case TYPE_LONG:
						long longVal = rs.getLong(1);
						result = rs.wasNull() ? null : Long.valueOf(longVal);
						break;
					case TYPE_STRING:
						result = rs.getString(1);
						break;
					case TYPE_DATE:
						Timestamp tsVal = rs.getTimestamp(1);
						result = rs.wasNull() ? null : new Date(tsVal.getTime());
						break;
					case TYPE_BOOL:
						Boolean blVal = rs.getBoolean(1);
						result = rs.wasNull() ? null : blVal;
						break;
				}
			}
		} catch (Exception e) {
			logQueryCmdError("executeScalar", cmd, params, e, true);
		} finally {
			releaseDbResource(rs, stmt, conn);
		}
		return result;
	}

	public ArrayList<DataRow> executeQuery(String cmd, Object[] params, int dataRowType) {
		return this.executeQuery(cmd, params, dataRowType, false);
	}

	public ArrayList<DataRow> executeQueryEx(String cmd, Object[] params, int dataRowType) {
		return this.executeQuery(cmd, params, dataRowType, true);
	}

	private ArrayList<DataRow> executeQuery(String cmd, Object[] params, int dataRowType, boolean throwEx) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<DataRow> dataRowList = null;
		try {
			conn = getConnection(true);
			stmt = this.prepareStatement(conn, cmd, params);
			rs = stmt.executeQuery();
			dataRowList = getDataRowListFromResultSet(rs, dataRowType);
		} catch (Exception e) {
			logQueryCmdError("executeQuery", cmd, params, e, throwEx);
		} finally {
			releaseDbResource(rs, stmt, conn);
		}
		return dataRowList;
	}

	public CachedRowSet executeQuery_RowSet(String cmd, Object[] params) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		CachedRowSet rowSet = null;
		try {
			conn = getConnection(true);
			stmt = this.prepareStatement(conn, cmd, params);
			rs = stmt.executeQuery();
			rowSet = new CachedRowSetImpl();
			rowSet.populate(rs);
		} catch (Exception e) {
			logQueryCmdError("executeQuery_RowSet", cmd, params, e, true);
		} finally {
			releaseDbResource(rs, stmt, conn);
		}
		return rowSet;
	}

	public <T> ArrayList<T> executeQuery_ObjectList(String cmd, Object[] params, ResultObjectBuilder<T> builder) {
		return this.executeQuery_ObjectList(cmd, params, builder, false);
	}

	public <T> ArrayList<T> executeQuery_ObjectListEx(String cmd, Object[] params, ResultObjectBuilder<T> builder) {
		return this.executeQuery_ObjectList(cmd, params, builder, true);
	}

	private <T> ArrayList<T> executeQuery_ObjectList(String cmd, Object[] params, ResultObjectBuilder<T> builder,
													 boolean throwEx) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<T> objectList = null;
		try {
			conn = getConnection(true);
			stmt = this.prepareStatement(conn, cmd, params);
			rs = stmt.executeQuery();
			objectList = getObjectListFromResultSet(rs, builder);
		} catch (Exception e) {
			logQueryCmdError("executeQuery_ObjectList", cmd, params, e, throwEx);
		} finally {
			releaseDbResource(rs, stmt, conn);
		}
		return objectList;
	}

	public int executeCommand(String cmd, Object[] params, Connection tranConn) {
		return this.executeCommand(cmd, params, tranConn, false);
	}

	public int executeCommandEx(String cmd, Object[] params, Connection tranConn) {
		return this.executeCommand(cmd, params, tranConn, true);
	}

	private int executeCommand(String cmd, Object[] params, Connection tranConn, boolean throwEx) {
		Connection conn = null;
		PreparedStatement stmt = null;
		int rowCount = -1;
		try {
			if (tranConn != null) {
				conn = tranConn;
			} else {
				conn = getConnection();
			}

			stmt = this.prepareStatement(conn, cmd, params);
			rowCount = stmt.executeUpdate();
		} catch (Exception e) {
			logQueryCmdError("executeCommand", cmd, params, e, throwEx);
		} finally {
			if (tranConn == null) {
				releaseDbResource(null, stmt, conn);
			} else {
				releaseDbResource(null, stmt, null);
			}
		}
		return rowCount;
	}

	public int getLastInsertId(Connection conn) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(GET_LAST_INSERT_ID);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("Id");
			}
			return -1;
		} catch (Exception er) {
			logError("getLastInsertId", er);
			return -1;
		} finally {
			releaseDbResource(rs, stmt, null);
		}
	}

	public int executeInsertCommand(String cmd, Object[] params) {
		return this.executeInsertCommand(cmd, params, false);
	}

	public int executeInsertCommandEx(String cmd, Object[] params) {
		return this.executeInsertCommand(cmd, params, true);
	}

	private int executeInsertCommand(String cmd, Object[] params, boolean throwEx) {
		Connection conn = null;
		int newId = -1;
		try {
			conn = getConnection();
			int result = this.executeCommand(cmd, params, conn, throwEx);
			if (result > 0) {
				newId = this.getLastInsertId(conn);
			}
		} catch (Exception e) {
			logQueryCmdError("executeInsertCommand", cmd, params, e, throwEx);
		} finally {
			releaseDbResource(null, conn);
		}

		return newId;
	}

	public PreparedStatement prepareStatement(Connection conn, String cmd, Object[] params) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(cmd);
		if (params != null) {
			setParams(stmt, params);
		}

		if (enableSqlProfile) {
			ApplicationLocal.instance().sql(logCmd(cmd, params));
		}

		return stmt;
	}

	public int[] executeBatchCommand(String cmd, Collection<Object[]> paramsList) {
		return this.executeBatchCommand(cmd, paramsList, null);
	}

	public int[] executeBatchCommandEx(String cmd, Collection<Object[]> paramsList) {
		return this.executeBatchCommandEx(cmd, paramsList, null);
	}

	public int[] executeBatchCommand(String cmd, Collection<Object[]> paramsList, Connection tranConn) {
		return this.executeBatchCommand(cmd, paramsList, tranConn, false);
	}

	public int[] executeBatchCommandEx(String cmd, Collection<Object[]> paramsList, Connection tranConn) {
		return this.executeBatchCommand(cmd, paramsList, tranConn, true);
	}

	public <T> int[] executeBatchCommand(String cmd, IParamsBuilder<T> builder, Collection<T> paramsList) {
		return executeBatchCommand(cmd, builder, paramsList, null);
	}

	public <T> int[] executeBatchCommandEx(String cmd, IParamsBuilder<T> builder, Collection<T> paramsList) {
		return executeBatchCommandEx(cmd, builder, paramsList, null);
	}

	public <T> int[] executeBatchCommand(String cmd, IParamsBuilder<T> builder, Collection<T> paramsList,
										 Connection tranConn) {
		return executeBatchCommand(cmd, builder, paramsList, tranConn, false);
	}

	public <T> int[] executeBatchCommandEx(String cmd, IParamsBuilder<T> builder, Collection<T> paramsList,
										   Connection tranConn) {
		return executeBatchCommand(cmd, builder, paramsList, tranConn, true);
	}

	private <T> int[] executeBatchCommand(String cmd, IParamsBuilder<T> builder, Collection<T> valuesList,
										  Connection tranConn, boolean throwEx) {
		Collection<Object[]> paramsList = new ArrayList<Object[]>(valuesList.size());
		for (T t : valuesList) {
			Object[] params = builder.buildParams(t);
			paramsList.add(params);
		}
		return executeBatchCommand(cmd, paramsList, tranConn, throwEx);
	}

	private int[] executeBatchCommand(String cmd, Collection<Object[]> paramsList, Connection tranConn, boolean throwEx) {

		Connection conn = null;
		PreparedStatement stmt = null;
		int[] rowCounts = null;
		try {
			if (tranConn != null) {
				conn = tranConn;
			} else {
				conn = getConnection();
				conn.setAutoCommit(false);
			}

			stmt = this.prepareBatchStatement(conn, cmd, paramsList);
			rowCounts = stmt.executeBatch();
			if (tranConn == null) {
				conn.commit();
			}
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception e1) {
			}
			logBatchCmdError("executeBatchCommand", cmd, paramsList, e, throwEx);
		} finally {
			if (tranConn == null) {
				releaseDbResource(null, stmt, conn);
			} else {
				releaseDbResource(null, stmt, null);
			}
		}
		return rowCounts;
	}

	public PreparedStatement prepareBatchStatement(Connection conn, String cmd, Collection<Object[]> paramsList)
			throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(cmd);
		if (paramsList != null && paramsList.size() != 0) {
			for (Object[] params : paramsList) {
				setParams(stmt, params);
				stmt.addBatch();
			}
		}

		if (enableSqlProfile) {
			if (paramsList != null) {
				ApplicationLocal.instance().sql(logBatchCmd(cmd, paramsList));
			}
		}

		return stmt;
	}

	// 为PreparedStatement设置参数
	private void setParams(PreparedStatement stmt, Object[] params) throws SQLException {
		Object o;
		for (int i = 0; i < params.length; i++) {
			o = params[i];
			if (o instanceof Integer) {
				stmt.setInt(i + 1, (Integer) o);
			} else if (o instanceof Short) {
				stmt.setShort(i + 1, (Short) o);
			} else if (o instanceof Long) {
				stmt.setLong(i + 1, (Long) o);
			} else if (o instanceof String) {
				stmt.setString(i + 1, (String) o);
			} else if (o instanceof Date) {
				stmt.setObject(i + 1, o);
			} else if (o instanceof Boolean) {
				stmt.setBoolean(i + 1, (Boolean) o);
			} else if (o instanceof byte[]) {
				stmt.setBytes(i + 1, (byte[]) o);
			} else if (o instanceof Double) {
				stmt.setDouble(i + 1, (Double) o);
			} else if (o instanceof Float) {
				stmt.setFloat(i + 1, (Float) o);
			} else if (o == null) {
				stmt.setNull(i + 1, java.sql.Types.OTHER);
			} else {
				throw new SQLException("Not allowed dataBase data type");
			}
		}
	}

	private ArrayList<DataRow> getDataRowListFromResultSet(ResultSet rs, int dataRowType) throws SQLException {
		if (rs != null) {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int totalColumn = rsMetaData.getColumnCount();
			ArrayList<DataRow> dataRowList = new ArrayList<DataRow>();
			while (rs.next()) {
				DataRow dr = new DataRow(dataRowType);
				// 新建的DataRow对象与ResultSet对象保持一致，第一列（对应项的下标为零）的对象是空值
				dr.addItem("");
				for (int i = 1; i <= totalColumn; i++) {
					if (dataRowType == DataRow.DATAROW_TYPE_VALUEONLY) {
						dr.addItem(rs.getString(i));
					} else {
						dr.addItem(rsMetaData.getColumnName(i), rs.getString(i));
					}
				}
				dataRowList.add(dr);
			}
			return dataRowList;
		} else {
			throw new SQLException("The ResultSet is null");
		}
	}

	static <T> ArrayList<T> getObjectListFromResultSet(ResultSet rs, ResultObjectBuilder<T> builder) {
		if (rs != null) {
			ArrayList<T> objectList = null;
			try {
				objectList = new ArrayList<T>();
				while (rs.next()) {
					objectList.add(builder.build(rs));
				}
			} catch (SQLException e) {
				logError("getObjectListFromResultSet", e);
			}
			return objectList;
		} else {
			return null;
		}
	}

	public static void releaseDbResource(Statement stmt, Connection conn) {
		releaseDbResource(null, stmt, conn);
	}

	public static void releaseDbResource(ResultSet rs, Statement stmt, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException ex) {
			logError(ex.getMessage(), ex);
		}

		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (SQLException ex) {
			logError(ex.getMessage(), ex);
		}

		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException ex) {
			logError(ex.getMessage(), ex);
		}
	}

	void releaseAllConnections() {
		if (!this.isShutDown) {
			this.dbGroup.destroy();
			this.isShutDown = true;
		}
	}

	public static void destory() {

		DistributedDbQuery.shutdown();

		SqlCounter.instance.stop();

		DbAvailableChecker.instance.stop();

		dbGroupList.destory();
	}

	public static String escapeString(String str) {
		str = str.replace("%", "\\%");
		str = str.replace("_", "\\_");
		return str;
	}

	private static void logError(String msg) {
		ApplicationLocal.instance().error(msg);
	}

	static void logError(String msg, Throwable t) {
		try {
			ApplicationLocal.instance().error(msg, t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void logQueryCmdError(String method, String cmd, Object[] params, Exception e, boolean reThrow) {
		String msg = new StringBuilder("<<DbManager>>-").append(method).append(" error [").append(logCmd(cmd, params))
				.append("]").toString();
		if (reThrow) {
			throw new DbException(msg, e);
		} else {
			logError(msg, e);
		}
	}

	private static String logCmd(String cmd, Object[] params) {
		StringBuilder sb = new StringBuilder();
		sb.append(cmd);
		sb.append("/");
		sb.append(params == null ? "" : Arrays.toString(params));

		return sb.toString();
	}

	private static void logBatchCmdError(String method, String cmd, Collection<Object[]> paramsList, Exception e,
										 boolean reThrow) {
		String msg = new StringBuilder("<<DbManager>>-").append(method).append(" error [").append(
				logBatchCmd(cmd, paramsList)).append("]").toString();
		if (reThrow) {
			throw new DbException(msg, e);
		} else {
			logError(msg, e);
		}
	}

	private static String logBatchCmd(String cmd, Collection<Object[]> paramsList) {
		StringBuilder sb = new StringBuilder();
		sb.append(cmd);
		if (paramsList != null) {
			int count = 0;
			for (Object[] params : paramsList) {
				if (++count > BATCH_MAX_PARAM_LOG_COUNT) {
					break;
				}
				sb.append("/");
				sb.append(params == null ? "" : Arrays.toString(params));
			}
			if (count > BATCH_MAX_PARAM_LOG_COUNT) {
				sb.append("/...");
			}
		}
		return sb.toString();
	}

	/**
	 * 获取本区的分库db
	 */
	public static DbManager getPartDb() {
		if (ApplicationLocal.instance().getZoneId() >= 0) {
			return dbGroupList.getPartDbs().get(0);
		} else {
			return null;
		}
	}

	public static ArrayList<DbManager> getPartDbs() {
		return new ArrayList<DbManager>(dbGroupList.getPartDbs());
	}
}