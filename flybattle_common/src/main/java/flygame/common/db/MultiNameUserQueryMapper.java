package flygame.common.db;

import flygame.extensions.db.DbManager;
import flygame.extensions.db.QueryCmd;
import flygame.extensions.db.QueryMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Deprecated
public class MultiNameUserQueryMapper implements QueryMapper {
	@SuppressWarnings("unchecked")
	public List<QueryCmd> map(String cmd, Object... params) {
		List<String> nameList = (List<String>) params[0];
		Map<Integer, List<String>> part_names = partNameList(nameList);
		List<QueryCmd> cmds = new ArrayList<QueryCmd>();

		int partIndex;
		String sql;
		for (Entry<Integer, List<String>> en : part_names.entrySet()) {
			partIndex = en.getKey();
			String clause = StringUtils.join(en.getValue(), "','");
			sql = String.format(cmd, partIndex, clause);
			// 参数都拼到IN()里去了，所以是null
			cmds.add(new QueryCmd(DbManager.instance(), sql, null));
		}

		return cmds;
	}

	private Map<Integer, List<String>> partNameList(List<String> nameList) {
		Map<Integer, List<String>> part_names = new HashMap<Integer, List<String>>();
/*
		List<String> onePartNameList;
		int partIndex;

		for (String name : nameList) {
			if (name.indexOf("'") > 0) {
				continue;
			}
			partIndex = TableMapper.getUserName2IdTableIndex(name);
			if (part_names.containsKey(partIndex)) {
				onePartNameList = part_names.get(partIndex);
			} else {
				onePartNameList = new ArrayList<String>();
				part_names.put(partIndex, onePartNameList);
			}
			onePartNameList.add(name);
		}
*/

		return part_names;
	}

	public final static QueryMapper instance = new MultiNameUserQueryMapper();
}
