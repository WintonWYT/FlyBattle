package flygame.common.db.asynwriter;

import flygame.extensions.db.DbManager;

import java.util.ArrayList;

/**
 * 用于创建和管理DbLogCacheHandler对象
 * @author chenjiayao
 */
public class DbLogAsynHandlers {
	/** 维护所有的handle */
	private static ArrayList<DbLogAsynHandler<?>> list = new ArrayList<DbLogAsynHandler<?>>();
	/** 将获得的handler设置静态变量, 尽量少创建handler, 应该是一个writer对应一个handle的 */
	public static<T> DbLogAsynHandler<T> createHandler(DbLogAsynWriter<T> writer){
		DbLogAsynHandler<T> handle = new DbLogAsynHandler<T>(writer);
		list.add(handle);
		return handle;
	}

	/**
	 * insert类型的简单log方法
	 * eg:
	 * handle = createCommonHandler("insert into testlog(id,name)VALUES(?,?)"); 
	 * handle.putMsg(new Object[]{769394,"陈嘉耀"});
	 */
	public static DbLogAsynHandler<Object[]> createCommonHandler(String logSql){
		DbLogAsynWriter<Object[]> writer = new DbLogAsynCommonWriter(logSql);
		return createHandler(writer);
	}
	public static DbLogAsynHandler<Object[]> createCommonHandler(DbManager dbm, String logSql, int minNum, int maxNum){
		DbLogAsynWriter<Object[]> writer = new DbLogAsynCommonWriter(logSql, dbm);
		return createHandler(writer, minNum, maxNum);
	}

	public static<T> DbLogAsynHandler<T> createHandler(DbLogAsynWriter<T> writer, int minNum, int maxNum){
		DbLogAsynHandler<T> handle = new DbLogAsynHandler<T>(writer, minNum, maxNum);
		list.add(handle);
		return handle;
	}
	/** 关闭所有的handle,写回所有剩余的缓冲数据并关闭线程 */
	public static void shutdown(){
		for(DbLogAsynHandler<?> handle:list){
			handle.shutdown();
		}
	}

}
