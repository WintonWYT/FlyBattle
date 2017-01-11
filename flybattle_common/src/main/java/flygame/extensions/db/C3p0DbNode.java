package flygame.extensions.db;

import flygame.common.ApplicationLocal;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import java.sql.Connection;
import java.sql.SQLException;

//一个数据库实例
class C3p0DbNode extends DbNode {
	private final ComboPooledDataSource pool;

	public C3p0DbNode(DbNodeConfig nodeConfig, boolean checkAvailable) {
		super(nodeConfig, checkAvailable);
		this.pool = createConnectionPool(nodeConfig);
	}

	private static ComboPooledDataSource createConnectionPool(DbNodeConfig nodeInfo) {
		ComboPooledDataSource ds = new ComboPooledDataSource();

		try {
			ds.setDriverClass(nodeInfo.driver);
		} catch (Exception ex) {
			String errorMsg = "Invalid jdbc driver class : " + nodeInfo.driver;
			DbManager.logError(errorMsg, ex);
			throw new DbException(errorMsg);
		}

		String connStr = String.format("jdbc:mysql://%s/%s?user=%s&password=%s&autoReconnect=true", nodeInfo.ip,
				nodeInfo.dbName, nodeInfo.user, nodeInfo.password);
		if (ApplicationLocal.instance().batchSqlConfigOptimize()) {
			// 有sql不能带;的副作用，所以只能选择性使用
			connStr += "&useServerPreparedStmts=false&rewriteBatchedStatements=true";
		}

		ds.setJdbcUrl(connStr);
		ds.setMinPoolSize(nodeInfo.poolMin);
		ds.setMaxPoolSize(nodeInfo.poolMax);
		ds.setCheckoutTimeout(nodeInfo.checkoutTimeout);
		ds.setIdleConnectionTestPeriod(nodeInfo.idleConnectionTestPeriod);
		ds.setMaxIdleTime(nodeInfo.maxIdleTime);
		ds.setPreferredTestQuery(nodeInfo.preferredTestQuery);
		ds.setTestConnectionOnCheckin(nodeInfo.testConnectionOnCheckin);

		return ds;
	}

	public Connection getConnection() throws SQLException {
		// 如果数据库挂了，从链接池getConnection会timeout，导致线程阻塞
		// 所以直接返回null。
		return this.isFailed() ? null : this.pool.getConnection();
	}

	public void destroy() throws SQLException {
		DataSources.destroy(pool);
	}

}
