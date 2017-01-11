package flygame.extensions.db;

import com.alibaba.druid.pool.DruidDataSource;
import flygame.common.ApplicationLocal;

import java.sql.Connection;
import java.sql.SQLException;


public class DruidDbNode extends DbNode {

    private DruidDataSource pool;

    public DruidDbNode(DbNodeConfig nodeConfig, boolean checkAvailable) {
        super(nodeConfig, checkAvailable);
        this.pool = createConnectionPool(nodeConfig);
    }

    private static DruidDataSource createConnectionPool(DbNodeConfig nodeInfo) {
        DruidDataSource ds = new DruidDataSource();

        try {
            ds.setDriverClassName(nodeInfo.driver);
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

        ds.setUrl(connStr);
        //初始化大小
        ds.setInitialSize(nodeInfo.poolMin);
        //最小
        ds.setMinIdle(nodeInfo.poolMin);
        //最大
        ds.setMaxActive(nodeInfo.poolMax);
        //获取连接等待超时的时间
        ds.setMaxWait(nodeInfo.checkoutTimeout);

        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);

        //间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        ds.setTimeBetweenEvictionRunsMillis(60000);
        //一个连接在池中最小生存的时间，单位是毫秒
        ds.setMinEvictableIdleTimeMillis(300000);
        //如果用Oracle，则把poolPreparedStatements配置为true，mysql可以配置为false。分库分表较多的数据库，建议配置为false。
        ds.setPoolPreparedStatements(false);
        ds.setValidationQuery("SELECT 1");
        return ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.isFailed()? null: pool.getConnection();
    }

    @Override
    public void destroy() throws SQLException {
        pool.close();
    }

}
