package org.sxb.mail.mock;

import org.sxb.mail.Mail;

/**
 * 値がセットされた項目だけ送信メールと比較する、MockSendMailでのテスト用Mailクラス。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MockMail.java,v 1.5
 */
public class MockMail extends Mail {

	/**
	 * コンストラクタ。
	 */
	public MockMail() {}

	/**
	 * コピーコンストラクタ。
	 * シャローコピー(shallow copy)です。
	 * 
	 * @author    Jeffson  (jeffson.app@gmail.com).2
	 * 
	 * @param original コピー元のMailインスタンス
	 */
	public MockMail(Mail original) {
		super(original);
	}

}