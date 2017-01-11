package flygame.extensions.db;

import java.util.List;

public class DbGroupConfig {
	public final int startRound;
	public final List<DbNodeConfig> nodes;
	public final boolean enableRound;
	public final boolean checkAvailable;

	public DbGroupConfig(int startRound, List<DbNodeConfig> nodes, boolean checkAvailable) {
		this.startRound = startRound;
		this.nodes = nodes;
		this.enableRound = nodes.size() > 1;
		this.checkAvailable = checkAvailable;
	}
}
