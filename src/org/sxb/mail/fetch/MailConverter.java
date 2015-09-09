package org.sxb.mail.fetch;

import javax.mail.internet.MimeMessage;

/**
 * <code>MimeMessage</code>から<code>ReceivedMail</code>を生成するインターフェース。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailConverter.java,v 1.1
 */
public interface MailConverter {

	/**
	 * 指定された<code>MimeMessage</code>を<code>ReceivedMail</code>に変換して返します。
	 * 
	 * @param message <code>ReceivedMail</code>に変換する<code>MimeMessage</code>
	 * @return <code>MimeMessage</code>から生成された<code>ReceivedMail</code>
	 */
	ReceivedMail convertIntoMail(MimeMessage message);

	/**
	 * 指定された<code>MimeMessage</code>を<code>ReceivedMail</code>に変換して返します。
	 * 
	 * @param message <code>ReceivedMail</code>に変換する<code>MimeMessage</code>の配列
	 * @return <code>MimeMessage</code>から生成された<code>ReceivedMail</code>の配列
	 */
	ReceivedMail[] convertIntoMails(MimeMessage[] messages);

}