package rostest;

public class ApplicationException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = 817337250242180488L;

	public ApplicationException(String message) {
		super(message);
	}

	public ApplicationException(String message, Exception cause) {
		super(message, cause);
	}
}