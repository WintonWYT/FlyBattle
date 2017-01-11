package flygame.common.reloader;

import flygame.extensions.db.DbManager;
import org.apache.commons.lang3.StringUtils;

class DatabaseReloaderHandler implements ReloadHandler {
	@Override
	public void reload(ReloadItem item) {
		if (StringUtils.isNotEmpty(item.argument)) {
			String[] args = item.argument.split(",");
			for (String arg : args) {
				DbManager.reConfigDb(arg.trim());
			}
		} else {
			DbManager.reConfigDb();
		}
	}
}
