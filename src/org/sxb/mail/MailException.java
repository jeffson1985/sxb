package org.sxb.mail;

/**
 * メール関連例外の規定クラス。RuntimeExceptionを継承しています。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailException.java,v 1.0
 */
public class MailException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3271536870421158526L;

	/**
	 * @param message 
	 */
	public MailException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause 
	 */
	public MailException(String message, Throwable cause) {
		super(message, cause);
	}

}