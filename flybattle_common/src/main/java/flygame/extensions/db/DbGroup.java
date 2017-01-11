package flygame.extensions.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

//一个包括master/slaves的数据库实例组
class DbGroup {
	private static final ThreadLocal<Boolean> globalWriteOnly = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	static void writeOnly() {
		globalWriteOnly.set(true);
	}

	private final String name;
	private DbNode master;
	// 多线程只读，线程安全
	private DbNode[] nodes;
	private boolean enableRound;
	private int startRound;
	private int maxNodeIndex;
	private int slice;

	public DbGroup(String name, DbGroupConfig groupConfig) {
		this.name = name;
		nodes = new DbNode[groupConfig.nodes.size()];
		for (int i = 0; i < groupConfig.nodes.size(); i++) {
			DbNode node = new DruidDbNode(groupConfig.nodes.get(i), groupConfig.checkAvailable);
			nodes[i] = node;

			if (i == 0) {
				master = node;
			}
		}

		this.enableRound = groupConfig.enableRound;
		if (this.enableRound) {
			this.startRound = groupConfig.startRound;
			this.slice = this.startRound - 1;
		}

		this.maxNodeIndex = nodes.length - 1;
	}

	String getName() {
		return this.name;
	}

	Connection getConnection(boolean readonly) {
		DbNode node = null;

		if (readonly && this.enableRound && (!globalWriteOnly.get())) {
			node = nodes[getSlice()];
		} else {
			node = master;
		}

		globalWriteOnly.set(false);

		if (node.isFailed()) {
			node = handleFailedNode(node);
			if (node == null || node.isFailed()) {
				throw new DbException("no available database !!!");
			}
		}

		try {
			Connection conn = node.getConnection();
			conn.setReadOnly(readonly);
			return conn;
		} catch (Exception e) {
			throw new DbException("Database connection pool get connection error:", e);
		}
	}

	// 当数据库检测程序检测到某个节点坏了，有两种处理方式：
	// 1. 重组master、nodes和slice等轮询相关变量，去掉挂的节点。
	// 2. 标记节点是否挂的状态，轮询之后再做处理。
	// 目前选择方式2，考虑到方式1重组代码逻辑比较复杂，涉及并发和没有可用节点重组情况。
	// 方式2逻辑实现比较清晰，略有性能代价，但数据库挂不是一个常态分支，而且几个简单条件
	// 判断相对数据库操作来讲，代价几乎忽略不计。
	private DbNode handleFailedNode(DbNode failedNode) {
		DbNode altNode = null; //alt = alternative
		if (failedNode == master) {
			return getFirstSlave();
		} else {
			altNode = getAvailableSlave();
			if (altNode != null) {
				return altNode;
			} else {
				return master;
			}
		}
	}

	private DbNode getFirstSlave() {
		if (nodes.length > 1) {
			return nodes[1];
		}
		return null;
	}

	private DbNode getAvailableSlave() {
		// loop start from 1, ignore master
		for (int i = 1; i < nodes.length; i++) {
			if (!nodes[i].isFailed()) {
				return nodes[i];
			}
		}
		return null;
	}

	List<DbNode> allNodes() {
		return Arrays.asList(nodes);
	}

	private synchronized int getSlice() {
		slice++;
		if (slice > maxNodeIndex) {
			slice = startRound;
		}
		return slice;
	}

	void destroy() {
		try {
			for (DbNode node : this.nodes) {
				node.destroy();
			}
		} catch (SQLException ex) {
			DbManager.logError("destroy data source error:", ex);
		}
	}
}
