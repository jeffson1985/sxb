package org.sxb.mail;

/**
 * SMTPサーバに接続していない時に、接続状態を要する処理を実行してスローされる例外。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: NotConnectedException.java,v 1.0
 */
public class NotConnectedException extends MailException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1991643921111955685L;

	/**
	 * @param message 
	 */
	public NotConnectedException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause 
	 */
	public NotConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

}