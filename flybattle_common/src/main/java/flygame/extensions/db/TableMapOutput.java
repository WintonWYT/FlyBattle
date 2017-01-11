package flygame.extensions.db;

import javax.sql.rowset.CachedRowSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class TableMapOutput {
	// table name or sql
	public final String cmdText;
	public final DbManager db;

	public TableMapOutput(String cmdText, DbManager db) {
		this.cmdText = cmdText;
		this.db = db;
	}

	public ArrayList<DataRow> executeQuery() {
		return this.db.executeQuery(this.cmdText);
	}

	public ArrayList<DataRow> executeQueryEx() {
		return this.db.executeQueryEx(this.cmdText);
	}

	public ArrayList<DataRow> executeQuery(int dataRowType) {
		return this.db.executeQuery(this.cmdText, dataRowType);
	}

	public ArrayList<DataRow> executeQueryEx(int dataRowType) {
		return this.db.executeQueryEx(this.cmdText, dataRowType);
	}

	public ArrayList<DataRow> executeQuery(Object[] params) {
		return this.db.executeQuery(this.cmdText, params);
	}

	public ArrayList<DataRow> executeQueryEx(Object[] params) {
		return this.db.executeQueryEx(this.cmdText, params);
	}

	public <T> ArrayList<T> executeQuery_ObjectList(ResultObjectBuilder<T> builder) {
		return this.db.executeQuery_ObjectList(this.cmdText, builder);
	}

	public <T> ArrayList<T> executeQuery_ObjectListEx(ResultObjectBuilder<T> builder) {
		return this.db.executeQuery_ObjectListEx(this.cmdText, builder);
	}

	public Boolean executeScalarBool() {
		return this.db.executeScalarBool(this.cmdText);
	}

	public Boolean executeScalarBool(Object[] params) {
		return this.db.executeScalarBool(this.cmdText, params);
	}

	public Integer executeScalarInt() {
		return this.db.executeScalarInt(this.cmdText);
	}

	public Integer executeScalarInt(Object[] params) {
		return this.db.executeScalarInt(this.cmdText, params);
	}

	public Long executeScalarLong() {
		return this.db.executeScalarLong(this.cmdText);
	}

	public Long executeScalarLong(Object[] params) {
		return this.db.executeScalarLong(this.cmdText, params);
	}

	public String executeScalarString() {
		return this.db.executeScalarString(this.cmdText);
	}

	public String executeScalarString(Object[] params) {
		return this.db.executeScalarString(this.cmdText, params);
	}

	public Date executeScalarDate() {
		return this.db.executeScalarDate(this.cmdText);
	}

	public Date executeScalarDate(Object[] params) {
		return this.db.executeScalarDate(this.cmdText, params);
	}

	public <T> T executeScalarObject(ResultObjectBuilder<T> builder) {
		return this.db.executeScalarObject(this.cmdText, builder);
	}

	public <T> T executeScalarObject(Object[] params, ResultObjectBuilder<T> builder) {
		return this.db.executeScalarObject(this.cmdText, params, builder);
	}

	public <T> T executeScalarObjectEx(ResultObjectBuilder<T> builder) {
		return this.db.executeScalarObjectEx(this.cmdText, builder);
	}

	public <T> T executeScalarObjectEx(Object[] params, ResultObjectBuilder<T> builder) {
		return this.db.executeScalarObjectEx(this.cmdText, params, builder);
	}

	public boolean isRecordExist() {
		return this.db.isRecordExist(this.cmdText);
	}

	public boolean isRecordExist(Object[] params) {
		return this.db.isRecordExist(this.cmdText, params);
	}

	public int executeCommand() {
		return this.db.executeCommand(this.cmdText);
	}

	public int executeCommandEx() {
		return this.db.executeCommandEx(this.cmdText);
	}

	public int executeCommand(Object[] params) {
		return this.db.executeCommand(this.cmdText, params);
	}

	public int executeCommandEx(Object[] params) {
		return this.db.executeCommandEx(this.cmdText, params);
	}

	public int[] executeBatchCommand(Collection<Object[]> paramsList) {
		return this.db.executeBatchCommand(this.cmdText, paramsList);
	}

	public int[] executeBatchCommandEx(Collection<Object[]> paramsList) {
		return this.db.executeBatchCommandEx(this.cmdText, paramsList);
	}

	public <T> int[] executeBatchCommand(IParamsBuilder<T> builder, Collection<T> paramsList) {
		return this.db.executeBatchCommand(this.cmdText, builder, paramsList, null);
	}

	public <T> int[] executeBatchCommandEx(IParamsBuilder<T> builder, Collection<T> paramsList) {
		return this.db.executeBatchCommandEx(this.cmdText, builder, paramsList, null);
	}

	public ArrayList<DataRow> executeQuery(Object[] params, int dataRowType) {
		return this.db.executeQuery(this.cmdText, params, dataRowType);
	}

	public ArrayList<DataRow> executeQueryEx(Object[] params, int dataRowType) {
		return this.db.executeQueryEx(this.cmdText, params, dataRowType);
	}

	public <T> ArrayList<T> executeQuery_ObjectList(Object[] params, ResultObjectBuilder<T> builder) {
		return this.db.executeQuery_ObjectList(this.cmdText, params, builder);
	}

	public <T> ArrayList<T> executeQuery_ObjectListEx(Object[] params, ResultObjectBuilder<T> builder) {
		return this.db.executeQuery_ObjectListEx(this.cmdText, params, builder);
	}

	// 返回该查询是否有结果
	// 为什么会有这个方法呢？cmd参数都没用的。
	@Deprecated
	public boolean isRecordExist(String cmd, Object[] params) {
		return this.db.isRecordExist(this.cmdText, params);
	}

	public CachedRowSet executeQueryRowSet(Object[] params) {
		return this.db.executeQuery_RowSet(this.cmdText, params);
	}
}
