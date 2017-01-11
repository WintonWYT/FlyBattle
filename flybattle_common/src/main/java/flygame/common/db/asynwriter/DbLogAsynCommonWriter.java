package flygame.common.db.asynwriter;

import flygame.common.ApplicationLocal;
import flygame.extensions.db.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class DbLogAsynCommonWriter implements DbLogAsynWriter<Object[]> {
	private final String logSql;
	private final DbManager dbm;

	public DbLogAsynCommonWriter(String logSql) {
		this(logSql,DbManager.getLogDb());
	}
	public DbLogAsynCommonWriter(String logSql, DbManager logDbm){
		this.logSql = logSql;
		this.dbm = logDbm;
	}

	public void flush(List<Object[]> msgList) {
		if(msgList == null || msgList.size() == 0){
			return;
		}
		PreparedStatement psmt = null;
		Connection conn = null;
		try{
			conn = dbm.getConnection();
			psmt = conn.prepareStatement(this.logSql);
			for(Object[] objs:msgList){
				this.addParams(psmt, objs);
				psmt.addBatch();
			}
			psmt.executeBatch();
		}catch(Throwable t){
			ApplicationLocal.instance().error(
					"DbLogAsynCommonWriter Error!"+this.logSql, t);
		}finally{
			DbManager.releaseDbResource(psmt, conn);
		}
	}
	//可以考虑将DbManager的setParams方法公开, 不过感觉不大好...
	private void addParams(PreparedStatement stmt, Object[] params)
			throws SQLException {
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
				stmt.setBoolean(i + 1, (Boolean)o);
			} else if (o instanceof byte[]) {
				stmt.setBytes(i + 1, (byte[])o);
			} else if(o == null){
				stmt.setNull(i + 1, java.sql.Types.OTHER);
			} else {
				throw new SQLException("Not allowed dataBase data type");
			}
		}
	}
}
