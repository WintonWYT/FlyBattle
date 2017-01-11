package flygame.common.reloader;

import flygame.extensions.db.ResultObjectBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReloadItem {
	public final String name;
	final String updateTime;
	public final String argument;

	ReloadItem(String name, String updateTime, String argument) {
		this.name = name;
		this.updateTime = updateTime;
		if (argument != null) {
			this.argument = argument.trim();
		} else {
			this.argument = null;
		}
	}

	static ResultObjectBuilder<ReloadItem> resultBuilder = new ResultObjectBuilder<ReloadItem>() {
		public ReloadItem build(ResultSet rs) throws SQLException {
			return new ReloadItem(rs.getString("Name"), rs.getString("UpdateTime"), rs.getString("Arg"));
		}
	};
}
