package flygame.common.db.tablemap;

import flygame.common.ApplicationLocal;
import flygame.common.config.ConfReaderFactory;
import flygame.common.config.DbConfigReader;

import java.util.HashMap;
import java.util.Map;

public class TableMappers {

	private final static int SHI_W = 100000; // 10w
	private final static int BAI_W = 10 * SHI_W; // 100w
	private final static int QIAN_W = 10 * BAI_W; // 1000w

	public final static PartTableMapper main = createMainTableMapper();

	private static PartTableMapper createMainTableMapper() {

		Map<String, Integer> tp = new HashMap<String, Integer>();

		tp.putAll(ConfReaderFactory.getConfigReader().loadTablePartUnit());

		Map<String, String> customConfigSet = DbConfigReader.loadCustomConfig();

		int defaultDbPartUnit = loadPartUnit(customConfigSet, "DbPartUnit", QIAN_W);
		int defaultTablePartUnit = loadPartUnit(customConfigSet, "TablePartUnit", BAI_W);

		PartTableMapStrategy strategy = new PartTableMapStrategy("part", defaultDbPartUnit, tp,
				defaultTablePartUnit);

		return new PartTableMapper(strategy, ApplicationLocal.instance().enableSqlCounter());
	}

	private static int loadPartUnit(Map<String, String> params, String paramName, int defaultValue) {
		String value = params.get(paramName);
		return value != null ? Integer.parseInt(value) * 10000 : defaultValue;
	}
}
