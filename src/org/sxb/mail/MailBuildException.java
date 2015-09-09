package org.sxb.mail;

/**
 * MimeMessageオブジェクトの生成に失敗した際にスローされる例外。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * 
 * @version $Id: MailBuildException.java,v 1.0
 */
public class MailBuildException extends MailException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8383268766869838742L;

	/**
	 * @param message 
	 */
	public MailBuildException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause 
	 */
	public MailBuildException(String message, Throwable cause) {
		super(message, cause);
	}

}