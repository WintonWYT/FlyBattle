package flygame.extensions.db;

import flygame.common.ApplicationLocal;
import flygame.common.config.ConfReaderFactory;

import java.util.*;
import java.util.Map.Entry;

// 进程用到的所有数据库集合。
//                    DbGroupList
//                 /         |         \
//		DbManager   DbManager   DbManager(DbGroup)
//     /         \                         /        \
//  DbNode    DbNode         DbNode   DbNode

// DbManager是DbGroup的wrapper
// 1. DbManager的命名是历史原因。
// 2. DbGroup由于reload的缘故，确实需要一个wrapper。
class DbGroupList {
	private final static String PARTITION_DB_PREFIX = "part";

	private DbManager workDb;
	private DbManager logDb;
	private DbManager asConfigDb;
	// 数据库名和Db的映射
	// workDb, logDb也在映射中，但由于用的比较频繁，所以单独定义了以上两个变量，取的时候
	// 就不用走HashMap的流程了
	private HashMap<String, DbManager> nameDbs = new HashMap<String, DbManager>();
	// part库的映射
	// 因为part库都是用part db index获得，而且index是连续整数，所以单独做个数组存放，方便查找
	// 为了reload和close all方便part库也在nameDbs中存在
	private ArrayList<DbManager> partDbs = new ArrayList<DbManager>();
	// part数量，供job遍历所有数据库用
	private int partDbCount = 0;
	private int[] zoneDbIndexs;
	private int[] allZoneDbIndexs;

	void init() {
		Map<String, DbGroupConfig> dbConfig = loadConfig();
		if (dbConfig == null) {
			return;
		}

		int zoneId = ApplicationLocal.instance().getZoneId();
		Map<Integer, int[]> zoneDbIndexMap = ConfReaderFactory.getConfigReader().loadZoneDbIndexs();
		this.zoneDbIndexs = zoneDbIndexMap.get(zoneId);
		if(this.zoneDbIndexs == null) {
			this.allZoneDbIndexs = getAllZoneDbIndexs(zoneDbIndexMap.values());
		}

		for (Entry<String, DbGroupConfig> en : dbConfig.entrySet()) {
			String dbName = en.getKey();
			if (isPartDb(dbName) && this.zoneDbIndexs != null) {
				boolean isZonePartDb = false;
				for (Integer zoneDbIndex : this.zoneDbIndexs) {
					if (dbName.equals(createPartDbName(zoneDbIndex))) {
						isZonePartDb = true;
						break;
					}
				}
				// 只加入本zone的partDb
				if (!isZonePartDb) {
					continue;
				}
			}

			DbGroupConfig groupInfo = en.getValue();
			if (groupInfo != null) {
				addDb(dbName, groupInfo);
			}
		}

		workDb = nameDbs.get("work");
		logDb = nameDbs.get("log");
		asConfigDb = nameDbs.get("as");

		// 为了用part db index get，所以排序。
		sortPartDbByIndex();

//		if (ApplicationLocal.instance().getZoneId() < 0 && !checkPartDbConfig()) {
//			ApplicationLocal.instance().error("part db 配置index不连续！");
//			throw new DbException("part db 配置index不连续！");
//		}
	}

	private String createPartDbName(int dbIndex) {
		return String.format("%s%s", PARTITION_DB_PREFIX, dbIndex);
	}

	/***当前应用连接的数据库
	 * 依赖于zoneId的配置
	 * 如zoneId为0在AS_Zone中不存在，则返回allZoneDbIndexs, 表示应用可以连接所有db，适用于WEB和job
	 * gameserver配置zoneId连接需要的db即可
	 * */
	public int[] getZoneDbIndexs() {
		if(zoneDbIndexs != null) {
			return zoneDbIndexs;
		}
		return allZoneDbIndexs;
	}

	private int[] getAllZoneDbIndexs(Collection<int[]> values) {
		List<Integer> dbIndexs = new ArrayList<>();
		for(int[] indexs: values) {
			for(int index: indexs) {
				dbIndexs.add(index);
			}
		}
		Collections.sort(dbIndexs);
		int[] indexs = new int[dbIndexs.size()];
		for(int i = 0; i < dbIndexs.size(); i++) {
			indexs[i] = dbIndexs.get(i);
		}
		return indexs;
	}

	public DbManager getWorkDb() {
		return workDb;
	}

	public DbManager getLogDb() {
		return logDb;
	}

	public DbManager getASConfigDb() {
		return asConfigDb;
	}

	public DbManager getDb(String name) {
		return nameDbs.get(name);
	}

	public int getPartDbCount() {
		return partDbCount;
	}

	public DbManager getPartitionDb(int partitionIndex) {
		return getDb(createPartDbName(partitionIndex));
	}

	// reload all 不支持part库新增，见reConfigDb(String name)
	public void reConfigDb() {
		Map<String, DbGroupConfig> dbConfig = loadConfig();

		for (DbManager nameDb : nameDbs.values()) {
			nameDb.reload(dbConfig);
		}
	}

	// 仅供DbAvailableChecker用！
	List<DbNode> getAllDbNodes() {
		List<DbManager> groups = new ArrayList<DbManager>(this.nameDbs.values());
		List<DbNode> nodes = new ArrayList<DbNode>();
		for (DbManager db : groups) {
			nodes.addAll(db.dbGroup.allNodes());
		}
		return nodes;
	}

	void reConfigDb(String name) {
		DbManager db = nameDbs.get(name);
		Map<String, DbGroupConfig> dbConfig = loadConfig();
		if (db != null) {
			db.reload(dbConfig);
		} else {
			reConfigPartDb(name, dbConfig);
		}
	}

	private void reConfigPartDb(String name, Map<String, DbGroupConfig> dbConfig) {
		if (!isPartDb(name)) {
			ApplicationLocal.instance().error("can not append non part db : " + name);
			return;
		}

		DbGroupConfig groupInfo = dbConfig.get(name);
		if (groupInfo == null) {
			ApplicationLocal.instance().error("reload no exist database : " + name);
			return;
		}

		int partIndex = getPartDbIndex(name);
		// new part db必须连续。
		if (partIndex != partDbs.size()) {
			ApplicationLocal.instance().error("invalid part db : " + name);
			return;
		}

		addDb(name, groupInfo);
	}

	// nameDbs和partDbs修改线程不安全
	// 考虑到addDb极少用到，所有没有使用读写锁
	private void addDb(String dbName, DbGroupConfig groupInfo) {
		DbManager dbManager = new DbManager(dbName, groupInfo);

		nameDbs.put(dbName, dbManager);

		if (isPartDb(dbName)) {
			partDbs.add(dbManager);
			partDbCount++;
		}
	}

	private static boolean isPartDb(String dbName) {
		return dbName.startsWith(PARTITION_DB_PREFIX);
	}

	private static int getPartDbIndex(String dbName) {
		return Integer.parseInt(dbName.substring(PARTITION_DB_PREFIX.length()));
	}

	private void sortPartDbByIndex() {
		Comparator<DbManager> dbComp = new Comparator<DbManager>() {
			@Override
			public int compare(DbManager db1, DbManager db2) {
				int partIndex1 = getPartDbIndex(db1.getName());
				int partIndex2 = getPartDbIndex(db2.getName());
				return partIndex1 - partIndex2;
			}
		};

		Collections.sort(partDbs, dbComp);
	}

//	private boolean checkPartDbConfig() {
//		for (int i = 0; i < partDbs.size(); i++) {
//			int partDbIndex = getPartDbIndex(partDbs.get(i).getName());
//			if (partDbIndex != i) {
//				return false;
//			}
//		}
//
//		return true;
//	}

	public void destory() {
		for (DbManager nameDb : nameDbs.values()) {
			nameDb.releaseAllConnections();
		}
	}

	private static Map<String, DbGroupConfig> loadConfig() {
		return ConfReaderFactory.getConfigReader().loadDbConfig();
	}

	ArrayList<DbManager> getPartDbs() {
		return partDbs;
	}
}
