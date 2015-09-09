package org.sxb.web.exception;

/**
 * @author jeffson
 */
public class ClassLoadException extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = -652750933507770078L;

	public ClassLoadException(String message) {
        super(message);   
    }

    public ClassLoadException(Throwable cause) {
        super(cause);   
    }
}
