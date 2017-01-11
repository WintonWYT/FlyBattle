package flygame.common.reloader;

import flygame.common.ApplicationLocal;
import flygame.extensions.utils.PoolThreadFactory;
import flygame.extensions.db.DbManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Reloader {
	public final static Reloader instance = new Reloader();
	private static final long CHECK_INTERVAL = 15;

	private AtomicBoolean start = new AtomicBoolean(false);
	private final Map<String, Collection<ReloadHandler>> nameHandlers = new HashMap<String, Collection<ReloadHandler>>();
	private final ScheduledExecutorService scheduler = Executors
			.newSingleThreadScheduledExecutor(new PoolThreadFactory("ReloadScheduler", true));

	private final Map<String, String> reloadItems = new HashMap<String, String>();

	public void start(Map<String, ReloadHandler> nameReloadHandlers) {
		if (!start.compareAndSet(false, true)) {
			return;
		}

		this.initItems();

		Runnable checker = () -> checkReload();
		scheduler.scheduleAtFixedRate(checker, 60, CHECK_INTERVAL, TimeUnit.SECONDS);

		addHandler("database", new DatabaseReloaderHandler());
		//addHandler("memcached", new MemcachedReloaderHandler());

		if (nameReloadHandlers != null) {
			for (Entry<String, ReloadHandler> en : nameReloadHandlers.entrySet()) {
				this.addHandler(en.getKey(), en.getValue());
			}
		}
	}

	public void start() {
		this.start(null);
	}

	// TODO 需求先把代码兼容，当所有项目都移库配置再作处理
	private Iterable<ReloadItem> getItems() {
		String sql = "SELECT Name, UpdateTime, Arg FROM As_ReloadItems";
		DbManager dbm = DbManager.getASConfigDb();
		if (dbm == null) {
			dbm = DbManager.instance();
		}
		return dbm.executeQuery_ObjectList(sql, ReloadItem.resultBuilder);
	}

	private void initItems() {
		Iterable<ReloadItem> items = getItems();
		for (ReloadItem item : items) {
			reloadItems.put(item.name, item.updateTime);
		}
	}

	public void stop() {
		if (!start.compareAndSet(true, false)) {
			return;
		}

		this.scheduler.shutdown();
	}

	/** 增加一个指定项的观察者 */
	public void addHandler(String name, ReloadHandler handler) {
		synchronized (this.nameHandlers) {
			Collection<ReloadHandler> handlers = this.nameHandlers.get(name);
			if (handlers == null) {
				handlers = new ArrayList<ReloadHandler>(1);
				this.nameHandlers.put(name, handlers);
			}
			handlers.add(handler);
		}
	}

	private void checkReload() {
		Iterable<ReloadItem> items = getItems();
		for (ReloadItem newItem : items) {
			String oldUpdate = reloadItems.get(newItem.name);
			boolean update = (oldUpdate == null) || !newItem.updateTime.equals(oldUpdate);
			if (update) {
				reloadItems.put(newItem.name, newItem.updateTime);
				dispatchEvent(newItem);
			}
		}
	}

	private void dispatchEvent(ReloadItem item) {

		ApplicationLocal.instance().info(
				String.format("%s reloaded with update : %s, arg : %s", item.name, item.updateTime, item.argument));

		Iterable<ReloadHandler> handlers = nameHandlers.get(item.name);
		if (handlers != null) {
			dispatchEvent(item, handlers);
		}
	}

	private void dispatchEvent(ReloadItem item, Iterable<ReloadHandler> handlers) {
		for (ReloadHandler handler : handlers) {
			try {
				handler.reload(item);
			} catch (Throwable t) {
				ApplicationLocal.instance().error(String.format("reload [%s] error : ", item.name), t);
			}
		}
	}
}
