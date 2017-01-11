package flygame.common.db;

import flygame.extensions.db.DbManager;
import flygame.extensions.db.QueryCmd;
import flygame.extensions.db.QueryMapper;
import flygame.extensions.db.TableMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MultiIdUserQueryMapper implements QueryMapper {
	@SuppressWarnings("unchecked")
	public List<QueryCmd> map(String cmd, Object... params) {
		List<Integer> idList = (List<Integer>) params[0];
		Map<Integer, List<Integer>> part_ids = partIdList(idList);
		List<QueryCmd> cmds = new ArrayList<QueryCmd>();

		int partIndex;
		String sql;
		for (Entry<Integer, List<Integer>> en : part_ids.entrySet()) {
			partIndex = en.getKey();
			String clause = StringUtils.join(en.getValue(), ",");
			sql = String.format(cmd, partIndex, clause);
			// 参数都拼到IN()里去了，所以是null
			cmds.add(new QueryCmd(DbManager.getPartitionDb(partIndex), sql,
					null));
		}

		return cmds;
	}

	private Map<Integer, List<Integer>> partIdList(List<Integer> idList) {
		Map<Integer, List<Integer>> part_ids = new HashMap<Integer, List<Integer>>();
		List<Integer> onePartIdList;
		int partIndex;

		for (Integer id : idList) {
			// User表是一千万一个表
			partIndex = id / TableMapper.DB_PARTITION_UNIT;
			if (part_ids.containsKey(partIndex)) {
				onePartIdList = part_ids.get(partIndex);
			} else {
				onePartIdList = new ArrayList<Integer>();
				part_ids.put(partIndex, onePartIdList);
			}
			onePartIdList.add(id);
		}

		return part_ids;
	}

	public final static QueryMapper instance = new MultiIdUserQueryMapper();
}
