package flygame.extensions.db;

import flygame.common.ApplicationLocal;
import flygame.extensions.utils.PoolThreadFactory;

import javax.sql.rowset.CachedRowSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DistributedDbQuery {

	private final static int QUERY_THREAD_COUNT = 20;
	private final static int QUERY_TIME_OUT = 5;// second

	private final ExecutorService threadPool = Executors
			.newFixedThreadPool(QUERY_THREAD_COUNT, new PoolThreadFactory("DistributedDbQuery", true));

	public final static DistributedDbQuery instance = new DistributedDbQuery();

	private DistributedDbQuery(){
	}

	/**
	 * @param cmd
	 *            即sql，由mapper控制，sql和mapper都由用户配套提供
	 * @param mapper
	 *            把sql和params映射成多个库上执行的命令
	 * @param params
	 *            任意参数，由mapper来控制
	 * @return 如果执行错误或超时，返回null
	 */
	public List<CachedRowSet> query(String cmd, QueryMapper mapper,
									Object... params) {
		List<QueryCmd> cmds = mapper.map(cmd, params);
		int cmdCount = cmds.size();

		if (cmdCount == 0) {
			return new ArrayList<CachedRowSet>();
		}

		CountDownLatch doneSignal = new CountDownLatch(cmdCount);
		QueryWorker[] workers = new QueryWorker[cmdCount];

		for (int i = 0; i < cmdCount; i++) {
			workers[i] = new QueryWorker(doneSignal, cmds.get(i));
			threadPool.execute(workers[i]);
		}

		try {
			// 这里设置超时的目的是防止分布式查询线程池阻塞(说明某个part数据库很忙)导致主线程阻塞
			// 如果超时，即!ok，直接返回空结果集,一个失败都失败
			boolean ok = doneSignal.await(QUERY_TIME_OUT, TimeUnit.SECONDS);
			if (!ok) {
				ApplicationLocal.instance().error(
						"DistributedDbQuery time out : " + cmd);
				return null;
			}
		} catch (InterruptedException ex) {
			return null;
		}

		return reduceResult(workers);
	}

	public <T> ArrayList<T> query(String cmd,
								  ResultObjectBuilder<T> resultBuilder, QueryMapper mapper,
								  Object... params) {
		List<CachedRowSet> result = this.query(cmd, mapper, params);
		if (result == null) {
			return null;
		}

		ArrayList<T> list = new ArrayList<T>();
		for (CachedRowSet rs : result) {
			list
					.addAll(DbManager.getObjectListFromResultSet(rs,
							resultBuilder));
		}

		return list;
	}

	private List<CachedRowSet> reduceResult(QueryWorker[] workers) {
		ArrayList<CachedRowSet> result = new ArrayList<CachedRowSet>();
		for (QueryWorker qw : workers) {
			// 如果执行错误了，result是null
			if (qw.result != null) {
				result.add(qw.getResult());
			} else {
				// 有一个错了，全部为null
				return null;
			}
		}
		return result;
	}

	private static class QueryWorker implements Runnable {
		private final CountDownLatch doneSignal;
		private final QueryCmd queryCmd;
		private CachedRowSet result;

		QueryWorker(CountDownLatch doneSignal, QueryCmd queryCmd) {
			this.doneSignal = doneSignal;
			this.queryCmd = queryCmd;
		}

		public void run() {
			try {
				result = queryCmd.query();
			} catch (Exception ex) {
				result = null;
			} finally {
				doneSignal.countDown();
			}
		}

		public CachedRowSet getResult() {
			return this.result;
		}
	}

	public static void shutdown(){
		instance.threadPool.shutdownNow();
	}
}
