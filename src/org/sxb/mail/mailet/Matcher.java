package org.sxb.mail.mailet;

import org.sxb.mail.fetch.ReceivedMail;

/**
 * Mailetの実行条件を満たすかどうか判定するインターフェース。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: Matcher.java,v 1.1.3
 */
public interface Matcher {

	/**
	 * 指定された受信メールがMailet実行条件を満たすかどうか判定します。
	 * 
	 * @param mail 受信メール
	 * @return 受信メールMailet実行条件を満たす場合 true
	 */
	boolean match(ReceivedMail mail);

}