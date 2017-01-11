package flygame.extensions.db;

/**
 * A unchecked exception for DbManager.
 * 
 * @author denglinghua
 * 
 */
public class DbException extends RuntimeException {

	private static final long serialVersionUID = 5921228986101865095L;

	public DbException() {
		super();
	}

	public DbException(String msg) {
		super(msg);
	}

	public DbException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
