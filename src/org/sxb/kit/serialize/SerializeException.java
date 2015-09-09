package org.sxb.kit.serialize;

/**
 * DBException
 */
public class SerializeException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 4270288217261214851L;

public SerializeException() {
  }

  public SerializeException(String message) {
    super(message);
  }

  public SerializeException(Throwable cause) {
    super(cause);
  }

  public SerializeException(String message, Throwable cause) {
    super(message, cause);
  }
}










