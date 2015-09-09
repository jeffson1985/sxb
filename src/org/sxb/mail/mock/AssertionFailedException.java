package org.sxb.mail.mock;

/**
 * MockSendMailで期待メールと送信メールが一致しなかった時にスローされる非チェック例外。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: AssertionFailedException.java,v 1.2 
 */
public class AssertionFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9098672637490273907L;

	public AssertionFailedException() {
		super();
	}

	public AssertionFailedException(String message) {
		super(message);
	}

	public AssertionFailedException(Throwable cause) {
		super(cause);
	}

	public AssertionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}