package flygame.common.db;

import flygame.common.ApplicationLocal;
import flygame.extensions.utils.PoolThreadFactory;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SqlCounter {

	public static final SqlCounter instance = new SqlCounter();

	private static final int DUMP_INTERVAL_MINS = 30;
	private static final char TABLE_MARK = '%';

	private ConcurrentHashMap<String, AtomicInteger> sqls = new ConcurrentHashMap<String, AtomicInteger>();
	private ScheduledExecutorService dumper = Executors.newSingleThreadScheduledExecutor(new PoolThreadFactory(
			"SqlCounter", true));

	private SqlCounter() {
		this.start();
	}

	private void start() {
		Runnable r = new Runnable() {
			public void run() {
				dump();
			}
		};
		dumper.scheduleAtFixedRate(r, 1, DUMP_INTERVAL_MINS, TimeUnit.MINUTES);
	}

	public void stop() {
		try {
			this.dumper.shutdownNow();
		} catch (Exception ignoreEx) {
			// ignore any exception，不要影响caller
		}
	}

	public void putSql(String sql) {
		// 为什么不用putIfAbsent ?
		// 因为每次都要new AtomicInteger(1)，内存比较浪费。
		// 以下代码尽管不简洁，而且有同步问题，但因为计数并不要求严格，所以没同步。
		String sqlKey = this.checkSql(sql);
		if (sqlKey == null) {
			return;
		}
		AtomicInteger count = sqls.get(sqlKey);
		if (count == null) {
			sqls.put(sqlKey, new AtomicInteger(1));
		} else {
			count.incrementAndGet();
		}
	}

	// 某些sql是根据变量拼凑的，可能是个无限集，所以要对sql进行处理。
	private String checkSql(String sql) {
		int pos = sql.indexOf(TABLE_MARK);
		if (pos == -1) {
			// 没有%的sql不处理
			return null;
		}
		return sql.substring(0, pos);
	}

	private void dump() {
		StringBuilder sb = new StringBuilder();
		// 这里遍历因为是ConcurrentHashMap，应该不会报错
		// 一致性不是问题，因为计数器要求不严格
		for (Entry<String, AtomicInteger> en : sqls.entrySet()) {
			sb.append(en.getKey());
			sb.append("\t");
			sb.append(en.getValue().get());
			sb.append("\n");
		}

		writeToFile(sb.toString());
	}

	private String createDumpFileName() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		return "sql_" + f.format(new Date());
	}

	private boolean ensureDirExist(String path) {
		File dir = new File(path);
		if (!dir.isDirectory()) {
			return dir.mkdir();
		}
		return true;
	}

	private String checkPathSeparator(String dir) {
		if (dir.endsWith(File.separator)) {
			return dir;
		}
		return dir + File.separator;
	}

	private void writeToFile(String content) {
		FileWriter fw = null;
		try {
			String dir = ApplicationLocal.instance().sqlCounterResultDir();
			if (!ensureDirExist(dir)) {
				ApplicationLocal.instance().error("dump sql counter result error : path error");
				return;
			}
			dir = checkPathSeparator(dir);
			fw = new FileWriter(dir + createDumpFileName());
			fw.write(content);
			fw.flush();
		} catch (Exception e) {
			ApplicationLocal.instance().error("dump sql counter result error:", e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception ignoreEx) {
				}
			}
		}
	}
}
