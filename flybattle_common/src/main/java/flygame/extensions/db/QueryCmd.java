package flygame.extensions.db;

import javax.sql.rowset.CachedRowSet;

public class QueryCmd {
	private DbManager db;
	private String cmd;
	private Object[] params;

	public QueryCmd(DbManager db, String cmd, final Object[] params) {
		this.db = db;
		this.cmd = cmd;
		this.params = params;
	}

	public CachedRowSet query() {
		// System.out.println("QueryCmd:" + cmd);
		return this.db.executeQuery_RowSet(cmd, params);
	}
}
