package acs.logic;

public class ObjectNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -3183057250236533850L;

	public ObjectNotFoundException() {
		super();
	}

	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
