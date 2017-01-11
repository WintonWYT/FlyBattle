package flygame.extensions.db;

import flygame.common.ApplicationLocal;
import flygame.extensions.utils.PoolThreadFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class DbAvailableChecker {
	public final static DbAvailableChecker instance = new DbAvailableChecker();
	private static final long CHECK_INTERVAL = 15;

	private AtomicBoolean start = new AtomicBoolean(false);
	private final ScheduledExecutorService scheduler = Executors
			.newSingleThreadScheduledExecutor(new PoolThreadFactory("DbCheckScheduler", true));

	public void start() {
		if (!start.compareAndSet(false, true)) {
			return;
		}

		Runnable checker = new Runnable() {
			@Override
			public void run() {
				try {
					checkAvailable();
				} catch (Exception e) {
					ApplicationLocal.instance().error("check database available error :", e);
				}
			}
		};
		scheduler.scheduleAtFixedRate(checker, 180, CHECK_INTERVAL, TimeUnit.SECONDS);
	}

	public void stop() {
		if (!start.compareAndSet(true, false)) {
			return;
		}

		this.scheduler.shutdown();
	}

	private synchronized void checkAvailable() {
		List<DbNode> nodes = DbManager.dbGroupList.getAllDbNodes();
		for (DbNode node : nodes) {
			if (node.checkAvailable && !node.isFailed()) {
				node.checkAvailable();
			}
		}
	}
}
