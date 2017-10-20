package fr.inra.maiage.bibliome.alvisdb;

public class ADBException extends Exception {
	private static final long serialVersionUID = 1L;

	public ADBException() {
		super();
	}

	protected ADBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ADBException(String message, Throwable cause) {
		super(message, cause);
	}

	public ADBException(String message) {
		super(message);
	}

	public ADBException(Throwable cause) {
		super(cause);
	}
}
