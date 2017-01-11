package flygame.extensions.db;

public class DbNodeConfig {
	public String driver;
	public String ip;
	public String dbName;
	public String user;
	public String password;
	public int poolMin;
	public int poolMax;
	public int checkoutTimeout;
	public int idleConnectionTestPeriod;
	public int maxIdleTime;
	public String preferredTestQuery;
	public boolean testConnectionOnCheckin;
}
