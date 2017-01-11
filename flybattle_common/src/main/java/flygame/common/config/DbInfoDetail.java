package flygame.common.config;

import java.util.List;

public class DbInfoDetail {
	public String name;
	public int dbPoolMax;
	public int authorityLevel;
	public boolean needSlave;
	public int startRound;

	public DbInfoDetail(String info) {
		String[] tmp = info.split("\\,");
		this.name = tmp[0];
		this.dbPoolMax = Integer.parseInt(tmp[1]);
		this.authorityLevel = Integer.parseInt(tmp[2]);
		this.needSlave = tmp[3].equals("1");
		this.startRound = Integer.parseInt(tmp[4]);
	}

	public String getQueryString(final List<String> prefixDbNames) {
		if (prefixDbNames.contains(this.name)) {
			// 只指定前缀的数据库，例如part，按like选取
			return String
					.format("SELECT Name, DbName,IPs,AccountLevel%s AS account, AccountLevel1 AS QueryAccount FROM AS_DbSource WHERE Name LIKE '%s%%' ",
							authorityLevel, name);
		} else {
			return String
					.format("SELECT Name,DbName,IPs,AccountLevel%s AS account, AccountLevel1 AS QueryAccount FROM AS_DbSource WHERE Name = '%s'",
							authorityLevel, name);
		}
	}
}
