package flygame.extensions.db;

import flygame.common.ApplicationLocal;

import java.sql.Connection;
import java.sql.SQLException;


public abstract class DbNode {

    public final boolean checkAvailable;
    private final String nodeCheckFailId;
    private volatile boolean failed = false;

    public DbNode(DbNodeConfig nodeConfig, boolean checkAvailable) {
        this.checkAvailable = checkAvailable;
        this.nodeCheckFailId = String.format("%s:%s", nodeConfig.ip, nodeConfig.dbName);
    }

    // 这个检查的准确性非常非常重要！！
    public final void checkAvailable() {
        if (!checkAvailable) {
            return;
        }

        if (!this.getMonitorResult(this.nodeCheckFailId)) {
            this.failed = true;
            String log = String.format("database[%s] failed!!", nodeCheckFailId);
            ApplicationLocal.instance().error(log);
        }
    }

    public final boolean isFailed() {
        return this.failed;
    }

    private boolean getMonitorResult(String nodeId) {
        // TODO : 去监控中心拿结果
        return true;
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void destroy() throws SQLException;

}
