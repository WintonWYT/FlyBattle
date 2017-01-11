package flygame.common.db.asynwriter;

import flygame.common.ApplicationLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DbLogCacheHandler用于缓存将写回数据库的不可改写的数据,在到达一定阀值之后由handle创建的另一线程将数据批量写回数据库
 * 初始化后立即启动线程
 * @author chenjiayao
 * @param <T>不可改写的数据
 */
public class DbLogAsynHandler<T> {
	public final static int DefaultMinNum = 50;
	public final static int DefaultMaxNum = 20000;

	// 目前仅用于记录日志，标识是哪个实例，T标识太泛了。
	private String name = "";

	private DbLogAsynWriter<T> writer = null;
	private ExecutorService flushThread = Executors.newSingleThreadExecutor();;
	private List<T> msgList = null;
	private Runnable r = null;
	private int minNum;
	private int maxNum;
	private int warnNum;

	private int warnSpace;
	/** handler一旦shutdown后将不接收任何消息 */
	private boolean hasShutDown = false;

	public final static int DefaultWarnPercent = 75;
	public final static int DefaultWarnSpacePercent = 5;

	/** 为防止创建太多线程,handle只能由manager初始化 
	 * @param writer是写回数据库的方法接口*/
	protected DbLogAsynHandler(DbLogAsynWriter<T> writer) {
		this(writer, DefaultMinNum, DefaultMaxNum);
	}
	/** 为防止创建太多线程,handle只能由manager初始化 
	 * @param writer是写回数据库的方法接口*/
	protected DbLogAsynHandler(DbLogAsynWriter<T> writer, int minNum, int maxNum) {
		this.minNum = minNum;
		this.maxNum = maxNum;
		this.warnNum = maxNum*DefaultWarnPercent/100;
		this.warnSpace = maxNum*DefaultWarnSpacePercent/100;

		this.writer = writer;
		this.name = this.writer.getClass().getName();
		this.msgList = new ArrayList<T>(maxNum);
		r = new Runnable(){
			public void run() {
				handleMsg();
			}
		};
	}

	public void setName(String name) {
		this.name = name;
	}

	/** 添加要写入的数据 */
	public void putMsg(T msg) {
		if(hasShutDown){
			return;
		}
		synchronized(msgList){
			int size = msgList.size();
			//以防万一
			if(size >= warnNum){
				if((size - warnNum)%warnSpace == 0){
					String warnLog = String.format("asym[%s] type[%s] memory warn:size=%d", this.name, msg.getClass().getName(), size);
					ApplicationLocal.instance().error(warnLog);
				}
			}
			if(size < maxNum) {
				this.msgList.add(msg);
			}
			if (this.msgList.size() >= minNum) {
				flushThread.execute(r);
			}
		}
	}
	/** 系统退出前调用shutdown,否则无法退出,通常情况下由Manager统一shutdown */
	public void shutdown() {
		hasShutDown = true;
		flushThread.shutdown();
		//清除尾巴
		handleMsg();
		//以防万一
		for(int i = 0; i<10; i++){
			if(flushThread.isTerminated())
				break;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				ApplicationLocal.instance().error("",null);
			}
			if(i > 0){
				String warnLog = String.format("shutdown delay %d times!",  i);
				ApplicationLocal.instance().error(warnLog);
			}
		}
	}

	private void handleMsg() {
		ArrayList<T> temps = new ArrayList<T>();
		synchronized(msgList){
			if(msgList.size() == 0)
				return ;
			temps.addAll(msgList);
			this.msgList.clear();
		}
		try {	//防止线程死掉
			writer.flush(temps);
		} catch (Throwable t){
			ApplicationLocal.instance().error("", t);
		}
	}
}
