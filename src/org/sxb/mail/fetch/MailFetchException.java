package org.sxb.mail.fetch;

import org.sxb.mail.MailException;

/**
 * メールの受信に失敗した時にスローされる例外。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailFetchException.java,v 1.1
 */
public class MailFetchException extends MailException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1482381229947732754L;

	/**
	 * @param message 
	 */
	public MailFetchException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause 
	 */
	public MailFetchException(String message, Throwable cause) {
		super(message, cause);
	}

}