package flygame.extensions.db;

import flygame.common.ApplicationLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class IdGenerator {
	private final static String SELECT_ALL_ID_INFO = "SELECT Name FROM IDGenInfo";
	// ** SELECT...FOR UPDATE
	private final static String SELECT_ID_INFO = "SELECT Current, Step FROM IDGenInfo WHERE Name = ? FOR UPDATE";
	private final static String UPDATE_ID_INFO = "UPDATE IDGenInfo SET Current = Current + Step WHERE Name = ?";

	private final static String INSERT_SERVER_ID_INFO_PERSIST = "REPLACE INTO IDGenInfoPersist(Server, IdName, Count, Value, PersistDate) VALUES (?, ?, ?, ?, NOW())";
	private final static String SELECT_SERVER_ID_INFO_PERSIST = "SELECT IdName, Count, Value, PersistDate FROM IDGenInfoPersist WHERE Server = ? FOR UPDATE";
	private final static String DELETE_SERVER_ID_INFO_PERSIST = "DELETE FROM IDGenInfoPersist WHERE Server = ?";

	private ConcurrentHashMap<String, IdInfo> counters = new ConcurrentHashMap<String, IdInfo>();
	// persist之后，IdGenerator是不让用的
	private boolean enabled = true;

	private IdGenerator() {
		ArrayList<String> names = DbManager.instance().executeQuery_ObjectList(
				SELECT_ALL_ID_INFO, ScalarResultBuilder.stringResultBuilder);
		for (String name : names) {
			counters.put(name, new IdInfo(name, 0, 0));
		}
	}

	private final static IdGenerator _instance = new IdGenerator();

	public static IdGenerator instance() {
		return _instance;
	}

	private long genNewId(String name) {
		if (!enabled){
			ApplicationLocal.instance().error("invalid operation:can Not gen new id after persist!");
			return -1;
		}

		IdInfo ii = counters.get(name);
		if (ii == null) {
			return -1;
		}

		synchronized (ii) {
			if (ii.count == 0) {
				fillIdInfo(ii, name);
				if (ii.count == 0) {
					// fillIdInfo失败了
					return -1;
				}
			}
			ii.count--;
			ii.val++;
			return ii.val;
		}
	}

	public int next(String name) {
		return (int)genNewId(name);
	}

	public long nextBigId(String name){
		return genNewId(name);
	}

	private void fillIdInfo(IdInfo ii, String name) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbManager.instance().getConnection();
			// select...for update需要setAutoCommit(false)
			conn.setAutoCommit(false);

			stmt = conn.prepareStatement(SELECT_ID_INFO);
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			rs.next();

			ii.val = rs.getLong("Current");
			ii.count = rs.getInt("Step");

			stmt.close();

			stmt = conn.prepareStatement(UPDATE_ID_INFO);
			stmt.setString(1, name);
			stmt.execute();

			conn.commit();
		} catch (Exception ex) {
			try {
				conn.rollback();
			} catch (Exception e) {

			}
			// 如果读取id info错误了，一定要吧IdInfo reset(count=0)
			// 否则next方法无法知道fillInfo是否成功了
			ii.reset();
			ApplicationLocal.instance().error("fill id info error :", ex);
		} finally {
			DbManager.releaseDbResource(rs, stmt, conn);
		}
	}

	public void loadPersistData(String server){
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbManager.instance().getConnection();
			// select...for update需要setAutoCommit(false)
			conn.setAutoCommit(false);

			stmt = conn.prepareStatement(SELECT_SERVER_ID_INFO_PERSIST);
			stmt.setString(1, server);
			rs = stmt.executeQuery();
			ArrayList<IdInfo> idItems = new ArrayList<IdInfo>();
			while (rs.next()){
				idItems.add(new IdInfo(rs.getString("IdName"),
						rs.getLong("Value"),
						rs.getInt("Count")));
			}

			stmt.close();

			stmt = conn.prepareStatement(DELETE_SERVER_ID_INFO_PERSIST);
			stmt.setString(1, server);
			stmt.execute();

			conn.commit();

			for(IdInfo pii : idItems){
				IdInfo ii = counters.get(pii.name);
				if (ii != null){
					ii.count = pii.count;
					ii.val = pii.val;
				}
			}
		} catch (Exception ex) {
			try {
				conn.rollback();
			} catch (Exception e) {

			}

			ApplicationLocal.instance().error("load id persist info error :", ex);
		} finally {
			DbManager.releaseDbResource(rs, stmt, conn);
		}
	}

	public void persistData(String server){
		for(Entry<String, IdInfo> en : this.counters.entrySet()){
			DbManager.instance().executeCommand(INSERT_SERVER_ID_INFO_PERSIST,
					new Object[] { server, en.getValue().name, en.getValue().count,
							en.getValue().val });
		}
		this.enabled = false;
	}

	static class IdInfo {
		public String name;
		public long val;
		public int count;

		public IdInfo(String name, long val, int count) {
			this.name = name;
			this.val = val;
			this.count = count;
		}

		public void reset() {
			this.val = -1;
			this.count = 0;
		}
	}
}
