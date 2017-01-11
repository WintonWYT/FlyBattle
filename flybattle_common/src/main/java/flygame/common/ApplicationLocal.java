package flygame.common;

public abstract class ApplicationLocal {

	private static volatile ApplicationLocal _appLocal = null;

	public static synchronized void init(ApplicationLocal appLocal) {
		if (_appLocal == null) {
			_appLocal = appLocal;
		}
	}

	public static ApplicationLocal instance() {
		if (_appLocal == null) {
			try {
				_appLocal = (ApplicationLocal) Class.forName("com.server.extensions.MyApplicationLocal").newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return _appLocal;
	}

	public abstract void info(String msg);

	public abstract void error(String msg);

	public abstract void error(String msg, Throwable t);

	public boolean enableSqlProfile() {
		return false;
	}

	public void sql(String sql) {
	}

	public boolean enableSqlCounter() {
		return false;
	}

	public String sqlCounterResultDir() {
		return null;
	}

	public boolean batchSqlConfigOptimize() {
		return false;
	}

	public abstract int getZoneId();
}
