package org.sxb.mail;

/**
 * メール送信に失敗した時にスローされる例外。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailSendException.java,v 1.0
 */
public class MailSendException extends MailException {

	private static final long serialVersionUID = -8590978542027055148L;

	/**
	 * @param message
	 */
	public MailSendException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MailSendException(String message, Throwable cause) {
		super(message, cause);
	}

}