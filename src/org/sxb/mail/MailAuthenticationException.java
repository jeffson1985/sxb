package org.sxb.mail;

/**
 * SMTPサーバ接続の認証に失敗した際にスローされる例外。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * 
 * @version $Id: MailAuthenticationException.java,v 1.0
 */
public class MailAuthenticationException extends MailException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2462961845270173650L;

	/**
	 * @param message 
	 */
	public MailAuthenticationException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause 
	 */
	public MailAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailAuthenticationException(Throwable cause) {
		super("Authentication failed: " + cause.getMessage(), cause);
	}

}

