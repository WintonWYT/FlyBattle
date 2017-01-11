package flygame.extensions.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public final class ScalarResultBuilder {

	public final static ResultObjectBuilder<Integer> intResultBuilder;
	public final static ResultObjectBuilder<Date> dateResultBuilder;
	public final static ResultObjectBuilder<String> stringResultBuilder;

	static {
		intResultBuilder = new ResultObjectBuilder<Integer>() {
			public Integer build(ResultSet rs) throws SQLException {
				return rs.getInt(1);
			}
		};

		dateResultBuilder = new ResultObjectBuilder<Date>() {
			public Date build(ResultSet rs) throws SQLException {
				return new Date(rs.getTimestamp(1).getTime());
			}
		};

		stringResultBuilder = new ResultObjectBuilder<String>() {
			public String build(ResultSet rs) throws SQLException {
				return rs.getString(1);
			}
		};
	}
}
