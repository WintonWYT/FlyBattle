package flygame.common.config;

import flygame.common.ApplicationLocal;
import flygame.extensions.db.DbGroupConfig;
import flygame.extensions.db.DbManager;
import flygame.extensions.db.DbNodeConfig;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;

public class DbConfigReader implements ConfigReader{
	private String appName;
	private String configDbConnectionString = "";
	private String driver = "";
	private boolean usePlainText = false;
	private final static int W = 10000;

	private final List<String> prefixDbNames = new LinkedList<String>();

	public DbConfigReader(String appName, String configDbConnectionString, String driver, boolean usePlainText){
		this.appName = appName;
		this.configDbConnectionString = configDbConnectionString;
		this.driver = driver;
		this.usePlainText = usePlainText;
		prefixDbNames.add("part");
	}

	@Override
	public void setPrefixDbNames(String[] names) {
		for(String name : names) {
			prefixDbNames.add(name);
		}
	}

	public Map<String, DbGroupConfig> loadDbConfig() {
		Map<String, DbGroupConfig> dbs = new HashMap<String, DbGroupConfig>();
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			Class.forName(this.driver).newInstance();
			conn = DriverManager.getConnection(this.configDbConnectionString);
			// 从主库中选出数据库连接池的基本配置
			stt = conn.createStatement();
			stt.executeQuery("SELECT PoolMin, CheckoutTimeout, MaxIdleTime, IdleConnectionTestPeriod, PreferredTestQuery, TestConnectionOnCheckin FROM AS_DbPoolInfo LIMIT 1");
			rs = stt.getResultSet();
			int poolMin = 0;
			int checkoutTimeout = 0;
			int maxIdleTime = 0;
			int idleConnectionTestPeriod = 0;
			String preferredTestQuery = "";
			boolean testConnectionOnCheckin = false;
			if(rs.next()){
				poolMin = rs.getInt("PoolMin");
				checkoutTimeout = rs.getInt("CheckoutTimeout");
				maxIdleTime = rs.getInt("MaxIdleTime");
				idleConnectionTestPeriod = rs.getInt("IdleConnectionTestPeriod");
				preferredTestQuery = rs.getString("PreferredTestQuery");
				testConnectionOnCheckin = rs.getBoolean("TestConnectionOnCheckin");
			}
			// 从主库中取出应用对应的数据库资源名称
			String dbSource = getDbSourceFromASConfigDb(conn);
			// 循环选出对应数据库资源信息
			String[] dbNameInfos = dbSource.split("\\|");
			for(String dbNameInfo : dbNameInfos){
				DbInfoDetail did = new DbInfoDetail(dbNameInfo.trim());
				stt = conn.createStatement();
				stt.executeQuery(did.getQueryString(this.prefixDbNames));
				rs = stt.getResultSet();
				while(rs.next()){
					String name = rs.getString("Name");
					String dbName = rs.getString("DbName");
					String IPs = rs.getString("IPs");
					String[] iPDetail = IPs.split("/");

					// 根据权限级别选出的帐户信息
					String account = rs.getString("account");
					// 如果使用的是密文,则进行解密
					if(!this.usePlainText){
						account = SecurityHelper.Instance().decrypt(account);
					}
					String[] tmp = account.split("/");
					String user = tmp[0].trim();
					String pwd = tmp[1].trim();

					// 仅有查询权限的帐户信息
					String queryAccount = rs.getString("QueryAccount");
					// 如果使用的是密文,则进行解密
					if(!usePlainText){
						queryAccount = SecurityHelper.Instance().decrypt(queryAccount);
					}
					tmp = queryAccount.split("/");
					String queryUser = tmp[0].trim();
					String queryPwd = tmp[1].trim();

					ArrayList<DbNodeConfig> connList = new ArrayList<DbNodeConfig>();

					// 如果不需要slave,则只加载主库信息
					int size = did.needSlave? iPDetail.length : 1;
					for (int i = 0 ; i < size ; i++) {
						DbNodeConfig connInfo = new DbNodeConfig();
						// 构造连接字符串,如果i > 0, 则使用仅有查询权限的帐户
						boolean queryOnly = (i != 0);
						connInfo.ip = iPDetail[i].trim();
						connInfo.dbName = dbName;
						connInfo.user = queryOnly ? queryUser : user;
						connInfo.password = queryOnly ? queryPwd : pwd;
						connInfo.driver = this.driver;
						connInfo.poolMin = poolMin;
						connInfo.poolMax = did.dbPoolMax;
						connInfo.checkoutTimeout = checkoutTimeout;
						connInfo.idleConnectionTestPeriod = idleConnectionTestPeriod;
						connInfo.maxIdleTime = maxIdleTime;
						connInfo.preferredTestQuery = preferredTestQuery;
						connInfo.testConnectionOnCheckin = testConnectionOnCheckin;
						connList.add(connInfo);
					}
					//TODO : checkAvailable from database
					dbs.put(name, new DbGroupConfig(did.startRound, connList, false));
				}
				release(stt, rs, null);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			ApplicationLocal.instance().error("init db config err", t);
		}finally{
			release(stt, rs, conn);
		}
		return dbs;
	}

	// 从主库中取出应用对应的数据库资源名称
	private String getDbSourceFromASConfigDb(Connection conn) throws SQLException{
		Statement stt = null;
		ResultSet rs = null;
		stt = conn.createStatement();
		rs = stt.executeQuery(String.format("SELECT DbSource FROM AS_AppSourceMap WHERE AppName = '%s'", this.appName));
		String dbSource = "";
		if(rs.next()){
			dbSource = rs.getString("DbSource");
		}
		release(stt, rs, null);
		return dbSource;
	}


	// 从主库中加载相应的mc资源信息
	/*public Map<String, String> loadMcConfig() {
		Map<String, String> mcs = new HashMap<String, String>();
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(this.configDbConnectionString);
			stt = conn.createStatement();
			// 从主库中取出应用对应的mc资源名称 
			rs = stt.executeQuery(String.format("SELECT McSource FROM AS_AppSourceMap WHERE AppName = '%s'", this.appName));
			String mcSource = "";
			if(rs.next()){
				mcSource = rs.getString("McSource");
			}
			release(stt, rs, null);

			// 构造mc资源名称字符串
			if(!StringUtils.isEmpty(mcSource)){
				mcSource = "'" + mcSource.replaceAll(",", "','") + "'";
				stt = conn.createStatement();
				// 按mc资源名称从主库中选出所有资源详细ip:port
				rs = stt.executeQuery(String.format("SELECT McName,Servers, Slave FROM AS_McSource WHERE McName IN (%s);", mcSource));
				while(rs.next()){
					String servers = rs.getString("Servers");
					String slave = rs.getString("Slave");
					if (StringUtils.isNotEmpty(slave)) {
						servers = servers + MemcachedNode.SUB_NODE_SP + slave;
					}
					mcs.put(rs.getString("McName"), servers);
				}
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("init db config err", e);
		}finally{
			release(stt, rs, conn);
		}
		return mcs;
	}*/

	// 从主库中加载相应的tt资源信息
	/*public Map<String, String> loadTtConfig() {
		Map<String, String> tts = new HashMap<String, String>();
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(this.configDbConnectionString);
			stt = conn.createStatement();

			// 从主库中取出应用对应的tt资源名称 
			rs = stt.executeQuery(String.format("SELECT TtSource FROM AS_AppSourceMap WHERE AppName = '%s'", this.appName));
			String ttSource = "";
			if(rs.next()){
				ttSource = rs.getString("TtSource");
			}
			release(stt, rs, null);

			// 构造tt资源名称字符串
			if(!StringUtils.isEmpty(ttSource)){
				stt = conn.createStatement();
				if (ttSource.equalsIgnoreCase("all")) {
					rs = stt.executeQuery("SELECT TtName,Servers FROM AS_TtSource;");
				}
				else {
					ttSource = "'" + ttSource.replaceAll(",", "','") + "'";

					// 按tt资源名称从主库中选出所有资源详细ip:port
					rs = stt.executeQuery(String.format("SELECT TtName,Servers FROM AS_TtSource WHERE TtName IN (%s);", ttSource));
				}
				while(rs.next()){
					tts.put(rs.getString("TtName"), rs.getString("Servers"));
				}
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("init db config err", e);
		}finally{
			release(stt, rs, conn);
		}
		return tts;
	}*/

	// 释放Statement和ResultSet
	private static void release(Statement stt , ResultSet rs, Connection conn){
		if(stt != null){
			try {
				stt.close();
			} catch (SQLException e) {
				ApplicationLocal.instance().error("init db config err", e);
			}
		}

		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				ApplicationLocal.instance().error("init db config err", e);
			}
		}

		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				ApplicationLocal.instance().error("init db config err", e);
			}
		}
	}

	public void setConfigElement(Object configElement) {

	}

	// 供baitian_common内部读取数据库配置
	// 最初的设计是baitian_common与配置读取无关，都通过ApplicationLocal与使用者交换配置信息
	// 使用者通过baitian_common提供的公用配置包读取配置，交给ApplicationLocal，很饶吧？
	// 为什么baitian_common不直接读取配置给自己使用呢？
	// 早期，不同项目使用baitian_common，配置形式不同，有xml, java property和数据库配置
	// 因为要兼容多种配置形式，所以提供多种配置形式的读取类，交给使用者读取之后（只有使用者知道配置形式）
	// 再通过applicationLocal给baitian_common使用。
	// 现在，配置形式基本固定，就是数据库，所以可以简化，让baintian_common直接去数据库读取配置，不用再
	// 饶了！这个前提是数据库相关的配置（链接字符串）已经初始化。
	// TODO 需求先把代码兼容，当所有项目都移库配置再作处理
	public static Map<String, String> loadCustomConfig() {
		DbManager dbm = DbManager.getASConfigDb();
		if(dbm == null) {
			dbm = DbManager.instance();
		}
		return loadCustomConfig(dbm.getConnection());
	}

	/** 将AS_UrlSource中的键值当做otherConfig来使用, AS_UrlSource的名字是历史原因 */
	public Map<String, String> loadOtherConfig() {
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(this.configDbConnectionString);
			return loadCustomConfig(conn);
		} catch (Exception e) {
			ApplicationLocal.instance().error("init db config err", e);
			return null;
		}
	}

	private static Map<String, String> loadCustomConfig(Connection conn) {
		Map<String, String> otherConfigMap = new HashMap<String, String>();
		Statement stt = null;
		ResultSet rs = null;
		try {
			stt = conn.createStatement();
			// 从主库中取出应用对应的mc资源名称 
			rs = stt.executeQuery("SELECT Name, URL FROM `AS_UrlSource`");
			while(rs.next()){
				String key = rs.getString("Name");
				String value = rs.getString("URL");
				otherConfigMap.put(key, value);
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("init db config err", e);
		}finally{
			release(stt, rs, conn);
		}
		return otherConfigMap;

	}

	/** 分表粒度由数据库PartUnit字段 * 100000得到 */
	@Override
	public Map<String, Integer> loadTablePartUnit() {
		Map<String, Integer> tablePartUnitMap = new HashMap<String, Integer>();
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(this.configDbConnectionString);
			stt = conn.createStatement();
			rs = stt.executeQuery("SELECT `Table`, PartUnitW FROM `AS_TablePartUnit`;");
			while(rs.next()){
				String key = rs.getString("Table");
				int partUnit = Integer.parseInt(rs.getString("PartUnitW"));
				tablePartUnitMap.put(key, partUnit * W);
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("init db config err", e);
		}finally{
			release(stt, rs, conn);
		}
		return tablePartUnitMap;
	}

	@Override
	public Map<String, String> loadRedisConfig() {
		Map<String, String> mcs = new HashMap<String, String>();
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(this.configDbConnectionString);
			stt = conn.createStatement();
			// 从主库中取出应用对应的reids资源名称 
			rs = stt.executeQuery(String.format("SELECT RedisSource FROM AS_AppSourceMap WHERE AppName = '%s'", this.appName));
			String redisSource = "";
			if(rs.next()){
				redisSource = rs.getString("RedisSource");
			}
			release(stt, rs, null);

			// 构造redis资源名称字符串
			if(!StringUtils.isEmpty(redisSource)){
				redisSource = "'" + redisSource.replaceAll(",", "','") + "'";
				stt = conn.createStatement();
				// 按redis资源名称从主库中选出所有资源详细ip:port
				rs = stt.executeQuery(String.format("SELECT RedisName,Servers FROM AS_RedisSource WHERE RedisName IN (%s);", redisSource));
				while(rs.next()){
					mcs.put(rs.getString("RedisName"), rs.getString("Servers"));
				}
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("init reids config err", e);
		}finally{
			release(stt, rs, conn);
		}
		return mcs;
	}

	@Override
	public String loadUcConfig() {
		String UcSource = null;
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(this.configDbConnectionString);
			stt = conn.createStatement();
			rs = stt.executeQuery(String.format("SELECT UcSource FROM AS_AppSourceMap WHERE AppName = '%s'", this.appName));
			if(rs.next()){
				UcSource = rs.getString("UcSource");
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("init uc config err", e);
		}finally{
			release(stt, rs, conn);
		}
		return UcSource;
	}

	@Override
	public Map<Integer, int[]> loadZoneDbIndexs() {
		Map<Integer, int[]> zoneDbIndexs = new HashMap<>();
		Connection conn = null;
		Statement stt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stt = conn.createStatement();
			rs = stt.executeQuery("SELECT Id, DbIndexs FROM AS_Zone");
			while(rs.next()) {
				int zoneId = rs.getInt("Id");
				String[] dbIndexses = rs.getString("DbIndexs").split(",");
				int[] indexs = new int[dbIndexses.length];
				for(int i = 0; i < indexs.length; i++) {
					indexs[i] = Integer.parseInt(dbIndexses[i]);
				}
				zoneDbIndexs.put(zoneId, indexs);
			}
		} catch (Exception e) {
			ApplicationLocal.instance().error("loadZoneDbIndexs error", e);
		} finally {
			release(stt, rs, conn);
		}
		return zoneDbIndexs;
	}

	private Connection getConnection() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
		Class.forName(driver).newInstance();
		return DriverManager.getConnection(this.configDbConnectionString);
	}

	@Override
	public String loadDbConnectionString() {
		return configDbConnectionString;
	}
}
