package org.sxb.plugin.quartz.exception;

/**
 * DBException
 */
public class QuartzException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 6983188093833015126L;

public QuartzException() {
  }

  public QuartzException(String message) {
    super(message);
  }

  public QuartzException(Throwable cause) {
    super(cause);
  }

  public QuartzException(String message, Throwable cause) {
    super(message, cause);
  }
}










